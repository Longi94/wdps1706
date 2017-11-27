package nl.vu.wdps1706.opennlp;

import openNLP.ONLP_Core;
import openNLP.ONLP_ResultWrapper;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;

/**
 * @author lngtr
 * @since 2017-11-27
 */
public class NLPProcessor {

    public static JavaRDD<ONLP_ResultWrapper> recognizeEntities(JavaRDD<Row> texts) {
        return texts.map(new Function<Row, ONLP_ResultWrapper>() {
            @Override
            public ONLP_ResultWrapper call(Row row) throws Exception {
                String id = row.getString(0);
                String text = row.getString(1);

                ONLP_ResultWrapper result = ONLP_Core.process(text);
                result.documentId = id;

                return result;
            }
        }).filter(new Function<ONLP_ResultWrapper, Boolean>() {
            @Override
            public Boolean call(ONLP_ResultWrapper result) throws Exception {
                return result.sentenceWrappers.size() > 0;
            }
        });
    }

}
