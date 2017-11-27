package nl.vu.wdps1706;

import nl.vu.wdps1706.opennlp.NLPProcessor;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * @author lngtr
 * @since 2017-11-27
 */
public class TextToEntities {

    private static final String INPUT_PATH = "spark-data/raw-text";
    private static final String OUTPUT_PATH = "spark-data/entities";

    public static void main(String[] args) {
        SparkSession spark = SparkSession
                .builder()
                .appName(TextToEntities.class.getSimpleName())
                .getOrCreate();

        JavaRDD<Row> texts = spark.read().csv(INPUT_PATH).toJavaRDD();

        JavaRDD<Object> entities = NLPProcessor.recognizeEntities(texts);

        spark.createDataFrame(entities, Object.class).write().json(OUTPUT_PATH);
    }
}
