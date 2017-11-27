package openNLP;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ONLP_SentenceDetails implements Serializable {

    @SerializedName("text")
    public String sentence;

    @SerializedName("ents")
    public List<ONLP_EntityDetails> entities;

    public ONLP_SentenceDetails() {
        sentence = null;
        entities = new ArrayList<ONLP_EntityDetails>();
    }
}
