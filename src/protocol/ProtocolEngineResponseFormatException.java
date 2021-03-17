package protocol;

/**
 * Protocol can't read response.
 * Response is in format that violates the protocal
 */
public class ProtocolEngineResponseFormatException extends Exception {
    public ProtocolEngineResponseFormatException() {
    }

    public ProtocolEngineResponseFormatException(String message) {
        super(message);
    }
}
