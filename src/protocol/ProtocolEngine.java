package protocol;

public interface ProtocolEngine {
    /**
     * Engine has to be in status ProtocolStatus.NOT_CONNECTED, check with getStatus()
     * connect to peer server, you are a client
     * only relevant for first connection
     * @param port 4 digit integer
     * @param hostname not null
     * @throws ProtocolEngineNoConnectionException can't connect to peer
     * @throws IllegalArgumentException
     */
    void connect(int port, String hostname) throws ProtocolEngineNoConnectionException, IllegalArgumentException, ProtocolEngineStatusException;

    /**
     * Engine has to be in status ProtocolStatus.NOT_CONNECTED, check with getStatus()
     * connect to peer client, you are the server
     * only relevant for first connection
     * @param port 4 digit integer
     * @throws ProtocolEngineNoConnectionException
     * @throws IllegalArgumentException
     */
    void connect(int port) throws ProtocolEngineNoConnectionException, IllegalArgumentException, ProtocolEngineStatusException;

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
    boolean amIStarter() throws ProtocolEngineNoConnectionException;

    /**
     * Game has ended clean up
     */
    void close();
}
