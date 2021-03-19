package protocol;

import board.BoardPositionNotFreeException;
import board.Piece;
import board.Position;
import command.Command;
import command.CommandAction;
import game.GameEngine;
import game.GameStatus;
import game.GameStatusNotYourTurnException;
import game.Player;
import network.TCPStream;
import network.TCPStreamCreatedListener;
import userInterface.UICantGetNextMoveFromUser;

import java.io.*;
import java.util.List;
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
 * 5. call startGame(Alice's GameEngine) // game starts
 * Alices input are handled by the user interface, PE only calls PE Listeners
 * if input is required.
 * The game ends when one of the players has won, or all gameboard positions are
 * set. No further action required.
 */
public class ProtocolEngineImpl implements ProtocolEngine{
    private final PEObserver peObserver;
    private GameEngine gameEngine;
    ProtocolStatus status;
    // status check flags
    boolean wasConnectionEstablished;
    boolean wasNameExchanged;
    boolean wasStarterDetermined;
    boolean wasStartEnemyConfirmed;
    // naming convention:
    private Player alice;
    private Player bob;
    private final String playerNameAlice; // alice is our side, us ....
    private String playerNameBob; // bob stands for enemy, other side ....

    private CoinTosser coinTosser;
    private final Piece tossWinner = Piece.X;
    private final Piece tossLoser = Piece.O;


    List<TCPStreamCreatedListener> streamCreatedListeners;


    TCPStream tcpStream;
    private InputStream is;
    private DataInputStream dis;
    private OutputStream os;
    private DataOutputStream dos;

    public ProtocolEngineImpl(String playerNameAlice, PEObserver peObserver) {
        this.playerNameAlice = playerNameAlice;
        this.status = ProtocolStatus.NOT_CONNECTED;
        streamCreatedListeners.add(new StreamCreated());
        coinTosser = new CoinTosser();
        this.peObserver = peObserver;
    }

    private class StreamCreated implements TCPStreamCreatedListener{

        @Override
        public void streamCreated(TCPStream channel) {

        }
    }

    private class CoinTosser{
        private int alice = 0;
        private int bob = 0;

        private boolean alreadyDetermined = false;
        private boolean aliceStart;

        public CoinTosser(){
            this.alice = this.generateRandom();
        }

        public void requestBobCoin() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (bob == 0) {
                        try {
                            new Command(CommandAction.COIN_INT_REQUEST).sendVia(os);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        public int getAliceCoin() {
            return alice;
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

        public boolean doWeStart() throws IOException, ProtocolEngineNoEnemyCoinReceivedException {
            if (alreadyDetermined) return aliceStart;
            if (bob == 0) {
                // give enemy second chance to send coin
                requestBobCoin();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (bob == 0){
                // enemy didn't use second chance
                throw new ProtocolEngineNoEnemyCoinReceivedException("Enemy didn't" +
                        " give me a coin value");
            }
            if (this.alice == this.bob){
                this.alice = this.generateRandom();
                this.requestBobCoin();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return this.doWeStart();
            }

            this.aliceStart = this.alice > this.bob;
            this.alreadyDetermined = true;

            return aliceStart;
        }
    }

    private void processNextEnemyCommand() throws IOException, ProtocolEngineNoCommandInStreamException,
            ProtocolEngineStatusException, UICantGetNextMoveFromUser {
        if(dis.available() == 0)
            throw new ProtocolEngineNoCommandInStreamException("trying to read command from Sttream: but not availaba");
        Command cmd = Command.readCMDFrom(is);
        CommandAction cmdAction = cmd.getCmdAction();

        switch (cmdAction){
            case ENEMY_NAME_REQUEST:
                new Command(CommandAction.ENEMY_NAME_RECEIVE).addToCmdInfoAsUTF(this.playerNameBob).sendVia(os);
                break;
            case ENEMY_NAME_RECEIVE:
                this.playerNameBob = cmd.getCmdInfoAsDIS().readUTF();
                break;
            case COIN_INT_REQUEST:
                new Command(CommandAction.COIN_INT_RECEIVE).addToCmdInfo(coinTosser.getAliceCoin()).sendVia(os);
                break;
            case COIN_INT_RECEIVE:
                this.coinTosser.setBobCoin(cmd.getCmdInfoAsDIS().readInt());
                break;
            case START_REQUEST:
                if(wasConnectionEstablished && wasNameExchanged && wasStarterDetermined)
                    this.status = ProtocolStatus.GAME_STARTED;
                else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    new Command(CommandAction.FATAL_ERROR).sendVia(os);
                    throw new ProtocolEngineStatusException("Fatal Error reported" +
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
                }else if(this.isWinnerUS()){
                    peObserver.receiveMsg("We won.");
                } else if(this.isWinnerEnemy()){
                    peObserver.receiveMsg(this.playerNameBob + " has won. You lost!");
                } else {
                    // no not done; get next move from alice and tell bob
                    peObserver.receiveMsg("Game still open!");
                    Position nextAliceMove = peObserver.promptForNextSet();
                    try {
                        this.gameEngine.set(nextAliceMove);
                        new Command(CommandAction.RECEIVE_MOVE, nextAliceMove.toByteArray()).sendVia(os);
                    } catch (BoardPositionNotFreeException e) {
                        e.printStackTrace();
                    } catch (GameStatusNotYourTurnException e) {
                        e.printStackTrace();
                        peObserver.receiveMsg("Fatal error while setting your move." + nextAliceMove);
                        new Command(CommandAction.FATAL_ERROR).sendVia(os);
                        this.close();
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
                } catch (ProtocolEngineCMDReadException | BoardPositionNotFreeException | GameStatusNotYourTurnException e) {
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
                peObserver.receiveMsg("Fatal error encountered. GameOver here!!!");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + cmdAction);
        }
    }

    private boolean isGameOver(){
        return this.gameEngine.getStatus() == GameStatus.GAME_OVER;
    }

    private boolean isWinnerUS(){
        Piece pieceUS = this.gameEngine.getPieceAlice();
        return this.gameEngine.hasWon(pieceUS);
    }

    private boolean isWinnerEnemy(){
        Piece pieceEnemy = this.gameEngine.getPieceBob();
        return this.gameEngine.hasWon(pieceEnemy);
    }

    @Override
    public void connect(int port, boolean asServer)
            throws ProtocolEngineStatusException,
            IllegalArgumentException {
        if(this.status != ProtocolStatus.NOT_CONNECTED)
            throw new ProtocolEngineStatusException("trying to establish new connection, " +
                    "but protocol Engine not it NOT_CONNECTED status. actual status: " + this.status);

        if(port < 1000 || port > 9999)
            throw new IllegalArgumentException("port has to be 4 digit integer. But is: " + port);

        this.tcpStream = new TCPStream(port, asServer, gameEngine.getName());
        try {
            tcpStream.waitForConnection();
            tcpStream.checkConnected();
            os = tcpStream.getOutputStream();
            dos = new DataOutputStream(os);
            is = tcpStream.getInputStream();
            dis = new DataInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.status = ProtocolStatus.CONNECTED;
        this.informStreamCreatedListeners(tcpStream);
    }

    private void informStreamCreatedListeners(TCPStream createdTcpStream) {
        for(TCPStreamCreatedListener listener: streamCreatedListeners){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    listener.streamCreated(tcpStream);
                }
            }).start();
        }
    }


    @Override
    public ProtocolStatus getStatus() {
        return this.status;
    }

    @Override
    public String requestNameBob() throws ProtocolEngineNoConnectionException, ProtocolEngineResponseFormatException {
        return null;
    }

    @Override
    public boolean amIStarter() throws ProtocolEngineNoConnectionException, IOException, ProtocolEngineNoEnemyCoinReceivedException {
        if(!(this.status == ProtocolStatus.CONNECTED || this.status == ProtocolStatus.NAMED))
            throw new ProtocolEngineNoConnectionException("trying to toss coin," +
                    " but not connected to enemy!");
        try {
            return coinTosser.doWeStart();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ProtocolEngineNoConnectionException();
        } catch (ProtocolEngineNoEnemyCoinReceivedException e){
            e.printStackTrace();
            // give it another try
            this.coinTosser = new CoinTosser();
            this.coinTosser.requestBobCoin();
            try {
                Thread.sleep(100);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            return coinTosser.doWeStart();
        }
    }

    @Override
    // todo:
    public void close() {

    }

    @Override
    public void startGame(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    private void addStreamCreatedListener(TCPStreamCreatedListener listener){
        if(!streamCreatedListeners.contains(listener))
            streamCreatedListeners.add(listener);

    }

}
