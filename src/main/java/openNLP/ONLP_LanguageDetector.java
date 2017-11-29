package openNLP;

import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.Language;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ONLP_LanguageDetector {

    private static LanguageDetector languageDetector = null;

    private static void createInstance() {
        InputStream modelIn = null;
        try {
            URL resourceURL = ONLP_POSTagger.class.getClassLoader().getResource("langdetect-183.bin");
            if(resourceURL != null) {
                modelIn = new FileInputStream(resourceURL.getFile());
                LanguageDetectorModel model = new LanguageDetectorModel(modelIn);
                languageDetector = new LanguageDetectorME(model);
            }
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

    public static String getLanguage(String input) {
        if(languageDetector == null) {
            createInstance();
        }
        Language langResult = languageDetector.predictLanguage(input);
        String result = langResult.getLang();
        return result;
    }

}
