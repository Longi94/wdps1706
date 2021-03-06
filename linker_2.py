from __future__ import print_function
from __future__ import division
import requests
import json
from bs4 import BeautifulSoup
import nltk
from nltk import word_tokenize
from nltk.corpus import stopwords
import word2vec
import numpy
from collections import defaultdict

model = None
# nltk.download('punkt')
nltk.download('stopwords')
stop_words = set(stopwords.words('english'))

ELASTICSEARCH_URL = 'http://10.149.0.127:9200/freebase/label/_search'
TRIDENT_URL = 'http://10.141.0.11:8082/sparql'
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


def get_dbpedia_entity_type(link, try_num, e_type):
    # print("db entity type link")
    # print(link)
    response = requests.get(link.strip())
    if response.status_code == 200:
        html = response.content
        soup = BeautifulSoup(html, 'html.parser')
        div = soup.find_all('div', class_='page-resource-uri')
        if div:
            div = div[0]
            a_tags = div.find_all('a')
            entity_type_link = {}
            entity_name = ""
            if a_tags:
                entity_type_link = a_tags[0]
                entity_name = entity_type_link.get_text()
            else:
                entity_type_link['href'] = ""

            if try_num > 0:
                if entity_name.lower() == e_type.lower():
                    return True
                elif entity_name.lower() != 'class':
                    try_num = try_num - 1
                    if "http" in entity_type_link['href']:
                        return get_dbpedia_entity_type(entity_type_link['href'], try_num, e_type)
                elif entity_name.lower() == 'class':
                    descripition_table = soup.find_all('div', class_='row')
                    for row in descripition_table:
                        subclass_type = row.find('table', class_="description table table-striped").find('a',
                                                                                                         rel="rdfs:subClassOf").get_text().split(
                            ':')[1]
                        if subclass_type.lower() == e_type.lower():
                            return True
                        else:
                            return False
            else:
                return False
    return False


def get_wikidata_entity_type(url, num_try, kb_e_type, orig_e_type):
    if num_try > 0:
        response = requests.get(url)
        if response.status_code == 200:
            html = response.content
            soup = BeautifulSoup(html, "html.parser")
            if kb_e_type != None:
                subclassOf = soup.find("div", id="P279")
                if subclassOf:
                    subclassOf_links = subclassOf.find_all('a')
                    for link in subclassOf_links:
                        if link['title']:
                            if "Property" not in link['title']:
                                link_text = link.get_text().lower()
                                if link_text == "person":
                                    return True
                                elif link_text == "company":
                                    new_url = "https://wikidata.org" + link['href']
                                    num_try = num_try - 1
                                    return get_wikidata_entity_type(new_url, num_try, link_text, orig_e_type)
                                elif link_text == "organization":
                                    return True
                                else:
                                    continue
                    return False
            else:
                instanceOf = soup.find("div", id="P31")
                if instanceOf:
                    instantceOf_links = instanceOf.find_all('a')
                    for link in instantceOf_links:
                        if link['title']:
                            if "Property" not in link['title']:
                                link_text = link.get_text().lower()
                                # type()
                                if link_text == "human":
                                    new_url = "https://wikidata.org" + link['href']
                                    num_try = num_try - 1
                                    return get_wikidata_entity_type(new_url, num_try, link_text, orig_e_type)
                                elif link_text == "person":
                                    return True
                                elif link_text == "business enterprise":
                                    new_url = "https://wikidata.org" + link['href']
                                    num_try = num_try - 1
                                    return get_wikidata_entity_type(new_url, num_try, link_text, orig_e_type)
                                elif link_text == "country" and orig_e_type == "location":
                                    return True
                                elif link_text == "city" or link_text == "village" or link_text == "town" and orig_e_type == "location":
                                    return True
                    return False
    else:
        return False


