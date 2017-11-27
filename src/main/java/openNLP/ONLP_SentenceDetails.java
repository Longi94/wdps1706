package openNLP;

import java.util.HashMap;
import java.util.Map;

public class ONLP_SentenceDetails {

    String sentence;
    Map<String,String> entities;

    public ONLP_SentenceDetails() {
        sentence = null;
        entities = new HashMap<String,String>();
    }
}
