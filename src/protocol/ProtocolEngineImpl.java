package protocol;

import board.BoardPositionNotFreeException;
import board.Piece;
import board.Position;
import command.CommandAction;
import game.GameEngine;
import game.GameStatus;
import game.GameStatusNotYourTurnException;
import network.TCPStream;
import network.TCPStreamCreatedListener;
import userInterface.UICantGetNextMoveFromUser;
import utils.Converter;

import java.io.*;
import java.util.List;
import java.util.Random;


public class ProtocolEngineImpl implements ProtocolEngine{
    private final PEObserver peObserver;
    private GameEngine gameEngine;
    ProtocolStatus status;

    boolean enemyRequestedMove;
    // status check flags
    boolean wasConnectionEstablished;
    boolean wasNameExchanged;
    boolean wasStarterDetermined;
    boolean wasStartEnemyConfirmed;

    private final String playerNameUS;
    private String playerNameEnemy;


    private CoinTosser coinTosser;

    List<TCPStreamCreatedListener> streamCreatedListeners;


    TCPStream tcpStream;
    Thread tcpStreamThread;
    private InputStream is;
    private DataInputStream dis;
    private OutputStream os;
    private DataOutputStream dos;

    public ProtocolEngineImpl(String playerNameUS, PEObserver peObserver) {
        this.playerNameUS = playerNameUS;
        this.status = ProtocolStatus.NOT_CONNECTED;
        streamCreatedListeners.add(new StreamCreated());
        coinTosser = new CoinTosser();
        this.peObserver = peObserver;
    }

    /**
     * converts integer to action,
     * use to convert received command integers to command action enum
     * @param cmdNum has to be a viable ordinal of Command Actions values
     * @return
     */
    private CommandAction numToCmd(int cmdNum){
        return CommandAction.intToCmd(cmdNum);
    }

    private int cmdToNum(CommandAction cmd){
        return cmd.getOrdinal();
    }

    private class StreamCreated implements TCPStreamCreatedListener{

        @Override
        public void streamCreated(TCPStream channel) {

        }
    }

    private class CoinTosser{
        private int us = 0;
        private int enemy = 0;

        private boolean alreadyDetermined = false;
        private boolean alreadyDeterminedWeStart;

        public CoinTosser(){
            this.us = this.generateRandom();
        }

