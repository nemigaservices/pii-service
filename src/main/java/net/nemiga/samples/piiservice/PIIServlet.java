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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** A servlet that echoes JSON message bodies. */
@WebServlet("/pii")
public class PIIServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.addHeader("Content-Encoding", "application/json");

    String key = req.getParameter("key");

    System.out.println("Received request with the key: " + key);
    Object responseBody = null;
    try {
      JsonParser parser = new JsonParser();

      JsonElement bodyElement = parser.parse(req.getReader());
      JsonObject body = bodyElement.getAsJsonObject();
      body.addProperty("key", key);
      System.out.println("Valid json, sending the reply.");
      responseBody = body;

    } catch (JsonParseException je) {
      System.err.println("Invalid json. Error: " + je.getMessage());
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      JsonObject error = new JsonObject();
      error.addProperty("key", key);
      error.addProperty("code", HttpServletResponse.SC_BAD_REQUEST);
      error.addProperty("message", "Body was not valid JSON.");
      responseBody = error;
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

  }

}
