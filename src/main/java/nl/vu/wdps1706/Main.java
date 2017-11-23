package nl.vu.wdps1706;


import openNLP.ONLP_Core;
import openNLP.ONLP_POSTagger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author lngtr
 * @since 2017-11-14
 */

public class Main {


    public static void main(String[] args) {
      //try{
        //SparkConf sparkConf = new SparkConf().setAppName("TestApp");
        //JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
        //SQLContext sqlContext = new SQLContext(sparkContext);

      //String modelFile = Main.class.getClassLoader().getResource("ModelsDir/en-pos-maxent.bin").getFile();
      //URL modelFile = ClassLoader.getSystemResource("en-pos-maxent.bin");

      String path = "C:\\Users\\Daniel\\Desktop\\Files\\School and work\\Master's\\1st year\\Period 2\\Web Data Processing Systems\\Assignment\\raw data\\tmp.txt";
      try {
        String contents = new String(Files.readAllBytes(Paths.get(path)));
        ONLP_Core.process(contents);
      }catch(IOException e){
        e.printStackTrace();
      }


    }
}
