package nl.vu.wdps1706;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lngtr
 * @since 2017-11-14
 */
public class SparkPi {

    public static final int NUM_SAMPLES = 10000;

    public static void main(String[] args) {

        SparkSession spark = SparkSession
                .builder()
                .appName(SparkPi.class.getSimpleName())
                .getOrCreate();

        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        List<Integer> l = new ArrayList<>(NUM_SAMPLES);
        for (int i = 0; i < NUM_SAMPLES; i++) {
            l.add(i);
        }

        long count = jsc.parallelize(l).filter(i -> {
            double x = Math.random();
            double y = Math.random();
            return x*x + y*y < 1;
        }).count();

        System.out.println("Pi is roughly " + 4.0 * count / NUM_SAMPLES);
    }
}
