package nl.vu.wdps1706;

import com.google.gson.Gson;
import nl.vu.wdps1706.opennlp.NLPProcessor;
import openNLP.ONLP_ResultWrapper;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
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

        JavaRDD<ONLP_ResultWrapper> entities = NLPProcessor.recognizeEntities(texts);

        entities.map(new Function<ONLP_ResultWrapper, String>() {
            @Override
            public String call(ONLP_ResultWrapper result) throws Exception {
                return new Gson().toJson(result);
            }
        }).saveAsTextFile(OUTPUT_PATH);
    }
}
