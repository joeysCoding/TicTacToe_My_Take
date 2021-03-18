package command;

import utils.Converter;

import java.io.*;

class CMD {
    public final CommandAction cmdAction;
    public byte[] cmdInfo;
    private int cmdTotalLength;
    private byte[] entireCmd;


    public CMD(CommandAction cmdAction, byte[] cmdInfo) {
        this.cmdAction = cmdAction;
        this.cmdInfo = cmdInfo;
        setCmdTotalLength();
        setEntireCmd();
    }

    public CMD(CommandAction cmdAction) {
        this(cmdAction, null);
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


    public static CMD createCMDFrom(InputStream is) throws IOException {
        return createCMDFrom(new DataInputStream(is));
    }

    public static CMD createCMDFrom(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        CommandAction cmd = CommandAction.intToCmd(dis.readInt());
        byte[] info = dis.readNBytes(length - 2);

        return new CMD(cmd, info);
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
    public void addToCmdInfo(int num){
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
    }

    /**
     * see addToCmdInfo comment
     * @param txt
     */
    public void addToCmdInfoAsUTF(String txt) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            if(cmdInfo != null) dos.write(cmdInfo);
            dos.writeUTF(txt);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cmdInfo = baos.toByteArray();
    }
}
