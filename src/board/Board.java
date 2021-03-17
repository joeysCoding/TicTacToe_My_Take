package board;

public interface Board {
    /**
     * set piece at position on board
     * @param position
     */
    void set(Position position) throws BoardPositionNotFreeException;

    /**
     * get the piece that was set at this position
     * @param position Piece argument can be null
     * @return null for there is no piece at this position,
     * else actual piece at this position
     */
    Piece getPieceAt(Position position);

    /**
     * can I set a piece here, because pos is free
     * @param position
     * @return false, there is a piece in this position, true otherwise
     */
    boolean isFree(Position position);

    /**
     * has this piece won the game?
     * @param piece
     * @return true - yes has won
     */
    boolean hasWon(Piece piece);
}
