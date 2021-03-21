package protocol;

import game.GameEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ProtocolEngine {
    /**
     * establishes connection with bob
     * (starts own Thread, ie PE run is executed
     * @param os
     * @param is
     */
    void handleConnection(OutputStream os, InputStream is);

    /**
     * get current status
     * @return
     */
    ProtocolStatus getStatus();


    /**
     * verify isStarterDetermined() first
     * @return true - I start the game  false - Enemy starts game
     * @throws ProtocolEngineNoConnectionException
     */
    boolean amIStarter();

    /**
     * Game has ended clean up
     */
    void close() throws IOException;

    /**
     * starts the game
     */
    void setGameEngine(GameEngine gameEngine);

    boolean isConnectionEstablished();

    boolean isNameExchanged();

    boolean isStarterDetermined();

    boolean isStartEnemyConfirmed();

    /**
     * check isNameExchanged first
     * @return
     */
    String getPlayerNameAlice();

    /**
     * check isNameExchanged first
     * @return
     */
    String getPlayerNameBob();
}
