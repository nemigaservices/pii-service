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
import net.nemiga.samples.piiservice.data.DataException;
import net.nemiga.samples.piiservice.data.piistorage.PIIStorage;
import net.nemiga.samples.piiservice.data.sql.PIISqlDataAccess;
import net.nemiga.samples.piiservice.processors.DataProcessorException;
import net.nemiga.samples.piiservice.processors.ReturnedDataProcessor;
import net.nemiga.samples.piiservice.validators.RequestException;
import net.nemiga.samples.piiservice.validators.RequestValidator;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

/** A servlet that echoes JSON message bodies. */
@WebServlet("/pii/*")
public class PIIServlet extends HttpServlet {

  private static final String CONTENT_ENCODING = "Content-Encoding";
  private static final String APPLICATION_JSON = "application/json";
  private final RequestValidator validator=new RequestValidator();

  private final PIIStorage piiStorage = new PIIStorage();
  private final ReturnedDataProcessor returnedDataProcessor = new ReturnedDataProcessor();
  private PIISqlDataAccess piiSqlDataAccess = null;

  @Override
  public void init() {
      Properties properties = new Properties();
      try {
        properties.load(getServletContext().getResourceAsStream("/WEB-INF/classes/config.properties"));
        String url = properties.getProperty("sqlUrl");
        System.out.println("Initializing SQL module with the URL: "+url);
        this.piiSqlDataAccess = new PIISqlDataAccess(url);
      } catch (IOException e) {
        System.err.println("Cannot load URL property: "+e.getMessage());  // Servlet Init should never fail.
      } catch (DataException e) {
        System.err.println("Cannot initialize the SQL module: "+e.getMessage());  // Servlet Init should never fail.
      }
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader(CONTENT_ENCODING, APPLICATION_JSON);
    String key = req.getParameter("key");

    System.out.println("Received POST request with the key: " + key);
    Object responseBody;
    if (piiSqlDataAccess != null) {
      try {
        if (piiSqlDataAccess.isMethodAllowed(key, PIISqlDataAccess.METHOD.POST)){
          JsonObject data = this.validator.getJsonPayload(req);
          // TODO: Add validation for the presense of the required fiedls

          long id = piiStorage.createPII(data);
          this.piiSqlDataAccess.recordChange(key, id, data);
          System.out.println("Created user with the ID: " + id);
          responseBody = this.generateResponse(id, HttpServletResponse.SC_OK, "PII Object created.");

        }
        else{
          resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
          responseBody = generateResponse(-1, HttpServletResponse.SC_FORBIDDEN,"Method POST is not allowed for key: "+key);
        }

      } catch (RequestException re) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        responseBody = generateResponse(-1, HttpServletResponse.SC_BAD_REQUEST, re.getMessage());
      } catch (DataException e) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        responseBody = generateResponse(-1, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }
    }
    else{
      responseBody = generateResponse(-1, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SQL module is not initialized");
    }

    new Gson().toJson(responseBody, resp.getWriter());
  }

