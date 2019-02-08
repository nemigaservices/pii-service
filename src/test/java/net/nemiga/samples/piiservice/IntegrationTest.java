package net.nemiga.samples.piiservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.nemiga.samples.piiservice.data.DataException;
import net.nemiga.samples.piiservice.data.sql.PIISqlDataAccess;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

/*
    !!! Do not forget to start SQL Proxy! ./runCloudSQLProxy.sh if run locally!
 */

public class IntegrationTest {
  public static void main(String[] args) throws IOException {
    // Initialize SQL
    PIISqlDataAccess piiSqlDataAccess=null;
    try {
      piiSqlDataAccess = new PIISqlDataAccess("jdbc:mysql://dbuser:dbuser@127.0.0.1:3306/pii");
    } catch (DataException e) {
      System.err.println("Error connecting to a database: "+e.getMessage());
      System.exit(-1);
    }


    JsonParser parser = new JsonParser();

    HttpClient client = HttpClientBuilder.create().build();

    JsonObject pii = new JsonObject();
    pii.addProperty("name", "Test");
    pii.addProperty("phone", "555-555-5555");
    pii.addProperty("email", "test@test.com");

    // Create PII entry
    HttpPost postRequest = new HttpPost("https://pii-service.appspot.com/pii?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I");
    StringEntity postingString = new StringEntity(pii.toString());
    postRequest.setEntity(postingString);
    postRequest.setHeader("Content-type", "application/json");
    HttpResponse response = client.execute(postRequest);
    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    String responseData = "";
    String line = null;
    while ((line = rd.readLine()) != null) {
      responseData+=line;
    }
    JsonObject responseJson = parser.parse(responseData).getAsJsonObject();
    String retCode = responseJson.getAsJsonPrimitive("code").getAsString();

    if (!retCode.equals("200")){
      System.err.println("POST returned an error "+retCode);
      System.exit(-1);
    }



    long id = Long.parseLong(responseJson.getAsJsonPrimitive("id").getAsString());

    System.out.println("Created customer with ID: "+id);

    try {
      String auditEntries = piiSqlDataAccess.getAuditDataAsStringForTesting("AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I", id );
      String expected = "AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I, "+id+", name, Test\n"+
        "AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I, "+id+", phone, 555-555-5555\n"+
        "AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I, "+id+", email, test@test.com\n";
        if (!auditEntries.equals(expected)){
            System.err.println("Audit entries are incorrect (Create user)!\n Expected:["+expected+"]\nReceived: ["+auditEntries+"]");
            System.exit(-1);
        }
    } catch (SQLException e) {
      System.err.println("Error obtaining audit log records (create user): "+e.getMessage());
      System.exit(-1);
    }

    // Get created PII
    HttpGet getRequest = new HttpGet("https://pii-service.appspot.com/pii/"+id+"?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I");
    response = client.execute(getRequest);
    rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
    responseData = "";
    while ((line = rd.readLine()) != null) {
      responseData+=line;
    }
    if (!responseData.equals("{\"email\":\"test@test.com\",\"name\":\"Test\",\"phone\":\"555-555-5555\"}")){
      System.err.println("GET did not return the right object!");
      System.exit(-1);
    }


    // Update PII
    JsonObject piiUpd = new JsonObject();
    piiUpd.addProperty("phone", "666-666-6666");

    HttpPut putRequest = new HttpPut("https://pii-service.appspot.com/pii/"+id+"?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I");
    StringEntity putString = new StringEntity(piiUpd.toString());
    putRequest.setEntity(putString);
    putRequest.setHeader("Content-type", "application/json");
    response = client.execute(putRequest);
    rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
    responseData = "";
    while ((line = rd.readLine()) != null) {
      responseData+=line;
    }
    responseJson = parser.parse(responseData).getAsJsonObject();
    retCode = responseJson.getAsJsonPrimitive("code").getAsString();

    if (!retCode.equals("200")){
      System.err.println("PUT returned an error "+retCode);
      System.exit(-1);
    }

    try {
      String auditEntries = piiSqlDataAccess.getAuditDataAsStringForTesting("AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I", id );
      String expected = "AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I, "+id+", name, Test\n"+
              "AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I, "+id+", phone, 555-555-5555\n"+
              "AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I, "+id+", email, test@test.com\n"+
              "AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I, "+id+", phone, 666-666-6666\n";
      if (!auditEntries.equals(expected)){
        System.err.println("Audit entries are incorrect (Update user)!\n Expected:["+expected+"]\nReceived: ["+auditEntries+"]");
        System.exit(-1);
      }
    } catch (SQLException e) {
      System.err.println("Error obtaining audit log records (create user): "+e.getMessage());
      System.exit(-1);
    }

    // Get updated PII
    getRequest = new HttpGet("https://pii-service.appspot.com/pii/"+id+"?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I");
    response = client.execute(getRequest);
    rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
    responseData = "";
    while ((line = rd.readLine()) != null) {
      responseData+=line;
    }
    if (!responseData.equals("{\"email\":\"test@test.com\",\"name\":\"Test\",\"phone\":\"666-666-6666\"}")){
      System.err.println("GET did not return the right updated object!");
      System.exit(-1);
    }

    // Delete the object
    HttpDelete deleteRequest = new HttpDelete("https://pii-service.appspot.com/pii/"+id+"?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I");
    response = client.execute(deleteRequest);
    rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
    responseData = "";
    while ((line = rd.readLine()) != null) {
      responseData+=line;
    }
    responseJson = parser.parse(responseData).getAsJsonObject();
    retCode = responseJson.getAsJsonPrimitive("code").getAsString();

    if (!retCode.equals("200")){
      System.err.println("DELETE returned an error "+retCode);
      System.exit(-1);
    }


    getRequest = new HttpGet("https://pii-service.appspot.com/pii/"+id+"?key=AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I");
    response = client.execute(getRequest);
    if (response.getStatusLine().getStatusCode()!=404){
      System.err.println("Object was not deleted!");
      System.exit(-1);
    }

    try {
      String auditEntries = piiSqlDataAccess.getAuditDataAsStringForTesting("AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I", id );
      String expected = "AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I, "+id+", name, Test\n"+
              "AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I, "+id+", phone, 555-555-5555\n"+
              "AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I, "+id+", email, test@test.com\n"+
              "AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I, "+id+", phone, 666-666-6666\n"+
              "AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I, "+id+", all, deleted\n";
      if (!auditEntries.equals(expected)){
        System.err.println("Audit entries are incorrect (Update user)!\n Expected:["+expected+"]\nReceived: ["+auditEntries+"]");
        System.exit(-1);
      }
    } catch (SQLException e) {
      System.err.println("Error obtaining audit log records (create user): "+e.getMessage());
      System.exit(-1);
    }

    try {
      piiSqlDataAccess.deleteTestData("AIzaSyDvl4L2SOY_jtgm6GvLzJM24A9G1tdV72I", id);
    } catch (SQLException e) {
      System.err.println("Error deleting test data: "+e.getMessage());
      System.exit(-1);
    }

    System.out.println("All tests are passing!");
    System.exit(0);
  }
}
