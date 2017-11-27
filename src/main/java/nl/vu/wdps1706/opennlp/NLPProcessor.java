package nl.vu.wdps1706.opennlp;

import openNLP.ONLP_Core;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.Row;

/**
 * @author lngtr
 * @since 2017-11-27
 */
public class NLPProcessor {

    public static JavaRDD<Object> recognizeEntities(JavaRDD<Row> texts) {
        texts.foreach(new VoidFunction<Row>() {
            @Override
            public void call(Row row) throws Exception {
                String id = row.getString(0);
                String text = row.getString(1);

                ONLP_Core.process(text);
            }
        });

        return null;
    }

}