def get_dbpedia_data_values(soup, query):
    features_properties = {}
    abstract = ""
    if soup.find("span", {"property": "dbo:abstract", "xml:lang": "en"}):
        abstract = soup.find("span", {"property": "dbo:abstract", "xml:lang": "en"}).get_text()
    features_properties['abstract'] = abstract
    title = ""
    if soup.find("h1", id="title"):
        a_tag = soup.find("h1", id="title").find('a')
        if a_tag:
            title = a_tag.get_text()
    if title == query:
        features_properties["title_query_match"] = True
    else:
        features_properties["title_query_match"] = False
    return features_properties


def get_wikidata_values(soup, query):
    values = {}
    title = soup.find('h1', id="firstHeading", lang="en").find('span', class_="wikibase-title-label").get_text()
    # values['title'] = title
    if title.lower() == query:
        values['title_query_match'] = True
    else:
        values['title_query_match'] = False
    gender_div = soup.find('div', id="P21")
    date = ""
    gender = ""
    description = ""

    if gender_div:
        a_tags = gender_div.find_all('a')
        for a_tag in a_tags:
            link_text = a_tag.get_text().lower()
            if link_text == "male" or link_text == "female":
                gender = link_text
                # values['gender'] = link_text
    dob_div = soup.find('div', id="P569")
    if dob_div:
        date = dob_div.find('div', class_="wikibase-snakview-value wikibase-snakview-variation-valuesnak").get_text()
        # values['dob'] = date
    tr_tags = soup.find('tbody', class_="wikibase-entitytermsforlanguagelistview-listview").find_all('tr')
    td_tags = None
    for tr in tr_tags:
        # print(tr['class'])
        for classes in tr['class']:
            if "languageview-en" in classes:
                td_tags = tr.find_all('td')
                break
    aliases = []
    for td_tag in td_tags:
        description_found = td_tag.find('div', class_="wikibase-descriptionview")
        if td_tag['class'][0] == "wikibase-entitytermsforlanguageview-aliases":
            li_tags = td_tag.find_all('li')
            aliases = []
            if li_tags:
                for li_tag in li_tags:
                    aliases.append(li_tag.get_text())
                    # values["aliases"] = aliases

        if description_found:
            description = description_found.find('span').get_text()
            for alias in aliases:
                description = description + " " + alias
            description = description + " " + date
            description = description + " " + gender
    values['abstract'] = description

    return values


def context_abstract_similarity(abstract, orig_sentence):
    abstract_tokens = word_tokenize(abstract.encode('ascii', 'ignore'))
    filtered_abstract = [w for w in abstract_tokens if not w in stop_words]
    orig_sent_tokens = word_tokenize(orig_sentence)
    filtered_context = [w for w in orig_sent_tokens if not w in stop_words]
    features = {}
    exact_token_match = 0
    word_similarity = 0
    for context_token in filtered_context:
        for abstract_token in filtered_abstract:
            if context_token == abstract_token:
                exact_token_match = exact_token_match + 1
            word_similarity += cosine_similarity(context_token, abstract_token, model)
    features['exact_token_matches'] = exact_token_match
    # features['word_similarity'] = word_similarity
    if len(abstract_tokens) != 0 and len(orig_sent_tokens) != 0:
        word_similarity = word_similarity / (len(abstract_tokens) * len(orig_sent_tokens))
    features['sentence_abstract_similarity'] = word_similarity
    return features


def get_features(bindings, orig_sentence, model, e_type, query):
    features = {}
    for f_id in bindings:
        features[f_id] = []
        for binding in bindings[f_id]:
            if "wikidata" in binding and "entity" in binding:
                binding = binding.replace("entity", "wiki")
            response = requests.get(binding)
            other_values = {}
            if response.status_code == 200:
                # print(binding)
                binding_dict = {}
                binding_dict[binding] = []

                html = response.content
                soup = BeautifulSoup(html, 'html.parser')
                if "dbpedia" in binding:
                    # parse dbpedia html
                    entityTypeMatched = get_dbpedia_entity_type(binding, 3, e_type)
                    # print(binding)
                    other_values = get_dbpedia_data_values(soup, query)
                    other_values['type_matched'] = entityTypeMatched
                    other_values.update(context_abstract_similarity(other_values['abstract'], orig_sentence))

                elif "wikidata" in binding:
                    # parse wikidata html
                    entityTypeMatched = get_wikidata_entity_type(binding, 3, None, e_type)
                    other_values = get_wikidata_values(soup, query)
                    other_values['type_matched'] = entityTypeMatched
                    other_values.update(context_abstract_similarity(other_values['abstract'], orig_sentence))

                binding_dict[binding].append(other_values)
                features[f_id].append(binding_dict)
    return features


