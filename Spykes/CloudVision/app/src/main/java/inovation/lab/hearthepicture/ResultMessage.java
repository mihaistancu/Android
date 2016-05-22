package inovation.lab.hearthepicture;

public class ResultMessage {
    private String labels;
    private String feelings;
    private String text;
    private String logo;

    public String getLogo() {
        if (this.logo == null || this.logo == "") {
            return "No logo found!";
        }

        return "Logo: " + RemoveLastComma(logo);
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getLabels() {
        if (this.labels == null || this.labels == "") {
            return "No label found!";
        }

        return "Labels: " + RemoveLastComma(labels);
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getFeelings() {
        if (this.feelings == null || this.feelings == "") {
            return "No feelings found!";
        }

        return "Feelings: " + RemoveLastComma(feelings);
    }

    public void setFeelings(String feelings) {
        this.feelings = feelings;
    }

    public String getText() {
        if (this.text == null || this.text == "") {
            return "No text found!";
        }

        return RemoveLastComma(text);
    }

    public void setText(String text) {
        this.text = text;
    }

    private String RemoveLastComma(String message) {
        message = message.trim();

        if (message.length() > 0 && message.charAt(message.length() - 1) == ',') {
            message = message.substring(0, message.length() - 1);
        }

        return message;
    }
}
