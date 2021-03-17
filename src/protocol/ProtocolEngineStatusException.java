package protocol;

/**
 * trying to call protocol engine in wrong state
 * before calling:
 * - connect : status has to be not connected
 */
public class ProtocolEngineStatusException extends Exception {
    public ProtocolEngineStatusException() {
    }

    public ProtocolEngineStatusException(String message) {
        super(message);
    }
}
