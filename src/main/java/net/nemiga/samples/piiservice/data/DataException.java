package net.nemiga.samples.piiservice.data;

/**
 * Indicates the issue with the data being stored in any of the databases
 */
public class DataException extends Exception {
    public DataException(String message){
        super(message);
    }
}
