package inovation.lab.hearthepicture;

public class ResultMessage {
    private String labels;
    private String feelings;
    private String text;

    public String getLabels() {
        if (this.labels == null) {
            return "Sorry! No label found.";
        }

        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getFeelings() {
        if (this.feelings == null) {
            return "";
        }

        return feelings;
    }

    public void setFeelings(String feelings) {
        this.feelings = feelings;
    }

    public String getText() {
        if (this.text == null) {
            return "Sorry! No text found.";
        }

        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
