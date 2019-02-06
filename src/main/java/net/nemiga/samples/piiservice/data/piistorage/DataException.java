package net.nemiga.samples.piiservice.data.piistorage;

/**
 * Indicates the issue with the data that is to be put to the PII storage
 */
public class DataException extends Exception {
    public DataException(String message){
        super(message);
    }
}
