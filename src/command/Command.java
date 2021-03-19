package command;

import java.io.*;

public class Command {
    private final CommandAction cmdAction;
    private byte[] cmdInfo;
    private int cmdTotalLength;
    private byte[] entireCmd;


    public Command(CommandAction cmdAction, byte[] cmdInfo) {
        this.cmdAction = cmdAction;
        this.cmdInfo = cmdInfo;
        setCmdTotalLength();
        setEntireCmd();
    }

    public Command(CommandAction cmdAction) {
        this(cmdAction, null);
    }

    public CommandAction getCmdAction() {
        return cmdAction;
    }

    public byte[] getCmdInfo() {
        return cmdInfo;
    }

    private void setEntireCmd() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(cmdTotalLength);
            int cmdInt = getCmdInt(cmdAction);
            dos.writeInt(cmdInt);
            if (cmdInfo != null)
                dos.write(cmdInfo);
            entireCmd = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private int getCmdInt(CommandAction toConvert){
        return toConvert.getOrdinal();
    }

    private void setCmdTotalLength() {
        this.cmdTotalLength = (cmdInfo == null ? 0 : cmdInfo.length) + 2;
    }


    public static Command readCMDFrom(InputStream is) throws IOException {
        return readCMDFrom(new DataInputStream(is));
    }

    public static Command readCMDFrom(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        CommandAction cmd = CommandAction.intToCmd(dis.readInt());
        byte[] info = dis.readNBytes(length - 2);

        return new Command(cmd, info);
    }

    /**
     *
     * @return creates a new dis for each call
     */
    public DataInputStream getCmdInfoAsDIS(){
        ByteArrayInputStream bais = new ByteArrayInputStream(entireCmd);
        DataInputStream dis = new DataInputStream(bais);
        return dis;
    }

    public void sendVia(OutputStream os) throws IOException {
        sendVia(new DataOutputStream(os));
    }

    public void sendVia(DataOutputStream dos) throws IOException {
        dos.write(this.entireCmd);
        dos.flush();
    }

    /**
     * argument is added in the same order as addToCmd... methods are called
     * @param num
     */
    public Command addToCmdInfo(int num){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            if(cmdInfo != null)
                dos.write(cmdInfo);
            dos.writeInt(num);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cmdInfo = baos.toByteArray();
        return this;
    }


    /**
     * see addToCmdInfo comment
     * @param txt
     * @return
     */
    public Command addToCmdInfoAsUTF(String txt) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            if(cmdInfo != null) dos.write(cmdInfo);
            dos.writeUTF(txt);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cmdInfo = baos.toByteArray();
        return this;
    }
}
