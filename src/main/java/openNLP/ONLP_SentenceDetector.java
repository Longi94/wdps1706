package openNLP;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.IOException;
import java.io.InputStream;

public class ONLP_SentenceDetector {

    private static SentenceDetectorME sentenceDetector = null;

    private static void createInstance() {
        InputStream modelIn = null;
        try {
            modelIn = ONLP_SentenceDetector.class.getResourceAsStream("/en-sent.bin");
            SentenceModel model = new SentenceModel(modelIn);
            sentenceDetector = new SentenceDetectorME(model);
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static String[] detectSentences(String input) {
        if(sentenceDetector == null) {
            createInstance();
        }
        String[] result = sentenceDetector.sentDetect(input);
        return result;
    }

}
