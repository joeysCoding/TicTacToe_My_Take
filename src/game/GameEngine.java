package game;

import board.Board;
import board.BoardPositionNotFreeException;
import board.Piece;
import board.Position;
import protocol.MoveObserver;
import protocol.RequestEnemyMoveObserver;

public interface GameEngine {
    /**
     * set this position
     * @param position
     * @exception BoardPositionNotFreeException there aleardy is a piece at this position
     * @exception GameStatusNotYourTurnException player can't set piece in this state,
     * because its not his turn, or game has't been started, or has ended
     */
    void set(Position position)
            throws BoardPositionNotFreeException, GameStatusNotYourTurnException, GameOverException, GameWonException;

    /**
     * has this piece won?
     * @param piece
     * @return
     */
    boolean hasWon(Piece piece);

    /**
     * get the current status the gameengine is in
     * @return
     */
    GameStatus getStatus();

    /**
     * which piece can play currently,
     * @return null if game status is game over or won, check with getStatus() first
     */
    Piece getTurn();

    Board getBoard();


    Player getAlice();

    Player getBob();
}
