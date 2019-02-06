package net.nemiga.samples.piiservice.validators;

/**
 * Indicates a validation exception
 */
public class RequestException extends Exception {
    public RequestException(String message){
        super(message);
    }
}
