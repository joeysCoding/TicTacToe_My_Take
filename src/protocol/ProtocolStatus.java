package protocol;

public enum ProtocolStatus {
    NO_CONNECTION_CONFIRMED,
    CONNECTED,
    NAMES_EXCHANGED,
    STARTER_DETERMINED,
    GAME_RUNNING,
    GAME_ENDED,
    FATAL;
}
