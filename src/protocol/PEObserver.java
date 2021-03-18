package protocol;

import board.Board;
import board.Position;
import userInterface.UICantGetNextMoveFromUser;

public interface PEObserver {
    /**
     *  Ask Alice for her next move
     * @return
     */
    Position promptForNextSet() throws UICantGetNextMoveFromUser;

    void updatedBoard(Board board);

    void receiveMsg(String msg);

    void promptName();
}
