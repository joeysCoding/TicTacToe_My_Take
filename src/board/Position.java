package board;

public class Position {
    public static final int UPPER_BOUND_X = 2;
    public static final int UPPER_BOUND_Y = 2;

    public final int x;
    public final int y;
    public final Piece piece;

    public Position(int x, int y, Piece piece) throws PositionOutOfBoundException{
        if(isOutOfBound(x, y))
            throw new PositionOutOfBoundException("upper bound x: " + UPPER_BOUND_X + " actual: " + x
                    + "upper bound y: " + UPPER_BOUND_Y + " actual: " + y);
        this.x = x;
        this.y = y;
        this.piece = piece;
    }

    private boolean isOutOfBound(int x, int y){
        return x > UPPER_BOUND_X || y > UPPER_BOUND_Y;
    }

    @Override
    public String toString(){
        return x + ", " + y + ", " + piece;
    }
}
