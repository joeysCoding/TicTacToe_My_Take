package protocol;

import board.BoardPositionNotFreeException;
import board.Position;
import game.GameOverException;

public interface MoveObserver {
    /**
     * Objects of implementing class are notified by moves on the game board
     * @param position that is set by move
     */
    void registerMove(Position position) throws BoardPositionNotFreeException, GameOverException;
}
