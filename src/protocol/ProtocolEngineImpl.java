package protocol;

import board.BoardPositionNotFreeException;
import board.Piece;
import board.Position;
import command.Command;
import command.CommandAction;
import game.*;
import userInterface.UICantGetNextMoveFromUser;

import java.io.*;
import java.util.Random;

/**
 * Manages the communication with Bob and informs peListeners about the game
 * states and asks the ui to get required input from user
 * Usage:
 * Game Alices game session with bob has to be started in this order!:
 * 1. Construct ProtocolEngine
 * 2. call connect() // connects Alices to bobs Protocol Engine
 * 3. call requestNameBob() // Alice and bob exchange names
 * 4. call amIStarter() // Alice and bob negotiate who starts
 * 5. call setGameEngine(Alice's GameEngine) // doesn't start the game
 * 6. New Thread in which this PE runs in its own thread, that starts the game
 * -> returns from thread when the game is either won or game over
 *
 *
 * Alices input are handled by the user interface, PE only calls PE Listeners
 * if input is required.
 * The game ends when one of the players has won, or all gameboard positions are
 * set. No further action required.
 */
public class ProtocolEngineImpl implements ProtocolEngine, Runnable{
    private final PEObserver peObserver;
    private GameEngine gameEngine;
    ProtocolStatus status;
    private Thread peThread;
    // status check flags
    private boolean wasConnectionEstablished;
    private boolean wasNameExchanged;
    private boolean wasStarterDetermined;
    private boolean wasStartEnemyConfirmed;
    // naming convention:
    private Player alice;
    private Player bob;
    // its job of the protocol engine to request and receive names
    // therefore we don't give it the names at construction,
    // but let it prompt for the enemy name via sending a command over os to bob
    // and let bob request alices name from command
    private String playerNameAlice; // alice is our side, us ....
    private String playerNameBob; // bob stands for enemy, other side ....

    private CoinTosser coinTosser;
    private final Piece tossWinner = Piece.X;
    private final Piece tossLoser = Piece.O;

    private InputStream is;
    private DataInputStream dis;
    private OutputStream os;
    private DataOutputStream dos;
    private long bobWaitTime = 50;

    public ProtocolEngineImpl(PEObserver peObserver) throws ProtocolEngineNoConnectionException {
        coinTosser = new CoinTosser();
        this.peObserver = peObserver;
        this.status = ProtocolStatus.NO_CONNECTION_CONFIRMED;
    }

    @Override
    public void handleConnection(OutputStream os, InputStream is){
        this.os = os;
        this.dos = new DataOutputStream(os);
        this.is = is;
        this.dis = new DataInputStream(is);
        this.peThread = new Thread(this);
        this.peThread.start();
    }

