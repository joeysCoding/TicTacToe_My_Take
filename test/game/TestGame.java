package game;

import board.*;
import org.junit.Assert;
import org.junit.Test;

public class TestGame {
    private final Piece starter = Piece.O;
    private final Piece nonStarter = Piece.X;
    private final GameStatus firstTurn = starter == Piece.O ? GameStatus.TURN_O : GameStatus.TURN_X;
    private final GameStatus secondTurn = starter == Piece.O ? GameStatus.TURN_X : GameStatus.TURN_O;


    private GameEngine getGameEngine(){
        // todo: choose starting piece in constructor, same for both sides, set player name
        // assumption in tests O starts, set enemy player name
        Board board = new BoardImpl();
        return new GameEngineImpl(board, starter, "Alice", "Bob");
    }

    @Test
    public void pickGood() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusNotYourTurnException, GameStatusGameAlreadyStartedException, GameOverException {
        GameEngine gameEngine = getGameEngine();
        gameEngine.pick(starter);
        gameEngine.set(new Position(2,2, starter));
    }

    @Test (expected = GameStatusGameAlreadyStartedException.class)
    public void pickBad() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusNotYourTurnException, GameStatusGameAlreadyStartedException, GameOverException {
        GameEngine gameEngine = getGameEngine();
        gameEngine.pick(starter);
        gameEngine.set(new Position(2,2, Piece.O));
        gameEngine.pick(nonStarter);
    }

    @Test
    public void setGood() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusGameAlreadyStartedException, GameStatusNotYourTurnException, GameOverException {
        GameEngine gameEngine = getGameEngine();
        gameEngine.pick(nonStarter);

        gameEngine.set(new Position(2,2, starter));
        gameEngine.set(new Position(1,2, nonStarter));
        gameEngine.set(new Position(0,2, starter));
    }

    @Test (expected = GameStatusNotYourTurnException.class)
    public void setBadGameNotStarted() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusGameAlreadyStartedException, GameStatusNotYourTurnException, GameOverException {
        GameEngine gameEngine = getGameEngine();

        gameEngine.set(new Position(2,2, starter));
    }

    @Test (expected = GameStatusNotYourTurnException.class)
    public void setBadNotYourTurn() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusGameAlreadyStartedException, GameStatusNotYourTurnException, GameOverException {
        GameEngine gameEngine = getGameEngine();
        gameEngine.pick(nonStarter);

        gameEngine.set(new Position(2,2, starter));
        gameEngine.set(new Position(1,2, nonStarter));
        gameEngine.set(new Position(0,2, nonStarter));
    }

    @Test (expected = BoardPositionNotFreeException.class)
    public void setBadBoardPositionNotFree() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusGameAlreadyStartedException, GameStatusNotYourTurnException, GameOverException {
        GameEngine gameEngine = getGameEngine();
        gameEngine.pick(nonStarter);

        gameEngine.set(new Position(2,2, starter));
        gameEngine.set(new Position(1,2, nonStarter));
        gameEngine.set(new Position(2,2, starter));
    }

    @Test (expected = GameStatusNotYourTurnException.class)
    public void setBadGameAlreadyWon() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusGameAlreadyStartedException, GameStatusNotYourTurnException, GameOverException {
        GameEngine gameEngine = getGameEngine();
        gameEngine.pick(nonStarter);

        gameEngine.set(new Position(0,0, starter));
        gameEngine.set(new Position(1,2, nonStarter));
        gameEngine.set(new Position(0,1, starter));
        gameEngine.set(new Position(2,2, nonStarter));
        gameEngine.set(new Position(0,2, starter));
        // game is already won by starter
        gameEngine.set(new Position(1,2, nonStarter));
    }

    @Test
    public void hasWonGood() throws PositionOutOfBoundException, BoardPositionNotFreeException, GameStatusGameAlreadyStartedException, GameStatusNotYourTurnException, GameOverException {
        GameEngine gameEngine = getGameEngine();
        Assert.assertEquals(GameStatus.WAITING_FOR_PICK, gameEngine.getStatus());

        gameEngine.pick(nonStarter);
        Assert.assertEquals(firstTurn, gameEngine.getStatus());

        gameEngine.set(new Position(0,0, starter));
        Assert.assertEquals(secondTurn, gameEngine.getStatus());

        gameEngine.set(new Position(1,2, nonStarter));
        Assert.assertEquals(firstTurn, gameEngine.getStatus());

        gameEngine.set(new Position(0,1, starter));
        Assert.assertEquals(secondTurn, gameEngine.getStatus());

        gameEngine.set(new Position(2,2, nonStarter));
        Assert.assertEquals(firstTurn, gameEngine.getStatus());

        gameEngine.set(new Position(0,2, starter));
        Assert.assertEquals(GameStatus.GAME_WON, gameEngine.getStatus());

        Assert.assertTrue(gameEngine.hasWon(starter));
        Assert.assertFalse(gameEngine.hasWon(nonStarter));
    }
}
