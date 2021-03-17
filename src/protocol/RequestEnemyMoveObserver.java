package protocol;

public interface RequestEnemyMoveObserver {
    /**
     * notifies enemy that you currently are not doing something with your game board
     */
    void requestMove();
}
