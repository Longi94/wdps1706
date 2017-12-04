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
This stage is done using the Apache openNLP tool.

1) The sentences are filtered to try to remove non English words whenever possible, following this tokenization takes place.
2) POS tagging is then done on the tokenized sentences
3) Finally, NER is done using the tool itself. The output from this is a json file with the WARC ID for each warc file, and the list of sentences and entities in those sentences for that warc file.

## Getting candidate entities URLs
In this stage we get the knowledge base URLs for possible candidates, and fix them.

1) For each named entity, we query elastic search to get a list of labels, their score, and freebase ID.
2) Then the list of candidate IDs is used to query trident, and the list is sorted based on the amount of 'facts' each candidate has. The top 10 candidates are then chosen and returned.
3) Next the ID for each candidate is stored in a list and this list is returned
4) The list of IDs is used to query trident and get a list of URLs for each freebase ID
5) Following this, the URLs are filtered to remove any unusable URLs and also "fix" any URLs which have a domain prefix that makes them unusable, but would be usable without them. For example: http://www.de.dbpedia.org/page/Barack_Obama would be converted to http://www.dbpedia.org/page/Barack_Obama

## Entity linking
In this stage we attempt to link the named entities found in stage 2 to the right knowledge base ID from the list returned in stage 3.

1)

## The pipeline
The text extraction and the pre-processing part of the pipeline is done
by a single Java Spark application while the rest is done by a Python
Spark application. As a result, the bash script to generate the output
runs two consecutive Spark applications. While the Java application runs
very well on a cluster, due to issues with Python dependencies, the
second stage is ran in a local environment.