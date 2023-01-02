package MBT.api;

import junit.framework.Assert;
import nz.ac.waikato.modeljunit.*;
import nz.ac.waikato.modeljunit.coverage.ActionCoverage;
import nz.ac.waikato.modeljunit.coverage.StateCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionPairCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;
import MBT.ApiResponse;
import MBT.api.enums.NextModelStates;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

public class ApiTest implements FsmModel {
    // This class represents the model for the marketalertum system
    private NextModelStates modelState;
    // Number of alerts
    private int TotAlerts;
    // Variables for storing the state of the model
    private boolean Uploaded, AlertsDeleted, AlertType, AlertName, AlertDetails, AlertImg, url, PostedBy, AlertPrice;
    // Flags for whether the user is logged in or viewing alerts
    private boolean LoggedIn = false;
    private boolean ViewingAlerts = false;

    // Defining the System Under Test (API)
    private API sut;

    // Method for getting the current state of the model
    public NextModelStates getState() { return modelState; }

    // Method for resetting the model
    public void reset(final boolean yes){
        // Set the model state to RESTART_API
        modelState = NextModelStates.RESTART_API;
        // Reset the number of alerts to 0
        TotAlerts = 0;
        // Reset the state variables
        Uploaded = false; AlertsDeleted = false; AlertType = false; AlertName = false; AlertDetails = false; url = false; AlertImg = false; PostedBy = false; AlertPrice = false;
        // If the yes flag is set, delete all alerts and create a new instance of the API
        if(yes) {
            API dummy = new API();
            try { dummy.AlertsDeleted();
            } catch (IOException e) { throw new RuntimeException(e);}
            try { ApiResponse dummyResponse = dummy.getRequestFromMarketAlertUm();
            } catch (IOException e) { throw new RuntimeException(e);
            }
            sut = new API();
        }
    }

    // Method for checking if the uploadAlert action is allowed in the current state
    public boolean uploadAlertGuard() {
        return getState().equals(NextModelStates.RESTART_API) ||getState().equals(NextModelStates.LOGGED_IN)|| getState().equals(NextModelStates.ALERTS_UPLOADED) || getState().equals(NextModelStates.ALERTS_DELETED);
    }

    // Method for uploading an alert
    @Action
    public void uploadAlert() throws IOException {
        // Call the API's PostAlert method
        sut.PostAlert();

        // Wait for 500 milliseconds
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // If the number of alerts is less than 5, set the state variables to indicate a successful upload
        if (TotAlerts < 5){
            TotAlerts++;
            Uploaded = true; AlertsDeleted = false; AlertType = true; AlertName = true; AlertDetails = true; url = true; AlertImg = true; PostedBy = true; AlertPrice = true;
        }// If the number of alerts is 5 or more, set the state variables to indicate a failed upload
        else {
            Uploaded = false; AlertsDeleted = false; AlertType = false; AlertName = false; AlertDetails = false; url = false; AlertImg = false; PostedBy = false; AlertPrice = false;
        }


        // Set the model state to ALERTS_UPLOADED
        modelState = NextModelStates.ALERTS_UPLOADED;

        // Compare the state variables of the model to the values returned by the API
        Assert.assertEquals("Model's Upload State != SUT State ", Uploaded, sut.GetPosted());
        Assert.assertEquals("Model's No. Of Alerts != SUT Number Of Alerts", TotAlerts, sut.getTotAlerts());
        Assert.assertEquals("Model's Alerts Removed != SUT Alerts Removed", AlertsDeleted, sut.GetAlertsRemoved());
        Assert.assertEquals("Model's Alert Type != SUT Alert Type", AlertType, sut.GetAlertType());
        Assert.assertEquals("Model's Alert Name != SUT Alert Name", AlertName, sut.GetAlertName());
        Assert.assertEquals("Model's Alert Details != SUT Alert Details", AlertDetails, sut.GetAlertDetails());
        Assert.assertEquals("Model's Alert URL != SUT Alert URL", url, sut.GetUrl());
        Assert.assertEquals("Model's Alert Price != SUT Alert Price", AlertPrice, sut.GetAlertPrice());
    }


    // Method for checking if the RemoveAlerts action is allowed in the current state
    public boolean RemoveAlertsGuard() {
        return getState().equals(NextModelStates.RESTART_API) || getState().equals(NextModelStates.ALERTS_UPLOADED) || getState().equals(NextModelStates.ALERTS_DELETED);
    }

    // Method for removing alerts
    @Action
    public void RemoveAlerts() throws IOException {
        // Call the API's RemoveAlerts method
        sut.RemoveAlerts();

        // Set the state variables to indicate that alerts have been deleted
        Uploaded = false;
        AlertsDeleted = true;
        TotAlerts = 0;

        // Set the model state to ALERTS_DELETED
        modelState = NextModelStates.ALERTS_DELETED;

        // Compare the state variables of the model to the values returned by the API
        Assert.assertEquals("Model's No. Of Alerts != SUT Number Of Alerts", TotAlerts, sut.getTotAlerts());
        Assert.assertEquals("Model's Alerts Removed != SUT Alerts Removed", AlertsDeleted, sut.GetAlertsRemoved());
    }

    // Method for viewing alerts
    @Action
    public void viewAlerts() {
        // URL for the alerts page
        String currentUrl = "https://www.marketalertum.com/Alerts/List";

        ViewingAlerts = sut.viewAlerts(currentUrl);;

        modelState = NextModelStates.VIEWING_ALERTS;
        Assert.assertEquals("Model's Upload State != SUT State", LoggedIn, sut.isLoggedIn());
        Assert.assertEquals("Model's Viewing State != SUT State", ViewingAlerts, sut.isAlertsBeingViewed());
    }

    // Method for checking if the viewAlerts action is allowed in the current state
    public boolean viewAlertsGuard() {
         return getState().equals(NextModelStates.RESTART_API) || getState().equals(NextModelStates.ALERTS_UPLOADED) || getState().equals(NextModelStates.ALERTS_DELETED);
    }

    // Test method that runs the test case
    @Test
    public void TestRunner() throws IOException {
        // Create a RandomTester object and set the random seed
        final Tester tester = new RandomTester(new ApiTest());
        tester.setRandom(new Random());

        // Build the graph of states and transitions
        tester.buildGraph();

        // Add a verbose listener to output the test progress
        tester.addListener("verbose");

        // Add coverage metrics to measure the test coverage
        tester.addCoverageMetric(new TransitionPairCoverage());
        tester.addCoverageMetric(new StateCoverage());
        tester.addCoverageMetric(new ActionCoverage());
        tester.addCoverageMetric(new TransitionCoverage());

        // Generate and run the test case
        tester.generate(120);

        // Print the coverage results
        tester.printCoverage();
    }
}
