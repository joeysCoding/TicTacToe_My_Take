package game;

/**
 * game is already decided, won, but still trying to set
 */
public class GameWonException extends Throwable {
    public GameWonException() {
    }

    public GameWonException(String message) {
        super(message);
    }
}
