package board;

import org.junit.Test;

public class TestPosition {

    @Test
    public void newPositionGood() throws PositionOutOfBoundException {
        new Position(0, 0, Piece.X);
    }

    @Test
    public void newPositionEdge() throws PositionOutOfBoundException {
        new Position(Position.UPPER_BOUND_X, Position.UPPER_BOUND_Y, Piece.X);
    }

    @Test (expected = PositionOutOfBoundException.class)
    public void newPositionBad() throws PositionOutOfBoundException {
        new Position(Position.UPPER_BOUND_X, Position.UPPER_BOUND_Y + 1, Piece.X);
    }
}
