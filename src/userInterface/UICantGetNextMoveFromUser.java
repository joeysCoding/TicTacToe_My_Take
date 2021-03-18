package userInterface;

/**
 * something went wrong while prompting the user for next move
 */
public class UICantGetNextMoveFromUser extends Exception {
    public UICantGetNextMoveFromUser() {
    }

    public UICantGetNextMoveFromUser(String message) {
        super(message);
    }
}
