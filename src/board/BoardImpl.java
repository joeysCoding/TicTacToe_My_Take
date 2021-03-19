package board;

public class BoardImpl implements Board {
    Piece[][] board;

    public BoardImpl() {
        board = new Piece[Position.UPPER_BOUND_X + 1][Position.UPPER_BOUND_Y + 1];
    }

    @Override
    public void set(Position position) throws BoardPositionNotFreeException {
        if(!isFree(position))
            throw new BoardPositionNotFreeException("Board not free at Pos:" + position.x + position.y);
        board[position.x][position.y] = position.piece;
    }

    @Override
    public Piece getPieceAt(Position position) {
        return board[position.x][position.y];
    }

    @Override
    public boolean isFree(Position position) {
        return getPieceAt(position) == null;
    }

    @Override
    public boolean hasWon(Piece piece) {
        return hasWonRow(piece) || hasWonColumn(piece) || hasWonDiagonal(piece);
    }

    private boolean hasWonRow(Piece piece){
        for(int row = 0; row <= Position.UPPER_BOUND_X; row++){
            if(board[row][0] == piece && board[row][1] == piece && board[row][2] == piece)
                return true;
        }
        return false;
    }


    private boolean hasWonColumn(Piece piece){
        for(int column = 0; column <= Position.UPPER_BOUND_X; column++){
            if(board[0][column] == piece && board[1][column] == piece && board[2][column] == piece)
                return true;
        }
        return false;
    }

    private boolean hasWonDiagonal(Piece piece){
        if(board[0][0] == piece && board[1][1] == piece && board[2][2] == piece)
            return true;
        if(board[0][2] == piece && board[1][1] == piece && board[2][0] == piece)
            return true;
        return false;
    }

    @Override
    public boolean isGameOver(){
        for(int row = 0; row <= Position.UPPER_BOUND_Y; row++){
            for(int col = 0; col <= Position.UPPER_BOUND_X; col++){
                if(board[row] [col] == null)
                    return false;
            }
        }
        return true;
    }
}
