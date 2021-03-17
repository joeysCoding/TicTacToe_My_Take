package board;

/**
 * Position x or y larger than Position.UPPER_BOUND_X ...
 */
public class PositionOutOfBoundException extends Exception {
    public PositionOutOfBoundException() {
    }

    public PositionOutOfBoundException(String message) {
        super(message);
    }
}
