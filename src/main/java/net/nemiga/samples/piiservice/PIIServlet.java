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

  private RequestValidator validator=new RequestValidator();

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader("Content-Encoding", "application/json");
    String key = req.getParameter("key");

    System.out.println("Received POST request with the key: " + key);
    Object responseBody;
    try {
      JsonObject data = this.validator.getJsonPayload(req);

      System.out.println("Valid json, sending the reply.");
      responseBody = this.generateResponse(1000, HttpServletResponse.SC_OK, "PII Object created.");

    } catch (RequestException re) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      responseBody = generateResponse(-1, HttpServletResponse.SC_BAD_REQUEST, re.getMessage());
    }

    new Gson().toJson(responseBody, resp.getWriter());
  }

  @Override
  public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {

  }

  @Override
  public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {

  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader("Content-Encoding", "application/json");
    String key = req.getParameter("key");

    System.out.println("Received GET request with the key: " + key);
    Object responseBody;
    try {
      String id = this.validator.getIdForGetDeletePut(req);

      JsonObject data = new JsonObject();
      data.addProperty("key", key);
      data.addProperty("name", "Joe Test");
      data.addProperty("phone", "555-555-5555");
      data.addProperty("email","test@test.org");
      System.out.println("Valid json, sending the reply.");
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
