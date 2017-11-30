package nl.vu.wdps1706.tools;

import nl.vu.wdps1706.SparkPi;
import nl.vu.wdps1706.warc.WarcParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

/**
 * @author lngtr
 * @since 2017-11-30
 */
public class WebPageExtractor {

    public static void main(final String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: [input path] [identifier-name] [identifier]");
            System.exit(-1);
        }

        SparkSession spark = SparkSession
                .builder()
                .appName(SparkPi.class.getSimpleName())
                .getOrCreate();


        Configuration conf = new Configuration();
        conf.set("textinputformat.record.delimiter", "WARC/1.0");
        JavaRDD<String> rdd = spark.sparkContext()
                .newAPIHadoopFile(args[0], TextInputFormat.class, LongWritable.class, Text.class, conf)
                .toJavaRDD()
                .map(new Function<Tuple2<LongWritable, Text>, String>() {
                    @Override
                    public String call(Tuple2<LongWritable, Text> v1) throws Exception {
                        return new String(v1._2.copyBytes());
                    }
                });

        String html = rdd.filter(
                new Function<String, Boolean>() {
                    @Override
                    public Boolean call(String v1) throws Exception {
                        return v1.contains(args[1] + ": " + args[2]);
                    }
                }
        ).first();

        System.out.println(WarcParser.cleanHtml(html.substring(html.toUpperCase().indexOf("<!DOCTYPE HTML"))));
    }
}
