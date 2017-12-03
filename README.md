# wdps1706

## Extracting the text
First the WARC file is parsed, using spark and 'boilerpipe' to split the HTML for different warc IDs and then extract the html, and subsequently the text inside the HTML tags.

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
