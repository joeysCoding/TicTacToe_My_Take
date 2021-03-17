package game;

/**
 * can't pick piece in status when game hase already been stated
 */
public class GameStatusGameAlreadyStartedException extends Exception {
    public GameStatusGameAlreadyStartedException() {
    }

    public GameStatusGameAlreadyStartedException(String message) {
        super(message);
    }
}
