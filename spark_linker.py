from linker_2 import init_model, run
from json import loads
from pyspark.sql import SparkSession

INPUT_PATH = 'spark-data/entities'
OUTPUT_PATH = 'spark-data/links'
MODEL_PATH = '/var/scratch/wdps1706/GoogleNews-vectors-negative300.bin'


def process_sentence(sentence):
    links = []

    for entity in sentence['ents']:
        kb_id = run(entity['name'], sentence['text'], entity['type'], MODEL_PATH)

        if kb_id is not None:
            links.append({'name': entity['name'], 'entity_id': kb_id})

    return links


def process_sentences(record):
    links = map(process_sentence, record['sents'])
    links = [item for sublist in links for item in sublist]  # flatten the list
    for link in links:
        link['id'] = record['id']
    return links


if __name__ == "__main__":
    init_model(MODEL_PATH)

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
