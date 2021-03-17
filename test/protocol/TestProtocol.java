package protocol;

import board.*;
import game.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestProtocol {
    private static final String HOSTNAME = "localhost";
    private static final int PORT_BASE_NR = 5555;
    private static int newPortCounter;

    private static final Piece starter = Piece.O;
    private static final Piece nonStarter = Piece.X;

    private final GameStatus firstTurn = starter == Piece.O ? GameStatus.TURN_O : GameStatus.TURN_X;
    private final GameStatus secondTurn = starter == Piece.O ? GameStatus.TURN_X : GameStatus.TURN_O;

    private static final String ALICE = "alice";
    private static final String BOB = "bob";

    @BeforeClass
    public static void initNewPortCounter(){
        newPortCounter = 0;
    }

//    @Before
//    public void incrementNewPortCounter(){
//        newPortCounter++;
//    }

    private ProtocolEngine getProtocolEngine(String name,
                                             MoveObserver moveObserver,
                                             RequestEnemyMoveObserver requestEnemyMoveObserver) {
        // todo: impl needs to now starter and nonstarter pieces
        return null;
    }

    private GameEngine getGameEngineAlice(){
        // todo: choose starting piece in constructor, same for both sides, set player name
        // assumption in tests O starts, set enemy player name
        Board board = new BoardImpl();
        return new GameEngineImpl(board, starter, "Alice", "Bob");
    }

    private GameEngine getGameEngineBob(){
        // todo: choose starting piece in constructor, same for both sides, set player name
        // assumption in tests O starts, set enemy player name
        Board board = new BoardImpl();
        return new GameEngineImpl(board, starter, "Alice", "Bob");
    }

    private int getUniquePort(){
        return PORT_BASE_NR + newPortCounter++;
    }

    private void waitForBob(GameEngine aliceGameEngine, Piece alicePiece) throws InterruptedException {
        while(!aliceGameEngine.isReadyForMove(alicePiece)){
            Thread.sleep(50);
        }
    }

    private class EnemyMoveObserver implements MoveObserver {
        public Position latestEnemyMove = null;

        @Override
        public void registerMove(Position position) throws BoardPositionNotFreeException, GameOverException {
            latestEnemyMove = position;
        }
    }

    private class EnemyMoveRequest implements RequestEnemyMoveObserver {
        public boolean moveRequested = false;
        @Override
        public void requestMove() {
            moveRequested = true;
        }

        public void setMoveRequested(boolean moveRequested) {
            this.moveRequested = moveRequested;
        }
    }


    @Test
    public void connectGoodrequestNameGood() throws ProtocolEngineNoConnectionException, ProtocolEngineResponseFormatException, PositionOutOfBoundException, GameStatusGameAlreadyStartedException, GameStatusNotYourTurnException, BoardPositionNotFreeException, InterruptedException, ProtocolEngineStatusException {
        MoveObserver enemyBobMoved = new EnemyMoveObserver();
        MoveObserver enemyAliceMoved = new EnemyMoveObserver();

        RequestEnemyMoveObserver enemyBobReady = new EnemyMoveRequest();
        RequestEnemyMoveObserver enemyAliceReady = new EnemyMoveRequest();

        ProtocolEngine aliceProtocol = getProtocolEngine(ALICE, enemyBobMoved, enemyBobReady);
        ProtocolEngine bobProtocol = getProtocolEngine(BOB, enemyAliceMoved, enemyAliceReady);

        Assert.assertEquals(ProtocolStatus.NOT_CONNECTED, aliceProtocol.getStatus());
        Assert.assertEquals(ProtocolStatus.NOT_CONNECTED, bobProtocol.getStatus());

        // connect; server first
        int port = getUniquePort();
        aliceProtocol.connect(port, true);
        bobProtocol.connect(port, false);

        Assert.assertEquals(ProtocolStatus.CONNECTED, aliceProtocol.getStatus());
        Assert.assertEquals(ProtocolStatus.CONNECTED, bobProtocol.getStatus());

        // request name enemy
        String bobName = aliceProtocol.requestNameEnemy();
        String aliceName = bobProtocol.requestNameEnemy();

        Assert.assertEquals(BOB, bobName);
        Assert.assertEquals(ALICE, aliceName);

        Assert.assertEquals(ProtocolStatus.NAMED, aliceProtocol.getStatus());
        Assert.assertEquals(ProtocolStatus.NAMED, bobProtocol.getStatus());

        // negotiate who starts
        boolean startAlice = aliceProtocol.amIStarter();
        boolean startBob = bobProtocol.amIStarter();

        Assert.assertTrue(startAlice ^ startBob);


        // todo: need alice game engine
        // use code from Game Engine Test to simulate game
        // feed the gameengine with observers and make private class here in test
        // that checkes weather it was called , with status flag
        // if the protocol engine was the central controlling piece of this program
        // you wouldn't have to do most of this shit!!!

        // todo: take the input and outputstreams from Protocol engine and
        // check
        GameEngine aliceGameEngine = getGameEngineAlice();

        EnemyMoveRequest aliceRequestFromBob = new EnemyMoveRequest();
        aliceGameEngine.addRequestEnemyMoveObserver(aliceRequestFromBob);

        EnemyMoveObserver aliceGEMovesToBob = new EnemyMoveObserver();
        aliceGameEngine.addMoveObservers(aliceGEMovesToBob);

        aliceGameEngine.pick(starter);
        Assert.assertEquals(firstTurn, aliceGameEngine.getStatus());

        GameEngine bobGameEngine = getGameEngineBob();
        bobGameEngine.pick(nonStarter);
        Assert.assertEquals(secondTurn, bobGameEngine.getStatus());

        /////////////////////////////////////////////////////////////////////////////////
        ///                         set
        /////////////////////////////////////////////////////////////////////////////////


        // Observers before any move
        Position firstAliceSet = new Position(0,0, starter);

        Assert.assertFalse(aliceRequestFromBob.moveRequested);
        Assert.assertFalse(aliceGameEngine.getRequestedEnemyMove());

        waitForBob(aliceGameEngine,starter);
        aliceGameEngine.set(firstAliceSet);

        // Observers have to be triggered
        Assert.assertEquals(0, aliceGEMovesToBob.latestEnemyMove.x);
        Assert.assertEquals(0, aliceGEMovesToBob.latestEnemyMove.y);
        Assert.assertEquals(starter, aliceGEMovesToBob.latestEnemyMove.piece);

        Assert.assertTrue(aliceRequestFromBob.moveRequested);
        // for the flag in game engine
        Assert.assertTrue(aliceGameEngine.getRequestedEnemyMove());

        // did the move make it to bob board
        Assert.assertEquals(starter, bobGameEngine.getBoard().getPieceAt(firstAliceSet));

        bobGameEngine.set(new Position(1,2, nonStarter));
        Assert.assertFalse(aliceGameEngine.getRequestedEnemyMove());

        waitForBob(aliceGameEngine,starter);
        aliceGameEngine.set(new Position(0,1, starter));
        Assert.assertTrue(aliceGameEngine.getRequestedEnemyMove());

        bobGameEngine.set(new Position(2,2, nonStarter));

        waitForBob(aliceGameEngine,starter);
        aliceGameEngine.set(new Position(0,2, starter));
        Assert.assertEquals(GameStatus.GAME_ENDED, aliceGameEngine.getStatus());
        Assert.assertTrue(aliceGameEngine.hasWon(starter));
        Assert.assertTrue(aliceGameEngine.hasWon(nonStarter));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Bobs Gameengine has to recognize that he lost
        Assert.assertEquals(GameStatus.GAME_ENDED, bobGameEngine.getStatus());
        Assert.assertFalse(bobGameEngine.hasWon(nonStarter));
        Assert.assertTrue(bobGameEngine.hasWon(starter));



        // todo: clean up
        // - threads
        // - sockets
        aliceProtocol.close();
        bobProtocol.close();
        // - etc.
    }

/*    @Test (expected = IllegalArgumentException.class)
    public void connectBadPort() throws ProtocolEngineNoConnectionException, ProtocolEngineResponseFormatException {
        ProtocolEngine aliceProtocol = getProtocolEngine(ALICE);

        aliceProtocol.connect(11);
    }*/

/*    @Test
    public void someFuckingName () throws ProtocolEngineNoConnectionException, ProtocolEngineResponseFormatException {
        ProtocolEngine aliceProtocol = getProtocolEngine(ALICE);
        ProtocolEngine bobProtocol = getProtocolEngine(BOB);

        Assert.assertEquals(ProtocolStatus.NOT_CONNECTED, aliceProtocol.getStatus());
        Assert.assertEquals(ProtocolStatus.NOT_CONNECTED, bobProtocol.getStatus());

        // yes, both Alice and Bob have to use connect, server first
        int port = getUniquePort();
        aliceProtocol.connect(port);
        bobProtocol.connect(port, HOSTNAME);

        Assert.assertEquals(ProtocolStatus.CONNECTED, aliceProtocol.getStatus());
        Assert.assertEquals(ProtocolStatus.CONNECTED, bobProtocol.getStatus());

        Assert.assertEquals(BOB, aliceProtocol.requestNameEnemy());
        Assert.assertEquals(ALICE, bobProtocol.requestNameEnemy());
    }*/
}
