package game;

import board.Board;
import board.BoardPositionNotFreeException;
import board.Piece;
import board.Position;
import protocol.MoveObserver;
import protocol.RequestEnemyMoveObserver;

public interface GameEngine {
    /**
     * set the piece of this sides player
     * (can't be set in constructor, because it has
     * to be negotiated by the ProtocalEngine first)
     * The game starts after the piece is picked
     * @param piece
     * @exception GameStatusNotYourTurnException pick can't be called after
     * game has already started
     */
    void pick(Piece piece) throws GameStatusGameAlreadyStartedException;

    /**
     * set this position
     * @param position
     * @exception BoardPositionNotFreeException there aleardy is a piece at this position
     * @exception GameStatusNotYourTurnException player can't set piece in this state,
     * because its not his turn, or game has't been started, or has ended
     */
    void set(Position position)
            throws BoardPositionNotFreeException, GameStatusNotYourTurnException;

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

    boolean getRequestedEnemyMove();

    void addRequestEnemyMoveObserver(RequestEnemyMoveObserver observer);

    void addMoveObservers(MoveObserver observer);

    Board getBoard();

    boolean isReadyForMove(Piece piece);

    String getName();
}