  @Override
  public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader(CONTENT_ENCODING, APPLICATION_JSON);
    String key = req.getParameter("key");
    System.out.println("Received DELETE request with the key: " + key);
    Object responseBody;
    if (piiSqlDataAccess != null) {
      try {
        if (piiSqlDataAccess.isMethodAllowed(key, PIISqlDataAccess.METHOD.DELETE)) {
          long id = this.validator.getIdForGetDeletePut(req);

          this.piiStorage.deletePII(id);
          JsonObject delObject = new JsonObject();
          delObject.addProperty("all", "deleted");
          this.piiSqlDataAccess.recordChange(key, id, delObject);
          System.out.println("Deleted user with the ID: " + id);
          responseBody = this.generateResponse(id, HttpServletResponse.SC_OK, "PII Object deleted.");
        }
        else{
          resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
          responseBody = generateResponse(-1, HttpServletResponse.SC_FORBIDDEN,"Method DELETE is not allowed for key: "+key);
        }
      } catch (RequestException re) {
        System.err.println("Request error - ID not found. Error: " + re.getMessage());
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        responseBody = this.generateResponse(-1, HttpServletResponse.SC_BAD_REQUEST, re.getMessage());
      } catch (DataException e) {
        System.err.println("Error recording to the audit. Error: " + e.getMessage());
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        responseBody = this.generateResponse(-1, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }
    }
    else{
      responseBody = generateResponse(-1, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SQL module is not initialized");
    }

    new Gson().toJson(responseBody, resp.getWriter());

  }

  @Override
  public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader(CONTENT_ENCODING, APPLICATION_JSON);
    String key = req.getParameter("key");

    System.out.println("Received PUT request with the key: " + key);
    Object responseBody;
    if (piiSqlDataAccess != null) {
      try {
        if (piiSqlDataAccess.isMethodAllowed(key, PIISqlDataAccess.METHOD.PUT)) {
          long id = this.validator.getIdForGetDeletePut(req);
          JsonObject data = this.validator.getJsonPayload(req);
          this.piiStorage.updatePII(id, data);
          this.piiSqlDataAccess.recordChange(key, id, data);
          System.out.println("Updated user with the ID: " + id);
          responseBody =
              this.generateResponse(id, HttpServletResponse.SC_OK, "PII Object updated.");
        }
        else{
          resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
          responseBody = generateResponse(-1, HttpServletResponse.SC_FORBIDDEN,"Method PUT is not allowed for key: "+key);
        }
      } catch (RequestException re) {
        System.err.println("Request error: either ID not found or pahload is not JSON. Error: " + re.getMessage());
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        responseBody = this.generateResponse(-1, HttpServletResponse.SC_BAD_REQUEST, re.getMessage());
      } catch (DataException e) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        responseBody = generateResponse(-1, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }
    }
    else {
      responseBody = generateResponse(-1, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SQL module is not initialized");
    }
    new Gson().toJson(responseBody, resp.getWriter());
  }



  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader(CONTENT_ENCODING, APPLICATION_JSON);

    String key = req.getParameter("key");

    System.out.println("Received GET request with the key: " + key);
    Object responseBody;
    if (piiSqlDataAccess!= null) {
      try {
        if (piiSqlDataAccess.isMethodAllowed(key, PIISqlDataAccess.METHOD.GET)) {
          long id = this.validator.getIdForGetDeletePut(req);

          String fields = req.getParameter("data");

          JsonObject data = this.piiStorage.getPII(id);
          if (data == null) {
            System.err.println("User with id " + id + " is not found!");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            responseBody =
                this.generateResponse(
                    id, HttpServletResponse.SC_NOT_FOUND, "User with id " + id + " is not found!");
          } else {
            System.out.println("Found data for the user " + id + ": " + data.toString());
            data = this.returnedDataProcessor.removedUnneededFields(data, fields);
            responseBody = data;
          }
        }
        else{
          resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
          responseBody = generateResponse(-1, HttpServletResponse.SC_FORBIDDEN,"Method GET is not allowed for key: "+key);
        }
      } catch (RequestException re) {
        System.err.println("Invalid json. Error: " + re.getMessage());
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        responseBody = this.generateResponse(-1, HttpServletResponse.SC_BAD_REQUEST, re.getMessage());
      } catch (DataException | DataProcessorException e) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        responseBody = generateResponse(-1, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }
    }
    else{
      responseBody = generateResponse(-1, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SQL module is not initialized");
    }

    new Gson().toJson(responseBody, resp.getWriter());

  }


  private JsonObject generateResponse(long id, int responseCode, String message){
    JsonObject responseObject = new JsonObject();
    responseObject.addProperty("code", responseCode);
    responseObject.addProperty("message",message);
    responseObject.addProperty("id", id);
    return responseObject;
  }

}
