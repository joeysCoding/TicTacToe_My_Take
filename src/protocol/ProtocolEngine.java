package protocol;

import game.GameEngine;

import java.io.IOException;

public interface ProtocolEngine {
    /**
     * Engine has to be in status ProtocolStatus.NOT_CONNECTED, check with getStatus()
     * connect to tcp peer
     * @param port 4 digit integer
     * @param asServer true if you are the server, false otherwise
     *                 only relevant for first connection after this peer to peer
     * @throws ProtocolEngineNoConnectionException can't connect to peer
     * @throws IllegalArgumentException
     */
    void connect(int port, boolean asServer) throws ProtocolEngineNoConnectionException, IllegalArgumentException, ProtocolEngineStatusException;

    /**
     * get current status
     * @return
     */
    ProtocolStatus getStatus();

    /**
     * Requests name from Enemy
     * @return enemy name
     * @throws ProtocolEngineNoConnectionException can't connect to peer
     * @throws ProtocolEngineResponseFormatException response violates protocol
     */
    String requestNameEnemy() throws  ProtocolEngineNoConnectionException, ProtocolEngineResponseFormatException;

    /**
     * negotiates the game starter
     * @return true - I start the game  false - Enemy starts game
     * @throws ProtocolEngineNoConnectionException
     */
    boolean amIStarter() throws ProtocolEngineNoConnectionException, IOException, ProtocolEngineNoEnemyCoinReceivedException;

    /**
     * Game has ended clean up
     */
    void close();

    /**
     * starts the game
     */
    void startGame(GameEngine gameEngine);
}
