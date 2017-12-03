package openNLP;

import java.io.Serializable;

public class ONLP_EntityDetails implements Serializable {
    public String name;
    public String type;
    public double prob;

    public ONLP_EntityDetails(String name, String type, double prob) {
        this.name = name.trim();
        this.type = type;
        this.prob = prob;
    }
}
