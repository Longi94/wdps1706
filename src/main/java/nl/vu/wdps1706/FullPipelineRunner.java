package nl.vu.wdps1706;

import nl.vu.wdps1706.opennlp.NLPProcessor;
import nl.vu.wdps1706.warc.WarcParser;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * @author lngtr
 * @since 2017-11-27
 */
public class FullPipelineRunner {
    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: [input path] [identifier]");
            System.exit(-1);
        }

        SparkSession spark = SparkSession
                .builder()
                .appName(FullPipelineRunner.class.getSimpleName())
                .getOrCreate();

        Dataset<Row> texts = WarcParser.parse(spark, args[0], args[1]);
        JavaRDD<Object> entities = NLPProcessor.recognizeEntities(texts.toJavaRDD());
    }
}
