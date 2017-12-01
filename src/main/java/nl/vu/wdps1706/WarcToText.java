package nl.vu.wdps1706;

import nl.vu.wdps1706.warc.WarcParser;
import org.apache.spark.sql.SparkSession;

/**
 * @author lngtr
 * @since 2017-11-15
 */
public class WarcToText {

    private static final String OUTPUT_PATH = "spark-data/raw-text";

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: [input path] [identifier] [number of executors]");
            System.exit(-1);
        }

        SparkSession spark = SparkSession
                .builder()
                .appName(WarcToText.class.getSimpleName())
                .getOrCreate();

        WarcParser.parse(spark, args[0], args[1], Integer.parseInt(args[2])).write().csv(OUTPUT_PATH);
    }
}