    @Override
    public void run() {
        // confirm connection with bob
        try {
            new Command(CommandAction.REQUEST_CONNECTION_CONFIRMANTION).sendVia(dos);
            processNextBobCommand();
            processNextBobCommand();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProtocolEngineNoCommandInStreamException e) {
            e.printStackTrace();
        } catch (ProtocolEngineStatusException e) {
            e.printStackTrace();
        } catch (UICantGetNextMoveFromUser uiCantGetNextMoveFromUser) {
            uiCantGetNextMoveFromUser.printStackTrace();
        }
        if(status != ProtocolStatus.CONNECTED){
            new Command(CommandAction.FATAL_ERROR);
            peObserver.receiveMsg("Fatal game status, unable to confirm connection. Game can't be started.");
            System.err.println("Unable to connect. Gamestatus shoulb be: "
                    + ProtocolStatus.CONNECTED + " but is: " + status );
            System.exit(1);
        }
        // exchange user names
        try {
//            System.out.println("Thread num: " + Thread.currentThread().getId() + " about to send call");
            new Command(CommandAction.REQUEST_NAME).sendVia(dos);
            processNextBobCommand();
            processNextBobCommand();

//            System.out.println("Thread num: " + Thread.currentThread().getId() + " about to send Command Request_Name");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ProtocolEngineNoCommandInStreamException e) {
            e.printStackTrace();
        } catch (ProtocolEngineStatusException e) {
            e.printStackTrace();
        } catch (UICantGetNextMoveFromUser uiCantGetNextMoveFromUser) {
            uiCantGetNextMoveFromUser.printStackTrace();
        }

        // time to toss coin
        if(status != ProtocolStatus.NAMES_EXCHANGED || playerNameAlice == null || playerNameBob == null){
            new Command(CommandAction.FATAL_ERROR);
            peObserver.receiveMsg("Fatal game status, unable to confirm connection. Game can't be started.");
            System.err.println("Unable to connect. Gamestatus shoulb be: "
                    + ProtocolStatus.CONNECTED + " but is: " + status );
            System.exit(1);
        }

        try {
            boolean aliceStarts = coinTosser.isAliceCoinTossWinner();
            Piece alicePiece = aliceStarts ? Piece.WIN : Piece.LOSS;
            alice = new Player(playerNameAlice, alicePiece, Side.ALICE);
            Piece bobPiece = Piece.getOtherPiece(alicePiece);
            bob = new Player(playerNameBob, bobPiece, Side.BOB);
            wasStarterDetermined = true;
            status = ProtocolStatus.STARTER_DETERMINED;
        } catch (IOException | ProtocolEngineNoCommandInStreamException | ProtocolEngineStatusException | UICantGetNextMoveFromUser | ProtocolEngineNoEnemyCoinReceivedException e) {
            e.printStackTrace();
        }

        // setting up the game
        if(status != ProtocolStatus.STARTER_DETERMINED){
            new Command(CommandAction.FATAL_ERROR);
            peObserver.receiveMsg("Fatal game status, Starter couldn't be determined. Game can't be started.");
            System.err.println("Unable to connect. Gamestatus shoulb be: "
                    + ProtocolStatus.STARTER_DETERMINED + " but is: " + status );
            System.exit(1);
        }

        this.gameEngine = new GameEngineImpl(alice, bob);

        // starting the game
        try {
            new Command(CommandAction.START_REQUEST).sendVia(dos);
            processNextBobCommand();
            processNextBobCommand();
            if(!amIStarter())
                new Command(CommandAction.REQUEST_MOVE).sendVia(dos);
        } catch (IOException | ProtocolEngineNoCommandInStreamException | ProtocolEngineStatusException | UICantGetNextMoveFromUser e) {
            e.printStackTrace();
        }
//        while(status == ProtocolStatus.GAME_RUNNING){
//            try {
//                processNextBobCommand();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (ProtocolEngineNoCommandInStreamException e) {
//                e.printStackTrace();
//            } catch (ProtocolEngineStatusException e) {
//                e.printStackTrace();
//            } catch (UICantGetNextMoveFromUser uiCantGetNextMoveFromUser) {
//                uiCantGetNextMoveFromUser.printStackTrace();
//            }
//        }


    }

    private void waitForBob() {
        try {
            while(dis.available() == 0)
                Thread.sleep(10);
            Thread.sleep(this.bobWaitTime);
        } catch (IOException | InterruptedException  e) {
            e.printStackTrace();
            System.err.println("Trying to wait for Bob failed!");
            System.exit(1);
        }
    }


    private class CoinTosser{
        private int alice = 0;
        private int bob = 0;

        private boolean alreadyDetermined = false;
        private boolean aliceStarts;

        public CoinTosser(){
            this.alice = this.generateRandom();
        }

        public void requestBobCoin() throws ProtocolEngineStatusException, ProtocolEngineNoCommandInStreamException, UICantGetNextMoveFromUser, IOException {
            this.bob = 0;
            while (this.bob == 0) {
                new Command(CommandAction.COIN_INT_REQUEST).sendVia(os);
                processNextBobCommand();
                processNextBobCommand();
            }
        }


        public int getAliceCoin() {
            return this.alice;
        }

