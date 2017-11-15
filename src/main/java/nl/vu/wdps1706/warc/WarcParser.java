package nl.vu.wdps1706.warc;

import de.l3s.boilerpipe.extractors.ArticleExtractor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import scala.Tuple2;

import java.util.Objects;

/**
 * @author lngtr
 * @since 2017-11-15
 */
public class WarcParser {
    public static Dataset<Row> parse(SparkSession session, String path, String recordAttribute) {
        Configuration conf = new Configuration();
        conf.set("textinputformat.record.delimiter", "WARC/1.0");
        JavaRDD<Tuple2<LongWritable, Text>> rdd = session.sparkContext()
                .newAPIHadoopFile(path, TextInputFormat.class, LongWritable.class, Text.class, conf)
                .toJavaRDD();

        JavaRDD<WarcRecord> records =  rdd
                .map(record -> {
                    String payload = new String(record._2.copyBytes());

                    if (payload.isEmpty()) {
                        return null;
                    }

                    String key = null;
                    StringBuilder htmlBuilder = new StringBuilder();
                    boolean readHtml = false;

                    for (String s : payload.split("\\r?\\n")) {
                        if (s.isEmpty()) {
                            continue;
                        }

                        if (readHtml) {
                            htmlBuilder.append(s);
                        } else if (s.startsWith("WARC-Type") && !"response".equals(s.split(": ")[1])) {
                            return null;
                        } else if (s.startsWith(recordAttribute)) {
                            key = s.split(": ")[1];
                        } else if (/*"<?xml version=\"1.0\"?>".equals(s) || */s.toUpperCase().startsWith("<!DOCTYPE HTML")) {
                            htmlBuilder.append(s);
                            readHtml = true;
                        }
                    }

                    if (key != null && htmlBuilder.length() > 0) {
                        String text = ArticleExtractor.INSTANCE.getText(htmlBuilder.toString()).replaceAll("\\r?\\n", " ").trim();

                        if (!text.isEmpty()) {
                            return new WarcRecord(key, text);
                        }
                    }
                    return null;
                })

                .filter(Objects::nonNull);

        return session.sqlContext().createDataFrame(records, WarcRecord.class);
    }

    private static void removeComments(Node node) {
        for (int i = 0; i < node.childNodeSize();) {
            Node child = node.childNode(i);
            if (child.nodeName().equals("#comment"))
                child.remove();
            else {
                removeComments(child);
                i++;
            }
        }
    }

    private static String cleanHtml(String html) {
        Document doc = Jsoup.parse(html, "", Parser.htmlParser());

        doc.select("script,link,style,img").remove();
        doc.select("*").forEach(Node::clearAttributes);

        removeComments(doc);

        doc.select("summary").forEach(summary -> {
            Document summaryDoc = Jsoup.parse(summary.text(), "", Parser.htmlParser());

            summaryDoc.select("script,link,style,img").remove();
            summaryDoc.select("*").forEach(Node::clearAttributes);

            summaryDoc.outputSettings().prettyPrint(false);
            summary.text(summaryDoc.toString());
        });

        doc.outputSettings().indentAmount(0).prettyPrint(false);

        return doc.toString();
    }
}
