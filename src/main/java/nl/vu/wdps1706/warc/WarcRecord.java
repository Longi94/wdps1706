package nl.vu.wdps1706.warc;

public class WarcRecord {
    private String id;

    private String textHtml;

    public WarcRecord(String id, String textHtml) {
        this.id = id;
        this.textHtml = textHtml;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTextHtml() {
        return textHtml;
    }

    public void setTextHtml(String textHtml) {
        this.textHtml = textHtml;
    }
}