package MBT;

public class Alert {
    public String postedBy = "548ac34f-7c6d-491f-99ec-5387bf312e84";
   //Alert Attributes
    public int alertType;public String heading;public String description;public String url;public String imageURL;public int priceInCents;

    // Constructor for creating an Alert object
    public Alert(int alertType, String AlertName, String AlertDetails,String AlertImg, String AlertUrl, int AlertPrice) {
        this.heading = AlertName;
        this.description = AlertDetails;
        this.alertType = alertType;
        this.imageURL = AlertImg;
        this.url = AlertUrl;
        this.priceInCents = AlertPrice;
    }


}