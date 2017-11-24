package nl.vu.wdps1706.entityLinking;

import com.google.gson.Gson;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Daniel on 24/11/2017.
 */
public class Query {
  String elastic_search_url = "http://10.149.0.127:9200/freebase/label/_search";
  String kb_url = "http://10.141.0.11:8082/sparql";
  Gson gson = new Gson();

  public String runQuery(String query){
    try {
      URL urlObj = new URL(elastic_search_url);
      HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

      connection.setRequestMethod("POST");
      connection.setDoOutput(true);

      String urlParamater = "q="+query+"&size=100";

      DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
      dos.writeBytes(urlParamater);
      dos.flush();
      dos.close();

      int responseCode = connection.getResponseCode();

      if(responseCode == 200){

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuffer responseData = new StringBuffer();
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
      return "";
    }
    return null;
  }

}