        public void requestEnemyCoin() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (enemy == 0) {
                        try {
                            dos.writeInt(cmdToNum(CommandAction.COIN_INT_REQUEST));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        public void setEnemy(int receivedEnemyCoin){
            this.enemy = receivedEnemyCoin;
        }

        private int generateRandom(){
            long seed = System.currentTimeMillis() + Thread.currentThread().getId();
            Random random = new Random(seed);
            int randomNum = 1 + random.nextInt(10000);
            return randomNum;
        }

        public boolean doWeStart() throws IOException, ProtocolEngineNoEnemyCoinReceivedException {
            if (alreadyDetermined) return alreadyDeterminedWeStart;
            if (enemy == 0) {
                // give enemy second chance to send coin
                requestEnemyCoin();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (enemy == 0){
                // enemy didn't use second chance
                throw new ProtocolEngineNoEnemyCoinReceivedException("Enemy didn't" +
                        " give me a coin value");
            }
            if (this.us == this.enemy){
                this.us = this.generateRandom();
                this.requestEnemyCoin();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return this.doWeStart();
            }

            this.alreadyDeterminedWeStart = this.us > this.enemy;
            this.alreadyDetermined = true;

            return alreadyDeterminedWeStart;
        }
    }

    private void sendCmd(byte[] cmdBytes){

    }

    // todo
    private int generateCoinInt(){
        return 0;
    }

    private void processNextEnemyCommand() throws IOException, ProtocolEngineNoCommandInStreamException,
            ProtocolEngineStatusException, UICantGetNextMoveFromUser {
        if(dis.available() == 0)
            throw new ProtocolEngineNoCommandInStreamException("trying to read command from Sttream: but not availaba");
        int cmdNum = this.dis.readInt();
        CommandAction cmd = numToCmd(cmdNum);

        switch (cmd){
            case ENEMY_NAME_REQUEST:
                sendCommand(CommandAction.ENEMY_NAME_RECEIVE,
                        Converter.writeUTF(this.playerNameEnemy));
                break;
            case ENEMY_NAME_RECEIVE:
                this.playerNameEnemy = dis.readUTF();
                break;
            case COIN_INT_REQUEST:
                sendCommand(CommandAction.COIN_INT_RECEIVE,
                        Converter.convert(generateCoinInt()));
                break;
            case COIN_INT_RECEIVE:
                this.coinTosser.setEnemy(dis.readInt());
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
                    sendCommand(CommandAction.FATAL_ERROR);
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
                    peObserver.receiveMsg(this.playerNameEnemy + " has won. You lost!");
                } else {
                    // no not done; get next move from alice and tell bob
                    peObserver.receiveMsg("Game still open!");
                    Position nextAliceMove = peObserver.promptForNextSet();
                    try {
                        this.gameEngine.set(nextAliceMove);
                        byte[] positionBytes = nextAliceMove.toByteArray();
                        this.sendCommand(CommandAction.RECEIVE_MOVE, positionBytes);
                    } catch (BoardPositionNotFreeException e) {
                        e.printStackTrace();
                    } catch (GameStatusNotYourTurnException e) {
                        e.printStackTrace();
                        peObserver.receiveMsg("Fatal error while setting your move." + nextAliceMove);
                        dos.writeInt(this.cmdToNum(CommandAction.FATAL_ERROR));
                        this.close();
                    }
                }
                break;
            case RECEIVE_MOVE:
                // bob olready checked, if game was done, so not doing this here again
                try {
                    Position position = Position.readFrom(this.dis);
                    this.gameEngine.set(position);
                    this.sendCommand(CommandAction.MOVE_SUCCESSFULLY_SET);
                    peObserver.receiveMsg(this.playerNameEnemy + " set position: " + position.toString());
                    peObserver.updatedBoard(gameEngine.getBoard());
                } catch (ProtocolEngineCMDReadException | BoardPositionNotFreeException | GameStatusNotYourTurnException e) {
                    e.printStackTrace();
                    sendCommand(CommandAction.FATAL_ERROR);
                    throw new ProtocolEngineStatusException("Fatal Error reported" +
                            " to Enemy, enemies moves can't be read correctly!");
                }
                break;
            case MOVE_SUCCESSFULLY_SET:
                // wether game is done will be checked by request move again, so not doing this here
                sendCommand(CommandAction.REQUEST_MOVE);
                peObserver.receiveMsg("Waiting for " + this.playerNameEnemy);
                break;

            case FATAL_ERROR:
                //todo: shut down whole f..ing program here
                peObserver.receiveMsg("Fatal error encountered. GameOver here!!!");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + cmd);
        }
    }
    // todo: replace with new CMD ... send
    private void sendCommand(CommandAction cmd, byte[] cmdInfo) throws IOException {
        int cmdTotalLength = (cmdInfo == null ? 0 : cmdInfo.length) + 2;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream localDos = new DataOutputStream(baos);
        localDos.writeInt(cmdTotalLength);
        int cmdInt = cmd.getOrdinal();
        localDos.writeInt(cmdInt);
        if(cmdInfo != null)
            localDos.write(cmdInfo);
        this.dos.write(baos.toByteArray());
    }

    // todo: replace with new CMD ... send
    private void sendCommand(CommandAction cmd) throws IOException{
        sendCommand(cmd, null);
    }

    private boolean isGameOver(){
        return this.gameEngine.getStatus() == GameStatus.GAME_OVER;
    }

    private boolean isWinnerUS(){
        Piece pieceUS = this.gameEngine.getPieceUS();
        return this.gameEngine.hasWon(pieceUS);
    }

    private boolean isWinnerEnemy(){
        Piece pieceEnemy = this.gameEngine.getPieceEnemy();
        return this.gameEngine.hasWon(pieceEnemy);
    }

    private Position readPositionFrom(DataInputStream dis) throws IOException, ProtocolEngineCMDReadException {
            return null;
    }



    @Override
    public void connect(int port, boolean asServer)
            throws ProtocolEngineStatusException,
            ProtocolEngineNoConnectionException,
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
    public String requestNameEnemy() throws ProtocolEngineNoConnectionException, ProtocolEngineResponseFormatException {
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
            this.coinTosser.requestEnemyCoin();
            try {
                Thread.sleep(100);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            return coinTosser.doWeStart();
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void startGame(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }


//
//    @Override
//    public void registerMove(Position position) throws BoardPositionNotFreeException, GameOverException {
//
//    }
//
//    @Override
//    public void requestMove() {
//
//    }

    private void addStreamCreatedListener(TCPStreamCreatedListener listener){
        if(!streamCreatedListeners.contains(listener))
            streamCreatedListeners.add(listener);

    }

}