def get_entity_KbID(features):
    ranked_features = {}
    for feature in features:
        points = 0
        for i in range(0, len(features[feature])):
            # print("---------------")
            # print(features[feature][i].values())
            # print("---------------")
            values = features[feature][i].values()
            for value in values:
                values_dict = value[0]
                if values_dict['title_query_match'] == True:
                    points = points + 1
                if values_dict['type_matched'] == True:
                    points = points + 1
                points = points + values_dict['exact_token_matches']
                points = points + values_dict['sentence_abstract_similarity']
        ranked_features[feature] = points
    return sorted(ranked_features.iteritems(), key=lambda (k, v): (v, k), reverse=True)[:1][0][0]
    # return ranked_features


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
    for i in candidate_list:
        response = requests.post(TRIDENT_URL, data={'print': False, 'query': po_template % candidate_list[i]['id']})
        if response:
            response = response.json()
            n = int(response.get('stats', {}).get('nresults', 0))
            candidate_list[i]['facts'] = n

    return sorted(candidate_list.iteritems(), key=lambda (k, v): (v, k), reverse=True)[:nr]


def get_freebase_ids(sorted_candidate_list):
    unique_ids = set()
    for candidate in sorted_candidate_list:
        unique_ids.add(candidate[1]['id'])
    return unique_ids


def get_trident_bindings(ids_set):
    # bindings = []
    bindings = defaultdict()
    for f_id in ids_set:
        response = requests.post(TRIDENT_URL, data={'print': True, 'query': same_as_template % f_id})
        if response:
            response = response.json()
            bindings[f_id] = []
            for binding in response.get('results', {}).get('bindings', []):
                # print(binding)
                bindings[f_id].append(binding)
    return bindings


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
            # print("bindings values")
            # print(bindings.values()[0])
            url = binding.values()[0]["value"]
            if "dbpedia" in url:
                new_url = "http://" + url[url.find("dbpedia"):]
                fixed_bindings[f_id].add(new_url)
            elif "wikidata" in url:
                fixed_bindings[f_id].add(url)
    return fixed_bindings


def run(query, context, _type, modelPath):
    global model
    if model is None:
        print("Model not loaded. should not happen")
        model = word2vec.WordVectors.from_binary(modelPath, encoding="ISO-8859-1")

    labels = get_candidates(query)

    if len(labels) == 0:
        return None

    print("-------- LABELS --------")
    top_candidates = rank_candidates(labels, 3)
    print("-------- TOP CANDIDATES --------")
    top_candidates_ids = get_freebase_ids(top_candidates)
    print("-------- TOP CANDIDATES IDS --------")
    bindings = get_trident_bindings(top_candidates_ids)
    print("total bindings")
    print(len(bindings))
    print("-------- BINDINGS --------")
    fixed_bindings = fix_binding_urls(bindings)
    print("total bindings")
    print(len(fixed_bindings))
    print("-------- FIXED BINDINGS --------")
    features = get_features(fixed_bindings, context, model, _type, query)
    print(json.dumps(features))
    print("-------- Finished --------")
    kb_id = get_entity_KbID(features)
    print("Last result", kb_id)
    return kb_id


def init_model(modelPath):
    global model
    print("Loading model...")
    model = word2vec.WordVectors.from_binary(modelPath, encoding="ISO-8859-1")
    print("Model loaded.")
