from __future__ import print_function
import requests
import sys
import json
import re
import collections
import math
import lxml.html
import word2vec
import numpy

ELASTICSEARCH_URL = 'http://10.149.0.127:9200/freebase/label/_search'
TRIDENT_URL = 'http://10.149.0.124:9001/sparql'
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

def rank_candidates(candidate_list, nr):
  facts  = {}
  def get_best(i):
    return math.log(facts[i]) * scores[i]
  for i in candidate_list:
    response = requests.post(TRIDENT_URL, data={'print': False, 'query': po_template % i})
    if response:
        response = response.json()
        n = int(response.get('stats',{}).get('nresults',0))
        print("Found " + str(n) + " facts about " + str(i) + " [" + candidate_list[i] + "] with Sparql")
        candidate_list[i]['facts'] = n
  return sorted(candidate_list, key=lambda x: x['facts'], reverse=True)[:nr] 


def get_abstract(query, nr_sent=1):
  response = requests.get("http://dbpedia.org/page/%s" % query.replace(" " , "_"))
  if response: 
    html = response.content

    parsed = lxml.html.fromstring(html).cssselect(
        "body > div.container > div.row > div:nth-child(1) > p")
    return parsed[0].text

def get_google(query_id): 
  response = requests.get("https://www.google.com/search?q=knowledge+graph+search+api&kponly&kgmid=/" + query_id)
  if response: 
    html = response.content
    parsed = lxml.fromstring(html).cssselect("_gdf kno-fb-ctx")
    parsed_lines = lxml.fromstring(html).cssselect("")

  

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
