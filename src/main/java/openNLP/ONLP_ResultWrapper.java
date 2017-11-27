package openNLP;

import java.util.ArrayList;
import java.util.List;

public class ONLP_ResultWrapper {

    String documentId;
    List<ONLP_SentenceDetails> sentenceWrappers;

    public ONLP_ResultWrapper() {
        documentId = null;
        sentenceWrappers = new ArrayList<ONLP_SentenceDetails>();
    }
}
