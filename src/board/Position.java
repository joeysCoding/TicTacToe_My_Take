package board;

import protocol.ProtocolEngineCMDReadException;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    public static Position readFrom(DataInputStream dis) throws IOException, ProtocolEngineCMDReadException {
        int x = dis.readInt();
        int y = dis.readInt();
        int pieceOrdinal = dis.readInt();
        Piece piece = Piece.intToPiece(pieceOrdinal);

        try{
            return new Position(x, y, piece);
        } catch (PositionOutOfBoundException e){
            e.printStackTrace();
            throw new ProtocolEngineCMDReadException("Can't read command from inputsttream");
        }
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(this.x);
        dos.writeInt(this.y);
        dos.writeInt(piece.pieceToInt());
        return bos.toByteArray();
    }

    @Override
    public String toString(){
        return x + ", " + y + ", " + piece;
    }
}
