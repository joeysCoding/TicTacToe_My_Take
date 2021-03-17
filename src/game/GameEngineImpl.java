package game;

import board.Board;
import board.BoardPositionNotFreeException;
import board.Piece;
import board.Position;
import protocol.MoveObserver;
import protocol.RequestEnemyMoveObserver;

import java.util.ArrayList;
import java.util.List;

public class GameEngineImpl implements GameEngine {
    private GameStatus status; // !!! Don't change directly use setter!!!

    private final Board board;

    private final Piece starter;
    private Piece pieceUS;
//    private GameStatus turnUS;      // status when it's upon us to move
    private Piece pieceEnemy;
//    private GameStatus turnEnemy;   // status when it's the enemies turn
    private final String playerNameUS;
    private final String playerNameEnemy;

    private boolean requestedEnemyMove;

    private List<RequestEnemyMoveObserver> requestEnemyMoveObservers;
    private List<MoveObserver> moveObservers;

    public GameEngineImpl(Board board,
                          Piece starter,
                          String playerNameUS,
                          String playerNameEnemy)  {
        this.board = board;
        this.status = GameStatus.WAITING_FOR_PICK;
        this.starter = starter;
        this.playerNameUS = playerNameUS;
        this.playerNameEnemy = playerNameEnemy;
        this.requestEnemyMoveObservers = new ArrayList<>();
        this.moveObservers = new ArrayList<>();
    }

    @Override
    public boolean getRequestedEnemyMove() {
        return requestedEnemyMove;
    }

    private void setStatus(GameStatus newStatus){
        if(newStatus == this.pieceToTurn(pieceEnemy)) {
            this.requestEnemyMove();
        }

        this.status = newStatus;
    }

    private void requestEnemyMove() {
        this.requestedEnemyMove = true;
        for(RequestEnemyMoveObserver observer: requestEnemyMoveObservers){
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    observer.requestMove();
                }
            });
            t.start();
        }
    }

    @Override
    public void pick(Piece piece) throws GameStatusGameAlreadyStartedException {
        if(status != GameStatus.WAITING_FOR_PICK)
            throw new GameStatusGameAlreadyStartedException("trying to pick game piece in wrong status. " +
                    "should be: " + GameStatus.WAITING_FOR_PICK + " actual: " + this.status);

        this.pieceUS = piece;
        this.pieceEnemy = this.pieceUS == Piece.X ? Piece.O : Piece.X;

        this.setStatus(this.starter == Piece.X ? GameStatus.TURN_X : GameStatus.TURN_O);
    }

    @Override
    public synchronized void set(Position position)
            throws BoardPositionNotFreeException, GameStatusNotYourTurnException {
        if(!isReadyForMove(position.piece))
            throw new GameStatusNotYourTurnException("trying to play game but its not your turn. " +
                    "Your Piece: " + position.piece + " game status is actual: " + this.status);
        try{
            board.set(position);
            if(board.hasWon(position.piece))
                this.setStatus(GameStatus.GAME_ENDED);
            else {
                this.setStatus(this.status == GameStatus.TURN_X ? GameStatus.TURN_O : GameStatus.TURN_X);
                if(position.piece == pieceEnemy)
                    this.requestedEnemyMove = false;
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

    public GameStatus getStatus() {
        return status;
    }

    @Override
    public void addRequestEnemyMoveObserver(RequestEnemyMoveObserver observer){
        if(!this.requestEnemyMoveObservers.contains(observer))
                   requestEnemyMoveObservers.add(observer);
    }


    @Override
    public void addMoveObservers(MoveObserver observer){
        if(!this.moveObservers.contains(observer)){
            // observer call could end up in infinite loop
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    moveObservers.add(observer);
                }
            });
            t.start();
        }
    }

    @Override
    public Board getBoard() {
        return this.board;
    }


    private boolean isRightTurn(Piece piece){
        if(!(this.status == GameStatus.TURN_O || this.status == GameStatus.TURN_X))
            return false;
        return piece == turnToPiece(this.status);
    }

    private boolean isNotBlocked(Piece piece){
        return piece == pieceEnemy ? requestedEnemyMove : !requestedEnemyMove;
    }

    /**
     * always use before calling set()
     * @param piece to be set
     * @return
     * true - you can move
     * false - don't move
     */
    @Override
    public boolean isReadyForMove(Piece piece){
        return isRightTurn(piece) && isNotBlocked(piece);
    }

    @Override
    public String getName() {
        return this.playerNameUS;
    }


    /**
     *
     * @param turnOorX turnX or turnY only
     * @return
     */
    private Piece turnToPiece(GameStatus turnOorX){
        return turnOorX == GameStatus.TURN_O ? Piece.O : Piece.X;
    }

    private GameStatus pieceToTurn(Piece piece){
        return piece == Piece.X ? GameStatus.TURN_X : GameStatus.TURN_O;
    }
}
