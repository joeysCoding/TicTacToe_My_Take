package board;

import org.junit.Assert;
import org.junit.Test;

public class TestBoard {
    private Board getBoard(){
        return new BoardImpl();
    }
    @Test
    public void setIsFreeGood() throws PositionOutOfBoundException, BoardPositionNotFreeException {
        Board board = getBoard();

        Assert.assertTrue(board.isFree(new Position(0,0, Piece.X)));
        board.set(new Position(0,0, Piece.X));
        Assert.assertFalse(board.isFree(new Position(0,0, Piece.X)));
        Piece actualPiece = board.getPieceAt(new Position(0, 0, (Piece) null));

        Assert.assertEquals(Piece.X ,actualPiece);
    }

    @Test (expected = BoardPositionNotFreeException.class)
    public void setBadSetAtNotFreePos() throws PositionOutOfBoundException, BoardPositionNotFreeException {
        Board board = getBoard();

        board.set(new Position(0,0, Piece.X));
        board.set(new Position(0,0, Piece.O));
    }

    @Test
    public void hasWonColumnGood() throws PositionOutOfBoundException, BoardPositionNotFreeException {
        Board board = getBoard();

        board.set(new Position(0,0, Piece.X));
        board.set(new Position(2,0, Piece.O));
        board.set(new Position(0,1, Piece.X));
        board.set(new Position(1,0, Piece.O));
        board.set(new Position(0,2, Piece.X));

        Assert.assertTrue(board.hasWon(Piece.X));
        Assert.assertFalse(board.hasWon(Piece.O));
    }

    @Test
    public void hasWonRowGood() throws PositionOutOfBoundException, BoardPositionNotFreeException {
        Board board = getBoard();

        board.set(new Position(0,0, Piece.X));
        board.set(new Position(1,1, Piece.O));
        board.set(new Position(1,0, Piece.X));
        board.set(new Position(2,2, Piece.O));
        board.set(new Position(2,0, Piece.X));

        Assert.assertTrue(board.hasWon(Piece.X));
        Assert.assertFalse(board.hasWon(Piece.O));
    }

    @Test
    public void hasWonDiagonalGood() throws PositionOutOfBoundException, BoardPositionNotFreeException {
        Board board = getBoard();

        board.set(new Position(0,0, Piece.X));
        board.set(new Position(2,0, Piece.O));
        board.set(new Position(1,1, Piece.X));
        board.set(new Position(1,0, Piece.O));
        board.set(new Position(2,2, Piece.X));

        Assert.assertTrue(board.hasWon(Piece.X));
        Assert.assertFalse(board.hasWon(Piece.O));
    }
}
