package utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Converter {
    public static byte[] convert(int toConvert) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(toConvert);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            // never going to happen
            return null;
        }
    }

    public static byte[] writeUTF(String toConvert){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeUTF(toConvert);
            return baos.toByteArray();
        } catch (IOException e) {
            // never going to happen
            e.printStackTrace();
            return null;
        }
    }
}
