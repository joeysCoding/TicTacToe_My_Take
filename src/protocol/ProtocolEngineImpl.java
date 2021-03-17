package protocol;

import board.BoardPositionNotFreeException;
import board.Position;
import game.GameEngine;
import game.GameOverException;
import network.TCPStream;
import network.TCPStreamCreatedListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class ProtocolEngineImpl implements ProtocolEngine, RequestEnemyMoveObserver, MoveObserver {
    private final GameEngine gameEngine;
    ProtocolStatus status;

    List<TCPStreamCreatedListener> streamCreatedListeners;


    TCPStream tcpStream;
    Thread tcpStreamThread;
    private InputStream is;
    private OutputStream os;

    public ProtocolEngineImpl(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        this.status = ProtocolStatus.NOT_CONNECTED;
        streamCreatedListeners.add(new StreamCreated());
    }

    private class StreamCreated implements TCPStreamCreatedListener{

        @Override
        public void streamCreated(TCPStream channel) {

        }
    }

    @Override
    public void connect(int port, String hostname)
            throws ProtocolEngineStatusException,
            ProtocolEngineNoConnectionException,
            IllegalArgumentException {
        if(hostname == null)
            throw new IllegalArgumentException("you are trying to set up a client. but hostname is null!");
        buildConnection(port, hostname);
    }

    @Override
    public void connect(int port) throws
            ProtocolEngineStatusException,
            ProtocolEngineNoConnectionException,
            IllegalArgumentException {
        buildConnection(port, null);
    }

    private void buildConnection(int port, String hostname) throws
            ProtocolEngineStatusException,
            ProtocolEngineNoConnectionException,
            IllegalArgumentException{
        if(port < 1000 || port > 9999)
            throw new IllegalArgumentException("port has to be 4 digit integer. But is: " + port);
        boolean asServer
    }

    @Override
    public ProtocolStatus getStatus() {
        return null;
    }

    @Override
    public String requestNameEnemy() throws ProtocolEngineNoConnectionException, ProtocolEngineResponseFormatException {
        return null;
    }

    @Override
    public boolean amIStarter() throws ProtocolEngineNoConnectionException {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public void registerMove(Position position) throws BoardPositionNotFreeException, GameOverException {

    }

    @Override
    public void requestMove() {

    }

    private void addStreamCreatedListener(TCPStreamCreatedListener listener){
        if(!streamCreatedListeners.contains(listener))
            streamCreatedListeners.add(listener);
    }
}
