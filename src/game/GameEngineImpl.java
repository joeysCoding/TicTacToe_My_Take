package game;

import board.*;

/**
 * Manages the board
 * usage: Use set to set pieces on the board. GE ensures that players can set in alternating order
 * !Here always the same piece starts, but during coin tossing the pieces are allocated
 * depending on which player is winner or loser
 */
public class GameEngineImpl implements GameEngine {
    private GameStatus status; // !!! Don't change directly use setter!!!
    private final Board board;

    private Piece turn = Piece.STARTER; // Hows turn is it to play
    private final Player alice;
    private final Player bob;

    // only set once game status GAME_WON, otherwise null
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
            throws BoardPositionNotFreeException, GameStatusNotYourTurnException, GameOverException, GameWonException {
        if(status == GameStatus.GAME_OVER)
            throw new GameOverException("Trying to play, but game is already over!");
        if(status == GameStatus.GAME_WON)
            throw new GameWonException("Trying to play, but game is already won!");
        if(turn != position.piece)
            throw new GameStatusNotYourTurnException("trying to play game but its not your turn. " +
                    "Your Piece: " + position.piece + " game status is actual: " + this.status);
        try{
            board.set(position);
            if(board.hasWon(position.piece)){
                this.Winner = position.piece;
                this.Loser = Piece.getOtherPiece(position.piece);
                this.setStatus(GameStatus.GAME_WON);
                this.turn = null;
            } else if(board.isGameOver()){
                this.setStatus(GameStatus.GAME_OVER);
                this.turn = null;
            } else {
                this.turn = Piece.getOtherPiece(position.piece);
            }
        } catch (BoardPositionNotFreeException e){
            e.printStackTrace();
            throw new BoardPositionNotFreeException("Game Set: this position is already taken: " + position.toString());
        }
    }

    @Override
    public boolean hasWon(Piece piece){
        if(this.status != GameStatus.GAME_WON)
            return false;
        return this.Winner == piece;
    }

    @Override
    public GameStatus getStatus() {
        return status;
    }

    public Piece getWinner() {
        return Winner;
    }

    public Piece getLoser() {
        return Loser;
    }

    @Override
    public Piece getTurn(){return this.turn;}

    @Override
    public Board getBoard() {
        return this.board;
    }

    @Override
    public Player getAlice() {
        return alice;
    }

    @Override
    public Player getBob() {
        return bob;
    }
}
