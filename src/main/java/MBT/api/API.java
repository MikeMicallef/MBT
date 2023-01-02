package MBT.api;

import java.net.URL;
import MBT.Alert;
import MBT.ApiResponse;
import com.google.gson.Gson;
import okhttp3.*;
import java.io.IOException;


public class API {
    final OkHttpClient httpClient = new OkHttpClient();
    private int TotAlerts = 0;  // total number of alerts in the system
    private boolean Posted = false;  // flag to indicate whether an alert has been posted
    private boolean AlertsRemoved = false;  // flag to indicate whether alerts have been removed
    private boolean AlertType = false;  // flag to indicate whether the alert has a type
    private boolean AlertName = false;  // flag to indicate whether the alert has a name
    private boolean AlertDetails = false;  // flag to indicate whether the alert has details
    private boolean AlertImg = false;  // flag to indicate whether the alert has an image
    private boolean AlertUrl = false;  // flag to indicate whether the alert has a URL
    private boolean AlertPrice = false;  // flag to indicate whether the alert has a price
    private boolean PostedBy = false;

    //methods to fetch value of Alert atrributes
    int getTotAlerts() {return TotAlerts;}

    boolean GetPosted() {return Posted;}

    boolean GetAlertsRemoved() { return AlertsRemoved;}

    boolean GetAlertType() { return AlertType;}

    boolean GetAlertName() { return AlertName;}

    boolean GetAlertDetails() { return AlertDetails; }

    boolean GetUrl() { return AlertUrl;}

    boolean GetAlertPrice() { return AlertPrice;}

    private boolean loggedIn = false;
    private boolean alertsBeingViewed = false;

    //since user cannot log out, the user is alwys logged in
    boolean isLoggedIn() { return loggedIn;}

    boolean isAlertsBeingViewed() { return alertsBeingViewed;}

    //viewingAlerts
    boolean viewAlerts(String url) {
        if (loggedIn) {
            try {
                //viewing alerts is confirmed by checking if the url of browser matches the url of the alerts page
                URL alertsPage = new URL("https://www.marketalertum.com/Alerts/List");
                URL currentPage = new URL(url);
                return alertsPage.equals(currentPage);
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    //Posting Alert
    // Method for posting an alert to the API
    void PostAlert() throws IOException {
        // Create a new alert object with all required fields
        Alert correctAlert = new Alert(6, "Apple iPhone 14 256GB Starlight", "iPhone 14. With the most impressive dual-camera system on iPhone", "https://www.scanmalta.com/shop/pub/media/catalog/product/cache/51cb816cf3b30ca1f94fc6cfcae49286/1/8/1812998_1.jpg", "https://www.maltapark.com/asset/itemphotos/9516448/9516448_1.jpg?_ts=4", 120900);

        // Convert the alert object to a JSON string
        String json = new Gson().toJson(correctAlert);

        // Set the request body to the JSON string with the correct media type
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

        // Create the POST request to the API
        Request request = new Request.Builder()
                .url("https://api.marketalertum.com/Alert").addHeader("Content-Type", "application/json").post(body).build();

        // Execute the request and handle any errors
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        }

        // Get the response from the API
        ApiResponse getResponse = getRequestFromMarketAlertUm();

        // If the response is successful and the event log type is 0 (upload alert)

        if (getResponse != null && getResponse.eventLogType == 0) {
            AlertsRemoved = false;
            Posted = true;
            TotAlerts = getResponse.systemState.alerts.size();
            Alert finalAlert = getResponse.systemState.alerts.get(TotAlerts - 1);


            // Checking if Alerts are valid to post
            AlertType = finalAlert.alertType >= 1 && finalAlert.alertType <= 6;
            AlertName = !finalAlert.heading.equals("");
            AlertDetails = !finalAlert.description.equals("");
            AlertUrl = !finalAlert.url.equals("");
            AlertImg = !finalAlert.imageURL.equals("");
            PostedBy = !finalAlert.postedBy.equals("");
            AlertPrice = finalAlert.priceInCents > 0;
        } else {
            Posted = false;AlertsRemoved = false;AlertType = false;AlertName = false;AlertDetails = false;AlertUrl = false;AlertImg = false;PostedBy = false;AlertPrice = false;
        }
    }

    // This method sends a DELETE request to the specified URL to delete alerts for a specific user
    public void AlertsDeleted() throws IOException {
        // Build the DELETE request
        Request request = new Request.Builder().url("https://api.marketalertum.com/Alert?userId=548ac34f-7c6d-491f-99ec-5387bf312e84").delete().build();
        // Execute the request and close the response
        try (Response response = httpClient.newCall(request).execute()) {
        }
    }

    // This method sends a DELETE request to delete alerts for a specific user and checks if the operation was successful
    void RemoveAlerts() throws IOException {
        // Send DELETE request to delete alerts
        Request request = new Request.Builder().url("https://api.marketalertum.com/Alert?userId=548ac34f-7c6d-491f-99ec-5387bf312e84").delete().build();
        try (Response response = httpClient.newCall(request).execute()) {
        }

        // Get response from marketalertum API
        ApiResponse getResponse = getRequestFromMarketAlertUm();
        // If the response is not null and the event log type is 1 (success), set AlertsRemoved to true and reset the Posted and TotAlerts variables
        if (getResponse != null && getResponse.eventLogType == 1) {
            AlertsRemoved = true;
            Posted = false;
            TotAlerts = getResponse.systemState.alerts.size();
            // If the response is null or the event log type is not 1, set AlertsRemoved to false
        } else {
            AlertsRemoved = false;
        }
    }

    // This method sends a GET request to the marketalertum API to retrieve events log information for a specific user
    ApiResponse getRequestFromMarketAlertUm() throws IOException {
        // Build the GET request
        Request request = new Request.Builder()
                .url("https://api.marketalertum.com/EventsLog/548ac34f-7c6d-491f-99ec-5387bf312e84").addHeader("Content-Type", "application/json").build();

        // Execute the request and close the response
        try (Response response = httpClient.newCall(request).execute()) {
            // If the request is not successful, throw an exception
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Get the response body as a string
            String jsonString = response.body().string();

            // Convert the JSON string to an array of ApiResponse objects
            ApiResponse[] ApiRespond = new Gson().fromJson(jsonString, ApiResponse[].class);

            // Return the first element in the array
            return ApiRespond[0];
        }
    }
}

