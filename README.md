# wdps1706

## Extracting the text
1) The WARC file(s) is loaded into Spark by splitting the file into WARC
records. Since compressed files cannot be partitioned before
decompressing the whole file, the RDD created by Spark is repartitioned.
2) Spark reads through the whole file and HTML code is detected just by
looking for a line starting with `<!DOCTYPE HTML`.
3) With the [Jsoup](https://github.com/jhy/jsoup) Java library useless
tags that definitely contain no useful text (e.g. script, link or svg)
are removed to make processing faster.
4) The [boilerpipe](https://github.com/robbypond/boilerpipe) Java
library is used to extract text from HTML strings. We noticed that
there were cases where there was escaped HTMLcode inside the website.
These were found to be inside `summary` and`figcaption` tags and were
handled when extracting the text.
5) The output is the identifier of the web page and the corresponding
extracted text. Identifiers with empty text are thrown away.

## Pre-processing
This stage is done using the Apache openNLP tools.
Used tools:
- Sentence Detector
- Language Detector
- Tokenizer
- POS Tagger
- Lemmatizer
- Name Finder (NER)

Tools are based on already pre-trained models that are available here: http://opennlp.sourceforge.net/models-1.5/

1) Input text is being divided into sentences by Sentence Detector Tool.
2) The sentences are filtered to try to remove non English words whenever possible. Only sentences recognized as english take into account for further processing.
3) Each senctence is being tokenized by Tokenizer Tool.
4) Tokenized sentence is then passed to the Part Of Speech Tagger.
5) To improve efficiency of NER, tokens are being lemmatized by Lemmatizer Tool (based on POS Tags as well).
6) Lemmatizer in not efficient enough and not all tokens are lemmatized successfully - in that situation we keep original tokens.
7) Lemmas (and tokens) are being passed to Name Finder Tool that provides Name Entity Recogition. As a result we receive a set of recognized entities for each sentence.
8) Each processed sentence is being wrapped with recognized entities and placed in a result list of wrappers that are passed for further processing. The output from this is a json file with the WARC ID for each warc file, and the list of sentences and entities in those sentences for that warc file.

## Getting candidate entities URLs
In this stage we get the knowledge base URLs for possible candidates, and fix them.

1) For each named entity, we query elastic search to get a list of labels, their score, and freebase ID.
2) Then the list of candidate IDs is used to query trident, and the list is sorted based on the amount of 'facts' each candidate has. The top 10 candidates are then chosen and returned.
3) Next the ID for each candidate is stored in a list and this list is returned
4) The list of IDs is used to query trident and get a list of URLs for each freebase ID
5) Following this, the URLs are filtered to remove any unusable URLs and also "fix" any URLs which have a domain prefix that makes them unusable, but would be usable without them. For example: http://www.de.dbpedia.org/page/Barack_Obama would be converted to http://www.dbpedia.org/page/Barack_Obama

## Entity linking
In this stage we attempt to link the named entities found in stage 2 to the right knowledge base ID from the list returned in stage 3. Data was extracted from DBPEDIA and WIKIDATA.

1) DBPEDIA - From dbpedia we extract the following information: i) Title, ii) abstract, iii) entity type. 
2) WIKIDATA - From wikidata we extract the following information: i) Title, ii) Description, iii) entity type, iii)Date of Birth (when applicable), iv) Gender (when applicable), and v) aliases. The aliases, dob and gender (when found) are added to the description, each word separated by a white space.

Four major attributes are compared to make the linking phase, namely: 
  - **Context matching**: We use the links obtained in section 3 to get the abstract of each candidate entity. We then remove the stop-words from this text and the original sentence of the main entity and compare the remaining words using word2vec. This model, which is trained using [Google's own dataset](https://github.com/mmihaltz/word2vec-GoogleNews-vectors), will return a 300 dimension vector for each word. Two vectors, corresponding to two words are than compared with cosine similarity of their vectors (which is a value between 0 and 1). This process happens for every element of the vector and result are accumulated and then divided by the total number of tokens. As a whole, this will yield an approximate probability of similarity between the original sentence and the abstract of the candidate. This value will be associated with the score of that candidate
  - **Type Matching**: If the type of the entity is equal to the type scraped from the links of the candidate, this will also be treated as a +1 score for that candidate.
  - **Query Title Matching**: If the title of the query is the exact same as the title of the link, another +1 will be added to the score. 
  - **Common Keywords**: The number of exact keywords in the original sentence and the abstract will be added and divided by the total number. This value will be normalized because it is less significant. In fact, it acts as a means to emphasize exacts matches in word2vec (which return 1 as probability) even more. 
  
The Combination of these four attributes will main features of each candidates and the candidate with the maximum score, will be returned as the winner.

## The pipeline
The text extraction and the pre-processing part of the pipeline is done
by a single Java Spark application while the rest is done by a Python
Spark application. As a result, the bash script to generate the output
runs two consecutive Spark applications. While the Java application runs
very well on a cluster, due to issues with Python dependencies, the
second stage is ran in a local environment.
