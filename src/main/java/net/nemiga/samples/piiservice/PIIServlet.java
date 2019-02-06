/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.nemiga.samples.piiservice;

import com.google.gson.*;
import net.nemiga.samples.piiservice.validators.RequestException;
import net.nemiga.samples.piiservice.validators.RequestValidator;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** A servlet that echoes JSON message bodies. */
@WebServlet("/pii/*")
public class PIIServlet extends HttpServlet {

  public static final String CONTENT_ENCODING = "Content-Encoding";
  public static final String APPLICATION_JSON = "application/json";
  private final RequestValidator validator=new RequestValidator();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader(CONTENT_ENCODING, APPLICATION_JSON);
    String key = req.getParameter("key");

    System.out.println("Received POST request with the key: " + key);
    Object responseBody;
    try {
      JsonObject data = this.validator.getJsonPayload(req);

      int id = 1000;

      System.out.println("Crteated user with the ID: "+id);
      responseBody = this.generateResponse(id, HttpServletResponse.SC_OK, "PII Object created.");

    } catch (RequestException re) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      responseBody = generateResponse(-1, HttpServletResponse.SC_BAD_REQUEST, re.getMessage());
    }

    new Gson().toJson(responseBody, resp.getWriter());
  }

  @Override
  public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader(CONTENT_ENCODING, APPLICATION_JSON);
    String key = req.getParameter("key");
    System.out.println("Received DELETE request with the key: " + key);
    Object responseBody;
    try {
      int id = this.validator.getIdForGetDeletePut(req);

      System.out.println("Deleted user with the ID: "+id);
      responseBody = this.generateResponse(id, HttpServletResponse.SC_OK, "PII Object deleted.");

    } catch (RequestException re) {
      System.err.println("Request error - ID not found. Error: " + re.getMessage());
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      responseBody = this.generateResponse(-1, HttpServletResponse.SC_BAD_REQUEST, re.getMessage());
    }

    new Gson().toJson(responseBody, resp.getWriter());

  }

  @Override
  public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader(CONTENT_ENCODING, APPLICATION_JSON);
    String key = req.getParameter("key");

    System.out.println("Received PUT request with the key: " + key);
    Object responseBody;
    try {
      int id = this.validator.getIdForGetDeletePut(req);

      System.out.println("Updated user with the ID: "+id);
      responseBody = this.generateResponse(id, HttpServletResponse.SC_OK, "PII Object updated.");

    } catch (RequestException re) {
      System.err.println("Request error: either ID not found or pahload is not JSON. Error: " + re.getMessage());
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      responseBody = this.generateResponse(-1, HttpServletResponse.SC_BAD_REQUEST, re.getMessage());
    }

    new Gson().toJson(responseBody, resp.getWriter());
  }



  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader(CONTENT_ENCODING, APPLICATION_JSON);
    String key = req.getParameter("key");

    System.out.println("Received GET request with the key: " + key);
    Object responseBody;
    try {
      int id = this.validator.getIdForGetDeletePut(req);

      String fields = req.getParameter("data");


      JsonObject data = new JsonObject();
      data.addProperty("key", key);
      if (fields == null)
        data.addProperty("name", "Joe Test");
      data.addProperty("phone", "555-555-5555");
      data.addProperty("email","test@test.org");
      System.out.println("User found, sending the reply.");
      responseBody = data;

    } catch (RequestException re) {
      System.err.println("Invalid json. Error: " + re.getMessage());
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      responseBody = this.generateResponse(-1, HttpServletResponse.SC_BAD_REQUEST, re.getMessage());
    }

    new Gson().toJson(responseBody, resp.getWriter());

  }

  private JsonObject generateResponse(int id, int responseCode, String message){
    JsonObject responseObject = new JsonObject();
    responseObject.addProperty("code", responseCode);
    responseObject.addProperty("message",message);
    responseObject.addProperty("id", id);
    return responseObject;
  }

}
