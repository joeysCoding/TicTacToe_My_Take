package command;

/**
 * don't use magic numbers to refer to this actions, use ordinance()
 */
public enum CommandAction {
    REQUEST_CONNECTION_CONFIRMANTION,
    RECEIVE_CONNECTION_CONFIRMED,

    REQUEST_NAME,
    ENEMY_NAME_RECEIVE,

    COIN_INT_REQUEST,
    COIN_INT_RECEIVE,

    START_REQUEST,
    START_CONFIRMED,

    REQUEST_MOVE,
    RECEIVE_MOVE,
    MOVE_SUCCESSFULLY_SET,

    FATAL_ERROR;


    public static int getUpperOrdinal(){
        return CommandAction.values().length - 1;
    }

    public static int getLowerOrdinal(){
        return 0;
    }

    public int getOrdinal(){
        return this.ordinal();
    }

    /**
     *
     * @param cmdOrdinal has to be between 0 and getUpperOrdinal()
     * @exception IllegalArgumentException thrown when trying to call with wrong ordinal
     * @return
     */
    public static CommandAction intToCmd(int cmdOrdinal) throws IllegalArgumentException{
        for(CommandAction cmd: CommandAction.values())
            if(cmd.ordinal() == cmdOrdinal)
                return cmd;
            throw new IllegalArgumentException("cmdOrdinal has to be between 0 and"
                    + getUpperOrdinal() + " but was: " + cmdOrdinal );
    }
}
