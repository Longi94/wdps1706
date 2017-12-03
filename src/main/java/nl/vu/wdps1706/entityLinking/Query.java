package nl.vu.wdps1706.entityLinking;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mortbay.util.ajax.JSON;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by Daniel on 24/11/2017.
 */

public class Query {

  private  String elastic_search_url = "http://10.149.0.127:9200/freebase/label/_search";
  private String trident_url = "http://10.141.0.11:8082/sparql";


  private String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "PREFIX fbase: <http://rdf.freebase.com/ns/>";

  String same_as_template = prefixes + "SELECT DISTINCT ?same WHERE {\n" +
                                        "    ?s owl:sameAs %s .\n" +
                                        "    { ?s owl:sameAs ?same .} UNION { ?same owl:sameAs ?s .}\n" +
                                        "}";

  String po_template = prefixes + "SELECT DISTINCT * WHERE {\n" +
                                  "    %s ?p ?o.\n" +
                                  "}";


  private String makeRequest(String url, String urlParamater){
    try {
      URL urlObj = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

      connection.setRequestMethod("POST");
      connection.setDoOutput(true);

       //urlParamater = "q="+query+"&size=100";

      DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
      dos.writeBytes(urlParamater);
      dos.flush();
      dos.close();

      int responseCode = connection.getResponseCode();

      if(responseCode == 200){

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder responseData = new StringBuilder();
        while((line = in.readLine()) != null){
          responseData.append(line);
        }
        in.close();
        return responseData.toString();
      }else{
        System.err.println("ERROR: request returned the following response code: " + responseCode);
      }

    }catch(IOException e){
      e.printStackTrace();
    }
    return null;
  }

  public String runQuery(String query){
     return makeRequest(elastic_search_url,"q="+query+"&size=100");
  }

  public HitsData getHitsData(String queryResult){
    JSONObject jobjectResult = new JSONObject(queryResult);
    JSONArray hits = jobjectResult.getJSONArray("hits");

    Set<String> idsSet = new HashSet<>();
    HashMap<String, Integer> scores = new HashMap<>();
    HashMap<String, HashSet<String>> labels = new HashMap<>();

    String freeBaseId = "", label = "";
    int score, maxForId;

    for(int i = 0; i < hits.length(); i++){
      //HitsData rd = new HitsData();
      JSONObject hit = hits.getJSONObject(i);
      JSONObject source = hit.getJSONObject("_source");
      freeBaseId = source.getString("resource");
      label = source.getString("label");

      if(hit.has("_score")){
        score = hit.getInt("_score");
      }else{
        score = 0;
      }

      idsSet.add(freeBaseId);
      scores.put(freeBaseId,Math.max(scores.getOrDefault(freeBaseId,0),score));
      labels.getOrDefault(freeBaseId, new HashSet<>()).add(label);

    }
    return new HitsData(idsSet,scores,labels);
  }

  public HashMap<String, Integer> countFacts(Set<String> ids){
    HashMap<String, Integer> facts = new HashMap<>();
      int n = 0;
      for(String id : ids){
       String response =  makeRequest(trident_url, String.format(po_template,id));
       if(response!=null){
        JSONObject tridentResult = new JSONObject(response);
        if(tridentResult.has("stats")){
          JSONObject stats = tridentResult.getJSONObject("stats");
          if(stats.has("nresults")){
            n = stats.getInt("nresults");
          }
        }
        facts.put(id,n);
       }
      }

      return facts;
  }
/*
  private double get_best(int fact, int score){
    return Math.log(fact) * score;
  }


  public void getBestMatches(HitsData hitsData){
    List<String> list = new ArrayList(hitsData.getIdsSet());
    Collections.sort(list, Collections.reverseOrder());
    Set<String> resultSet = new LinkedHashSet(list);

      for(String id : resultSet){
        String response = makeRequest(trident_url, "print=True&query="+String.format(same_as_template,id));
        if(response != null){
          JSONObject tridentResult = new JSONObject(response);
          if(tridentResult.has("results")){
            JSONObject results = tridentResult.getJSONObject("results");
            if(results.has("bindings")){
              JSONArray bindings = results.getJSONArray("bindings");
              for(int i = 0; i < bindings.length(); i++){

              }
            }
          }
        }
      }
  }
*/
}
