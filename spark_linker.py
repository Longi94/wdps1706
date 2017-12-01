from json import loads
from pyspark.sql import SparkSession

INPUT_PATH = 'spark-data/entities'
OUTPUT_PATH = 'spark-data/links'


def process_sentence(sentence):
    links = []

    # TODO do the stuff here the sentence dict has an ents list, each element has a name, type and prob property
    # TODO must return a list of {name, entity_id} dicts
    for entity in sentence['ents']:
        links.append({'name': entity['name'], 'entity_id': '/m/asdf' + entity['type']})

    return links


def process_sentences(record):
    links = map(process_sentence, record['sents'])
    links = [item for sublist in links for item in sublist]  # flatten the list
    for link in links:
        link['id'] = record['id']
    return links


if __name__ == "__main__":
    spark = SparkSession \
        .builder \
        .appName("SparkLinker") \
        .getOrCreate()

    lines = spark.read.text(INPUT_PATH).rdd.map(lambda r: r[0])

    lines.map(lambda line: process_sentences(loads(line))) \
        .flatMap(lambda x: x) \
        .map(lambda link: link['id'] + '\t' + link['name'] + '\t' + link['entity_id']) \
        .saveAsTextFile(OUTPUT_PATH)

    spark.stop()
