package game;

import board.*;

public class GameEngineImpl implements GameEngine {
    private GameStatus status; // !!! Don't change directly use setter!!!
    private final Board board;

    private Piece turn = Piece.STARTER; // Hows turn is it to play
    private final Player alice;
    private final Player bob;

    private Piece Winner;
    private Piece Loser;

    public GameEngineImpl(Player alice, Player bob)  {
        this.board = new BoardImpl();
        this.alice = alice;
        this.bob = bob;
        this.status = GameStatus.GAMING;
    }

    private void setStatus(GameStatus newStatus){
        this.status = newStatus;
    }

    @Override
    public synchronized void set(Position position)
            throws BoardPositionNotFreeException, GameStatusNotYourTurnException, GameOverException {
        if(turn != position.piece)
            throw new GameStatusNotYourTurnException("trying to play game but its not your turn. " +
                    "Your Piece: " + position.piece + " game status is actual: " + this.status);
        if(status != GameStatus.GAMING)
            throw new GameOverException("Trying to play, but game is already over!");
        try{
            board.set(position);
            if(board.hasWon(position.piece))
                this.setStatus(GameStatus.GAME_WON);
            if(board.isGameOver())
                this.setStatus(GameStatus.GAME_OVER);
            else {
                this.turn = Piece.getOtherPiece(position.piece);
            }
        } catch (BoardPositionNotFreeException e){
            e.printStackTrace();
            throw new BoardPositionNotFreeException("Game Set: this position is already taken: " + position.toString());
        }
    }

    @Override
    public boolean hasWon(Piece piece) {
        return board.hasWon(piece);
    }

    @Override
    public GameStatus getStatus() {
        return status;
    }

    @Override
    public Piece getTurn(){return this.turn;}

    @Override
    public Board getBoard() {
        return this.board;
    }
}
