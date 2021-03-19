package game;

import board.Piece;

public class Player {
    public final String name;
    public final Piece piece;
    public final Side side;

    public Player(String name, Piece piece, Side side) {
        this.name = name;
        this.piece = piece;
        this.side = side;
    }


}
