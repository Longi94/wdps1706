from __future__ import print_function
import sys, json
import word2vec
from flask import Flask, request, jsonify

MODEL_PATH = "/var/scratch/wdps1706/GoogleNews-vectors-negative300.bin"
if len(sys.argv) == 2: 
  MODEL_PATH = sys.argv[1]

print("Loading Model...")
model = word2vec.WordVectors.from_binary(MODEL_PATH, encoding="ISO-8859-1")

app = Flask(__name__)


@app.route('/w2v/')
def get_vector():
  query = request.args.get("query").lower()
  try:
    res = {"vector": list(model[query])}
    status = 200
  except: 
    res = {"vector": []}
    status = 404
  
  return jsonify(res), status

app.run()

  
