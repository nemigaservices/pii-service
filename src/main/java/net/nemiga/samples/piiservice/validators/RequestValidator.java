package net.nemiga.samples.piiservice.validators;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Validates the request for different methods
 */
public class RequestValidator {

    public JsonObject getJsonPayload(HttpServletRequest request) throws RequestException{
        JsonParser parser = new JsonParser();
        try {
            JsonElement bodyElement = parser.parse(request.getReader());
            JsonObject body = bodyElement.getAsJsonObject();
            return body;
        } catch (IOException e) {
            throw new RequestException("Error parsing JSON in the request: "+e.getMessage());
        }
    }

    public int getIdForGetDeletePut(HttpServletRequest request) throws RequestException{
        String pathInfo = request.getPathInfo(); // /{value}/test
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length!=2){
            throw new RequestException("Path for GET should match /pii/{ID}");
        }
        try{
            return Integer.parseInt(pathParts[1]);
        }
        catch (Exception e){
            throw new RequestException("ID is not an integer!");
        }
    }
}
