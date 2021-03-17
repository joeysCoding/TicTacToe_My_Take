package board;

/**
 * There is already a piece on the board, in a position when there shouldn't be
 */
public class BoardPositionNotFreeException extends Exception {
    public BoardPositionNotFreeException() {
    }

    public BoardPositionNotFreeException(String message) {
        super(message);
    }
}
