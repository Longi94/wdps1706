from __future__ import print_function
from __future__ import division
import requests
import sys
import json
import re
import collections
import math
import lxml.html
import urllib2
from bs4 import BeautifulSoup
from nltk import word_tokenize
import nltk
import word2vec
import numpy
from collections import defaultdict

ELASTICSEARCH_URL = 'http://10.149.0.127:9200/freebase/label/_search'
TRIDENT_URL = 'http://10.141.0.124:9001/sparql'
prefixes = """
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX fbase: <http://rdf.freebase.com/ns/>
"""
same_as_template = prefixes + """
SELECT DISTINCT ?same WHERE {
		?s owl:sameAs %s .
		{ ?s owl:sameAs ?same .} UNION { ?same owl:sameAs ?s .}
}
"""
po_template = prefixes + """
SELECT DISTINCT * WHERE {
		%s ?p ?o.
}
"""

features=[]

def get_kb_entity_type(link, try_num, e_type):
	response = requests.get(link)
	if response.status_code == 200:
		html = response.content
		soup = BeautifulSoup(html,'html.parser')
		div = soup.find_all('div', class_='page-resource-uri')
		if div:
			div = div[0]
			a_tags = div.find_all('a')
			entity_type_link = a_tags[0]
			entity_name = entity_type_link.get_text()
			if try_num > 0:
				if entity_name.lower() == e_type.lower():
					return True
				elif entity_name.lower() != 'class':
					try_num = try_num - 1
					return get_kb_entity_type(entity_type_link['href'], try_num, e_type)
				elif entity_name.lower() == 'class':
					descripition_table = soup.find_all('div', class_='row')
					for row in descripition_table:
						subclass_type = row.find('table', class_="description table table-striped").find('a', rel="rdfs:subClassOf" ).get_text().split(':')[1]
						if subclass_type.lower() == e_type.lower():
							return True
						else:
							return False
			else:
				return False


def get_features(bindings, orig_sentence, model, e_type):
	print("Collecting Features")
	orig_sent_tokens = word_tokenize(orig_sentence)
	features = {}
	idToAbstract = {}
	for f_id in bindings:
		features[f_id] = []
		idToAbstract[f_id] = []
		features_properties = {}
		for binding in bindings[f_id]:
			response = requests.get(binding)
			#print("Bindings")
			#print(len(bindings[f_id]))
			if response.status_code == 200:
				print("binding")
				print(binding)
				html = response.content
				soup = BeautifulSoup(html, 'html.parser')
				entityTypeMatched = get_kb_entity_type(binding,3, e_type)
				features_properties["type_matched"] = entityTypeMatched
				features[f_id].append(features_properties)
				abstract = soup.find("span", {"property": "dbo:abstract", "xml:lang": "en"}).get_text()
				idToAbstract[f_id].append(abstract)
				#add feature to match query with title, exact match, contains, word2vec...
	#print len(idToAbstract)
	for f_id in idToAbstract:
		exact_token_match = 0
		
		for abstract in idToAbstract[f_id]:
			abstract_tokens = word_tokenize(abstract.encode('ascii','ignore'))
			similarity = 0
			feature_property = {}
			for context_token in orig_sent_tokens:
				for abstract_token in abstract_tokens:
					if context_token == abstract_token:
						exact_token_match = exact_token_match + 1
					similarity += cosine_similarity(context_token, abstract_token, model)
			
			similarity = similarity/(len(abstract_tokens) * len(orig_sent_tokens))
			feature_property["sentence_abstract_similarity"] = similarity
			features[f_id].append(feature_property)
			#break
		feature_property["sentence_abstract_exact_matches"] = exact_token_match
		features[f_id].append(feature_property)

	return json.dumps(features)
	#return idToAbstract

def get_candidates(query):
	print('Searching for "%s"...' % query)
	response = requests.get(ELASTICSEARCH_URL, params={'q': query, 'size': 100})
	labels = {}
	if response:
			response = response.json()
			for hit in response.get('hits', {}).get('hits', []):
					freebase_id = hit.get('_source', {}).get('resource')
					label = hit.get('_source', {}).get('label')
					score = hit.get('_score', 0)
					labels[label] = {'score': score, 'id': freebase_id}
	print('Found %s results.' % len(labels))

	return labels

def get_best(i):
		return math.log(facts[i]) * scores[i]

def rank_candidates(candidate_list, nr):
	facts  = {}
	u_id = 0
	for i in candidate_list:
		#print([candidate_list[i]])
		response = requests.post(TRIDENT_URL, data={'print': False, 'query': po_template % candidate_list[i]['id']})
		if response:
				response = response.json()
				n = int(response.get('stats',{}).get('nresults',0))
				#print("Found " + str(n) + " facts about " + str(i) + " [" + str(candidate_list[i]).encode('utf-8') + "] with Sparql")
				candidate_list[i]['facts'] = n
				candidate_list[i]['u_id'] = u_id
				u_id = u_id+1
				#print(n)
	#return sorted(candidate_list, key=candidate_list['facts'], reverse=True)[:n]
	return sorted(candidate_list.iteritems(), key=lambda (k,v): (v,k) , reverse=True)[:nr]

