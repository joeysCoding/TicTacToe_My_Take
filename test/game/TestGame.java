package game;

import board.*;
import org.junit.Assert;
import org.junit.Test;

public class TestGame {
    //
    // alice gets piece that start the game, so she starts the game
    Player alice = new Player("alice", Piece.STARTER, Side.ALICE);
    Player bob = new Player("bob", Piece.getOtherPiece(alice.piece), Side.BOB);

    private GameEngine getGameEngine(){
        // todo: choose starting piece in constructor, same for both sides, set player name
        // assumption in tests O starts, set enemy player name
        return new GameEngineImpl(alice, bob);
    }

    @Test
    public void setGood() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusGameAlreadyStartedException, GameStatusNotYourTurnException, GameOverException, GameWonException {
        GameEngine gameEngine = getGameEngine();

        gameEngine.set(new Position(2,2, alice));
        gameEngine.set(new Position(1,2, bob));
        gameEngine.set(new Position(0,2, alice));
    }

    @Test (expected = GameStatusNotYourTurnException.class)
    public void setBadNotYourTurn() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusGameAlreadyStartedException, GameStatusNotYourTurnException, GameOverException, GameWonException {
        GameEngine gameEngine = getGameEngine();

        gameEngine.set(new Position(2,2, alice));
        gameEngine.set(new Position(1,2, bob));
        gameEngine.set(new Position(0,2, bob));
    }

    @Test (expected = BoardPositionNotFreeException.class)
    public void setBadBoardPositionNotFree() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusGameAlreadyStartedException, GameStatusNotYourTurnException, GameOverException, GameWonException {
        GameEngine gameEngine = getGameEngine();

        gameEngine.set(new Position(2,2, alice));
        gameEngine.set(new Position(1,2, bob));
        gameEngine.set(new Position(2,2, alice));
    }

    @Test (expected = GameWonException.class)
    public void setBadGameAlreadyWon() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusGameAlreadyStartedException, GameStatusNotYourTurnException, GameOverException, GameWonException {
        GameEngine gameEngine = getGameEngine();

        gameEngine.set(new Position(0,0, alice));
        gameEngine.set(new Position(1,2, bob));
        gameEngine.set(new Position(0,1, alice));
        gameEngine.set(new Position(2,2, bob));
        gameEngine.set(new Position(0,2, alice));
        // game is already won by starter
        gameEngine.set(new Position(1,2, bob));
    }

    @Test
    public void hasWonGood() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusGameAlreadyStartedException, GameStatusNotYourTurnException, GameOverException, GameWonException {
        GameEngine gameEngine = getGameEngine();
        Assert.assertEquals(GameStatus.GAMING, gameEngine.getStatus());
        Assert.assertEquals(alice.piece, gameEngine.getTurn());

        gameEngine.set(new Position(0,0, alice));
        Assert.assertEquals(bob.piece, gameEngine.getTurn());

        gameEngine.set(new Position(1,2, bob));
        Assert.assertEquals(alice.piece, gameEngine.getTurn());

        gameEngine.set(new Position(0,1, alice));
        Assert.assertEquals(bob.piece, gameEngine.getTurn());

        gameEngine.set(new Position(2,2, bob));
        Assert.assertEquals(alice.piece, gameEngine.getTurn());

        gameEngine.set(new Position(0,2, alice));
        Assert.assertEquals(GameStatus.GAME_WON, gameEngine.getStatus());
        Assert.assertNull(gameEngine.getTurn());

        Assert.assertTrue(gameEngine.hasWon(alice.piece));
        Assert.assertFalse(gameEngine.hasWon(bob.piece));
    }

    // todo: test for game over

    @Test (expected = GameOverException.class)
    public void gameOverGoodBad() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusNotYourTurnException, GameOverException, GameWonException {
        GameEngine gameEngine = getGameEngine();

        gameEngine.set(new Position(0,1, alice));
        gameEngine.set(new Position(0,0, bob));
        gameEngine.set(new Position(0,2, alice));
        gameEngine.set(new Position(1,2, bob));
        gameEngine.set(new Position(1,0, alice));
        gameEngine.set(new Position(1,1, bob));
        gameEngine.set(new Position(2,1, alice));
        gameEngine.set(new Position(2,0, bob));
        gameEngine.set(new Position(2,2, alice));

        Assert.assertEquals(GameStatus.GAME_OVER, gameEngine.getStatus());

        gameEngine.set(new Position(1,0, bob));

    }
}
