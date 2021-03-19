package board;

/**
 * represents game piece
 */
public enum Piece {
    X,
    O;

    public static final Piece WIN = X; // Alice piece when she wins
    public static final Piece LOSS = O;   // Alice piece when she loses
    public static final Piece STARTER = X; // refers to which piece starts game

    public static int getLowerOrdinal(){
        return 0;
    }

    public static int getUpperOrdinal(){
        return Piece.values().length - 1;
    }

    public int pieceToInt(){
        return this.ordinal();
    }

    public static Piece intToPiece(int pieceNum){
        for(Piece curPiece: Piece.values())
            if(curPiece.ordinal() == pieceNum)
                return curPiece;
        throw new IllegalArgumentException("Piece num has to be within ordinal limits");
    }

    public static Piece getOtherPiece(Piece piece){
        return piece == X ? O : X;
    }
}