        public void setBobCoin(int receivedEnemyCoin){
            this.bob = receivedEnemyCoin;
        }

        private int generateRandom(){
            long seed = System.currentTimeMillis() + Thread.currentThread().getId();
            Random random = new Random(seed);
            int randomNum = 1 + random.nextInt(10000);
            return randomNum;
        }

        public boolean isAliceCoinTossWinner() throws IOException, ProtocolEngineNoEnemyCoinReceivedException, ProtocolEngineStatusException, ProtocolEngineNoCommandInStreamException, UICantGetNextMoveFromUser {
            if (this.alreadyDetermined) return this.aliceStarts;
            requestBobCoin();

            if(this.alice == this.bob){
                this.alice = this.generateRandom();
                this.requestBobCoin();
                return this.isAliceCoinTossWinner();
            }

            this.aliceStarts = this.alice > this.bob;
            String areStarter = this.aliceStarts ? "you start the game." : "don't start the game";
            peObserver.receiveMsg(playerNameAlice + " your coin wos: " + this.alice + " while " + playerNameBob + " coin was: " + this.bob +
                    " The Lower coin wins, therefore you " + areStarter);
            this.alreadyDetermined = true;
            return this.aliceStarts;
        }
    }

    private void processNextBobCommand() throws IOException, ProtocolEngineNoCommandInStreamException,
            ProtocolEngineStatusException, UICantGetNextMoveFromUser {
        waitForBob();
        Command cmd = Command.readCMDFrom(dis);
        CommandAction cmdAction = cmd.getCmdAction();

        switch (cmdAction){
            case REQUEST_CONNECTION_CONFIRMANTION:
                System.out.println("Thread num: " + Thread.currentThread().getId() + "  Received cmdAction: " + cmdAction);
                new Command(CommandAction.RECEIVE_CONNECTION_CONFIRMED).sendVia(dos);
                break;
            case RECEIVE_CONNECTION_CONFIRMED:
                System.out.println("Thread num: " + Thread.currentThread().getId() + "  Received cmdAction: " + cmdAction);
                this.status = ProtocolStatus.CONNECTED;
                this.wasConnectionEstablished = true;
                break;
            case REQUEST_NAME:
                System.out.println("Thread num: " + Thread.currentThread().getId() + "  Received cmdAction: " + cmdAction);
                playerNameAlice = askObserverForAliceName();
                new Command(CommandAction.ENEMY_NAME_RECEIVE).addToCmdInfoAsUTF(playerNameAlice).sendVia(dos);
                break;
            case ENEMY_NAME_RECEIVE:
                System.out.println("Thread num: " + Thread.currentThread().getId() + "  Received cmdAction: " + cmdAction);
                this.playerNameBob = cmd.getCmdInfoAsDIS().readUTF();
                this.status = ProtocolStatus.NAMES_EXCHANGED;
                this.wasNameExchanged = true;
                break;
            case COIN_INT_REQUEST:
                new Command(CommandAction.COIN_INT_RECEIVE).addToCmdInfo(coinTosser.getAliceCoin()).sendVia(os);
                break;
            case COIN_INT_RECEIVE:
                this.coinTosser.setBobCoin(cmd.getCmdInfoAsDIS().readInt());
                // todo: elevate Cointosser to its own public class.
                // todo: ask cointosser here if new toss is required because aliceCoin = bobCoin
                // then send new cointoss request from here, !!!throw new coin for alice!!!
                //
                break;
            case START_REQUEST:
                if(wasConnectionEstablished && wasNameExchanged && wasStarterDetermined){
                    this.status = ProtocolStatus.GAME_RUNNING;
                    new Command(CommandAction.START_CONFIRMED).sendVia(dos);
                }
                else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    processFatalError("Fatal Error reported" +
                            " to Enemy, because I couldn't start Game!");
                }
                break;
            case START_CONFIRMED:
                this.wasStartEnemyConfirmed = true;
                peObserver.receiveMsg("game started");
                break;
            case REQUEST_MOVE:
                // is game already done?
                if(this.isGameOver()){
                    peObserver.receiveMsg("Game Over. There are no winners!");
                }else if(this.isWinnerAlice()){
                    peObserver.receiveMsg("We won.");
                } else if(this.isWinnerBob()){
                    peObserver.receiveMsg(this.playerNameBob + " has won. You lost!");
                } else {
                    // no not done; get next move from alice and tell bob
                    peObserver.receiveMsg("Game still open!");
                    Position nextAliceMove = peObserver.promptForNextSet();
                    try {
                        this.gameEngine.set(nextAliceMove);
                        new Command(CommandAction.RECEIVE_MOVE, nextAliceMove.toByteArray()).sendVia(os);
                    } catch (BoardPositionNotFreeException | GameStatusNotYourTurnException | GameOverException | GameWonException e) {
                        e.printStackTrace();
                        processFatalError("Fatal error while setting your move." + nextAliceMove);
                    }
                }
                break;
            case RECEIVE_MOVE:
                // bob olready checked, if game was done, so not doing this here again
                try {
                    Position position = Position.readFrom(cmd.getCmdInfoAsDIS());
                    this.gameEngine.set(position);
                    new Command(CommandAction.MOVE_SUCCESSFULLY_SET).sendVia(os);
                    peObserver.receiveMsg(this.playerNameBob + " set position: " + position.toString());
                    peObserver.updatedBoard(gameEngine.getBoard());
                } catch (ProtocolEngineCMDReadException | BoardPositionNotFreeException | GameStatusNotYourTurnException | GameOverException | GameWonException e) {
                    e.printStackTrace();
                    new Command(CommandAction.FATAL_ERROR).sendVia(os);
                    throw new ProtocolEngineStatusException("Fatal Error reported" +
                            " to Enemy, enemies moves can't be read correctly!");
                }
                break;
            case MOVE_SUCCESSFULLY_SET:
                // wether game is done will be checked by request move again, so not doing this here
                new Command(CommandAction.REQUEST_MOVE).sendVia(os);
                peObserver.receiveMsg("Waiting for " + this.playerNameBob);
                break;
            case FATAL_ERROR:
                //todo: shut down whole f..ing program here
                processFatalError("Bob reported fatal error!");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + cmdAction);
        }
    }

    private boolean isGameOver(){
        return this.gameEngine.getStatus() == GameStatus.GAME_OVER;
    }

    private boolean isWinnerAlice(){
        return this.gameEngine.hasWon(gameEngine.getAlice().piece);
    }

    private boolean isWinnerBob(){
        return this.gameEngine.hasWon(gameEngine.getBob().piece);
    }

    @Override
    public ProtocolStatus getStatus() {
        return this.status;
    }


    @Override
    public boolean amIStarter() {
        return alice.piece == Piece.STARTER;
    }

    private void processFatalError(String s){
        try {
            new Command(CommandAction.FATAL_ERROR).sendVia(os);
            // todo: report fatal error to user
            // todo: close this thing down as good as possible, when exception while losing, exit(1)
            close();
        } catch (IOException e) {
            // probably things are pretty fucked up at this point
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    // todo:
    public void close() throws IOException {

    }

    /**
     *
     * @return alices name it got from PE Observer
     */
    private String askObserverForAliceName(){
        System.out.println("askObserverForAliceName was called");
        return this.playerNameAlice == null ? this.peObserver.promptName() : this.playerNameAlice;
    }

    @Override
    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    @Override
    public boolean isConnectionEstablished() {
        return wasConnectionEstablished;
    }

    @Override
    public boolean isNameExchanged() {
        return wasNameExchanged;
    }

    @Override
    public boolean isStarterDetermined() {
        return wasStarterDetermined;
    }

    @Override
    public boolean isStartEnemyConfirmed() {
        return wasStartEnemyConfirmed;
    }

    @Override
    public String getPlayerNameAlice() {
        return playerNameAlice;
    }

    @Override
    public String getPlayerNameBob() {
        return playerNameBob;
    }
}
