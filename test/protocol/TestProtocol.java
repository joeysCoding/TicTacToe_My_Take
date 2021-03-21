package protocol;

import board.*;
import game.*;
import network.TCPStream;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import userInterface.UICantGetNextMoveFromUser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TestProtocol {
    private static final String HOSTNAME = "localhost";
    private static final int PORT_BASE_NR = 5555;
    private static int newPortCounter;
    private static long waitTimeMillis = 500;

    String aliceName = "aliceson";
    String bobName = "boby";

    @BeforeClass
    public static void initNewPortCounter() {
        newPortCounter = 0;
    }

    private ProtocolEngine getProtocolEngine(PEObserver playerUI) throws ProtocolEngineNoConnectionException {
        return new ProtocolEngineImpl(playerUI);

    }

    private class FakeUI implements PEObserver{
        public String userName;
        public boolean wasPromptedUserName = false;
        public List<Position> userMoves;

        public String lastMsg;
        public boolean wasUpdatedBoard = false;

        public FakeUI(String userName){
            this(userName, null);
        }

        public FakeUI(String userName, List<Position> userMoves){
            this.userName = userName;
            if(userMoves == null)
                this.userMoves = new LinkedList<>();
            else
                this.userMoves = userMoves;

        }

        /**
         *
         * @param move add to end of list, this position will be moved to last
         */
        public void addUserMove(Position move){
            userMoves.add(move);
        }


        @Override
        public Position promptForNextSet() throws UICantGetNextMoveFromUser {
            if(userMoves == null)
                throw new UICantGetNextMoveFromUser();
            return userMoves.remove(0);
        }

        @Override
        public void updatedBoard(Board board) {

        }

        @Override
        public void receiveMsg(String msg) {
            this.lastMsg = msg;
            System.out.println(this.userName + " UI received msg: " + msg);
        }

        @Override
        public String promptName() {
            this.wasPromptedUserName = true;
            return userName;
        }
    }

    private GameEngine getGameEngineAlice(Player alice, Player bob) {
        return new GameEngineImpl(alice, bob);
    }

    private GameEngine getGameEngineBob(Player alice, Player bob) {
        return new GameEngineImpl(bob, alice);
    }

    private int getUniquePort() {
        return PORT_BASE_NR + newPortCounter++;
    }

    @Test
    public void connectAndRequestBobNameGood()
            throws ProtocolEngineNoConnectionException,
            ProtocolEngineResponseFormatException,
            IOException, InterruptedException, ProtocolEngineNoEnemyCoinReceivedException, PositionOutOfBoundException {
        FakeUI uiAliceImpl = new FakeUI(aliceName);
        PEObserver uiAlice = uiAliceImpl;
        FakeUI uiBobImpl = new FakeUI(bobName);
        PEObserver uiBob = uiBobImpl;

        int port = getUniquePort();
        TCPStream streamAlice = new TCPStream(port, true, "aliceStream");
        TCPStream streamBob = new TCPStream(port, false, "bobStream");

        streamAlice.start(); streamBob.start();
        streamAlice.waitForConnection(); streamBob.waitForConnection();


        ProtocolEngine peAlice = getProtocolEngine(uiAlice);
        ProtocolEngine peBob = getProtocolEngine(uiBob);

        // confirm connection - run started in pe

        peAlice.handleConnection(streamAlice.getOutputStream(), streamAlice.getInputStream());
        peBob.handleConnection(streamBob.getOutputStream(), streamBob.getInputStream());

       Thread.sleep(waitTimeMillis);

        Assert.assertTrue(peAlice.isConnectionEstablished());
        Assert.assertTrue(peBob.isConnectionEstablished());

        // confirm name exchange

        Thread.sleep(waitTimeMillis);


        Assert.assertTrue(uiAliceImpl.wasPromptedUserName);
        Assert.assertTrue(peAlice.isNameExchanged());
        Assert.assertEquals(bobName, peAlice.getPlayerNameBob());

        Assert.assertTrue(uiBobImpl.wasPromptedUserName);
        Assert.assertTrue(peBob.isNameExchanged());
        // peBob.getPlayerNameBob() stands for get other player name
        Assert.assertEquals(aliceName, peBob.getPlayerNameBob());

        while(!(peAlice.isStarterDetermined() && peBob.isStarterDetermined())){
            Thread.sleep(waitTimeMillis);
        }

        // who starts the game
        Assert.assertTrue(peAlice.isStarterDetermined());
        Assert.assertTrue(peAlice.amIStarter() ^ peBob.amIStarter());

        ///////////////////////////////////////////////////////////////
        //                  Starting the game
        ///////////////////////////////////////////////////////////////
        Piece alicePiece = peAlice.amIStarter() ?
                Piece.STARTER : Piece.getOtherPiece(Piece.STARTER);
        Player alice = new Player(aliceName, alicePiece, Side.ALICE);
        Piece bobPiece = Piece.getOtherPiece(alice.piece);
        Player bob = new Player(bobName, bobPiece, Side.BOB);

        uiAliceImpl.addUserMove(new Position(0,0,alice.piece));
        uiAliceImpl.addUserMove(new Position(1,1,alice.piece));
        uiAliceImpl.addUserMove(new Position(2,2,alice.piece));

        uiBobImpl.addUserMove(new Position(0,1, bob.piece));
        uiBobImpl.addUserMove(new Position(2,1, bob.piece));
        uiBobImpl.addUserMove(new Position(1,0, bob.piece));







    }
}

