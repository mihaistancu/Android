package inovation.lab.hearthepicture;

public class ResultMessage {
    private String labels;
    private String feelings;
    private String text;
    private String logo;
    private String landmarks;

    private String getLandmarks() {
        if (this.landmarks == null || this.landmarks == "") {
            return "";
        }

        return RemoveLastComma(landmarks);
    }

    public void setLandmarks(String landmarks) {
        this.landmarks = landmarks;
    }

    private String getLogo() {
        if (this.logo == null || this.logo == "") {
            return "";
        }

        return RemoveLastComma(logo);
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    private String getLabels() {
        if (this.labels == null || this.labels == "") {
            return "";
        }

        return RemoveLastComma(labels);
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    private String getFeelings() {
        if (this.feelings == null || this.feelings == "") {
            return "";
        }

        return RemoveLastComma(feelings);
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

    public String GetMessageWithoutText() {
        String message = "";
        String feelings = this.getFeelings();
        String logos = this.getLogo();
        String landmarks = this.getLandmarks();
        String labels = this.getLabels();

        if (landmarks != "") {
            message = landmarks + ".";

            if (labels != "") {
                message += "\n\nLabels: " + labels.replace("landmark,", "") + ".";
            }
        } else if (labels != "") {
            message = labels + ".";
        }

        if (feelings != "") {
            message += "\n\nFeelings: " + feelings + ".";
        }

        if (logos != "") {
            message += "\n\nLogo: " + logos + ".";
        }

        if(message == ""){
            message = "Sorry! Nothing relevant found! Please try again!";
        }

        return message;
    }
}
