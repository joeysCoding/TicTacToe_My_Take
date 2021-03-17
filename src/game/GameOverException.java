package game;

/**
 * trying to set, but game is already over
 */
public class GameOverException extends Exception {
    public GameOverException() {
    }

    public GameOverException(String message) {
        super(message);
    }
}
