package nl.vu.wdps1706;

import nl.vu.wdps1706.warc.WarcParser;
import org.apache.spark.sql.SparkSession;

/**
 * @author lngtr
 * @since 2017-11-15
 */
public class WarcToText {

    public static final String OUTPUT_PATH = "spark-data/raw-html";

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: [input path] [identifier]");
            System.exit(-1);
        }

        SparkSession spark = SparkSession
                .builder()
                .appName(WarcToText.class.getSimpleName())
                .getOrCreate();

        WarcParser.parse(spark, args[0], args[1]).write().csv(OUTPUT_PATH);
    }
}
