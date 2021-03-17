package protocol;

/**
 * can't connect to peer
 */
public class ProtocolEngineNoConnectionException extends Exception {
    public ProtocolEngineNoConnectionException() {
    }

    public ProtocolEngineNoConnectionException(String message) {
        super(message);
    }
}
