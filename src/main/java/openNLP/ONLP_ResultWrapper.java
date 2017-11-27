package openNLP;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ONLP_ResultWrapper implements Serializable {

    @SerializedName("id")
    public String documentId;

    @SerializedName("sents")
    public List<ONLP_SentenceDetails> sentenceWrappers;

    public ONLP_ResultWrapper() {
        documentId = null;
        sentenceWrappers = new ArrayList<ONLP_SentenceDetails>();
    }
}
