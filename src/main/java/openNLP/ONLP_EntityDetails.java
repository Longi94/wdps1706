package openNLP;

public class ONLP_EntityDetails {
    public String name;
    public String type;
    public double prob;

    public ONLP_EntityDetails(String name, String type, double prob) {
        this.name = name;
        this.type = type;
        this.prob = prob;
    }
}
