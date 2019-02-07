package net.nemiga.samples.piiservice.processors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Contains methods to manipulate the returned data
 */
public class ReturnedDataProcessor {
    /**
     * Removes unneeded fields
     * @param sourceData source data object
     * @param neededFieldsString comma delimited string with the least of fields to keep
     * @return JSON object containing only needed fields
     * @throws DataProcessorException indicates the issue with the list of the fields to keep
     */
    public JsonObject removedUnneededFields(JsonObject sourceData, String neededFieldsString) throws DataProcessorException{
        if (neededFieldsString==null || neededFieldsString.isEmpty() ){
            return sourceData;
        }
        JsonObject ret = new JsonObject();
        String[] neededFields = neededFieldsString.split(",");
        for(String neededField:neededFields){
            JsonElement jsonElement = sourceData.get(neededField);
            if (jsonElement == null){
                throw new DataProcessorException("Element "+neededField+" does not exist in the data!");
            }
            ret.add(neededField, jsonElement);
        }
        return ret;
    }
}
