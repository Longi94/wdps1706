package openNLP;

import java.util.ArrayList;
import java.util.List;

public class ONLP_SentenceDetails {

    public String sentence;
    public List<ONLP_EntityDetails> entities;

    public ONLP_SentenceDetails() {
        sentence = null;
        entities = new ArrayList<ONLP_EntityDetails>();
    }
}