def get_freebase_ids(sorted_candidate_list):
	unique_ids = set()
	for candidate in sorted_candidate_list:
		unique_ids.add(candidate[1]['id'])
	return unique_ids

def get_trident_bindings(ids_set):
	#bindings = []
	bindings = defaultdict()
	for f_id in ids_set:
		response = requests.post(TRIDENT_URL, data={'print': True, 'query': same_as_template % f_id})
		if response:
				response = response.json()
				bindings[f_id] = []
				for binding in response.get('results', {}).get('bindings', []):
					bindings[f_id].append(binding)
	return bindings
'''
def get_abstract(query, nr_sent=1):
	response = requests.get("http://dbpedia.org/page/%s" % query.replace(" " , "_"))
	if response: 
		html = response.content

		parsed = lxml.html.fromstring(html).cssselect(
				"body > div.container > div.row > div:nth-child(1) > p")
		return parsed[0].text
'''
def get_google(query_id): 
	response = requests.get("https://www.google.com/search?q=knowledge+graph+search+api&kponly&kgmid=/" + query_id)
	if response: 
		html = response.content
		parsed = lxml.fromstring(html).cssselect("_gdf kno-fb-ctx")
		parsed_lines = lxml.fromstring(html).cssselect("")
	
def get_abstract_vector_average(abstract):
	print("test")

def compare_sent(s1, s2, model):
	tokenList1 = s1.split(" ")
	tokenList2 = s2.split(" ")

	sim = 0 
	for t1 in tokenList1: 
		for t2 in tokenList2: 
			sim += cosine_similarity(t1, t2, model)
	return sim

def cosine_similarity(w1, w2, model): 
	w1 = w1.lower()
	w2 = w2.lower()
	try: 
		return numpy.dot(model[w1], model[w2]) / (numpy.linalg.norm(model[w1]) * numpy.linalg.norm(model[w2]))
	except: 
		return 0

def fix_binding_urls(bindings):
	fixed_bindings = {}
	for f_id in bindings:
		fixed_bindings[f_id] = set()
		for binding in bindings[f_id]:
			url = binding.values()[0]["value"]
			if "dbpedia" in url:
				split_url = url.split('.')
				#print(split_url)
				split_url[-1] = "." + split_url[-1]
				if "dbpedia" in split_url[0]:
					new_url = split_url[0]
				else:
					new_url = "http://"
				for part_url in split_url[1:]:
					new_url = new_url + part_url
				#print(new_url)
				fixed_bindings[f_id].add(new_url)
	return fixed_bindings


if __name__ == "__main__":
	#nltk.download('punkt')
	query = sys.argv[1]
	model = word2vec.load(sys.argv[2])
	context = sys.argv[3]
	_type = sys.argv[4] 
	labels = get_candidates(query)
	print("-------- LABELS --------")
	#print(labels)
	top_candidates = rank_candidates(labels,5)
	print("-------- TOP CANDIDATES --------")
	#print(top_candidates)
	top_candidates_ids = get_freebase_ids(top_candidates)
	#print(top_candidates_ids)
	print("-------- TOP CANDIDATES IDS --------")
	bindings = get_trident_bindings(top_candidates_ids)
	print("-------- BINDINGS --------")
	print(len(bindings))
	#print(bindings)
	fixed_bindings = fix_binding_urls(bindings)
	print("-------- FIXED BINDINGS --------")
	print(len(fixed_bindings))
	#print(fixed_bindings)
	features = get_features(fixed_bindings, context, model, _type)
	print(features)
	#print(abstracts)


'''
if __name__ == "__main__": 
	query = sys.argv[1]
	model = sys.argv[2]
	context = sys.argv[3]
	_type = sys.argv[4]
	regex = re.compile('[^a-zA-Z]')
	regex2 = re.compile('\s+')
	context = regex.sub(' ', context)
	context = regex2.sub(' ', context)
	print(context)
	labels = get_candidates(query)
	print(labels)
	model = word2vec.load(sys.argv[2])
	best = 0 
	winner = ""
	for candidate in labels: 
		score = 0 
		print("working on cadidate:", candidate)
		abst = get_abstract(candidate)
		if not abst: 
			#print("no abstract :(")
			score = compare_sent(query + " " + _type, context, model)
			print("Sim result: ", candidate, score)
			continue
		score = compare_sent(context + " " + _type, abst, model)
		print("Sim result: ", candidate, score)
		if score > best: 
			best = score
			print(abst)
			print("######################################Current winner", labels[candidate], candidate) 
'''