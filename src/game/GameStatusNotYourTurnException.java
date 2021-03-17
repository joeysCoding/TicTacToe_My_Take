package game;

/**
 * Players are supposed to set pieces in order
 * Piece_O -> Piece_X -> Piece_O ... until one player has won
 * this exception is thrown when this order is violated, or game not started yet etc.
 */
public class GameStatusNotYourTurnException extends Exception {
    public GameStatusNotYourTurnException() {
    }

    public GameStatusNotYourTurnException(String message) {
        super(message);
    }
}
