package board;

/**
 * represents game piece
 */
public enum Piece {
    X,
    O;

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
}
