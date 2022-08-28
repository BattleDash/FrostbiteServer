package me.battledash.kyber.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Md5Util {

    public static void main(String[] args) {
        String str = "Gameplay/GameModes/Shared/PF_GameMode_Controller".toLowerCase();
        System.out.println(getMd5Guid(str));
    }

    public static UUID getMd5Guid(String str) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("MD5 not supported", e);
        }
        byte[] md5Bytes = md.digest(str.getBytes());
        return BufferUtil.guidToUUID(md5Bytes);
    }

    public static Guid getMd5Guid2(String str) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("MD5 not supported", e);
        }
        byte[] md5Bytes = md.digest(str.getBytes());
        Guid guid = new Guid();
        // Guid is data1 (long), data2 (int), data3 (int), data4 (byte[])
        guid.data1 = ((long) (md5Bytes[3] & 0xFF) << 24) | ((long) (md5Bytes[2] & 0xFF) << 16) | ((long) (md5Bytes[1] & 0xFF) << 8) | (long) (md5Bytes[0] & 0xFF);
        guid.data2 = ((md5Bytes[5] & 0xFF) << 8) | (md5Bytes[4] & 0xFF);
        guid.data3 = ((md5Bytes[7] & 0xFF) << 8) | (md5Bytes[6] & 0xFF);
        guid.data4[0] = md5Bytes[8];
        guid.data4[1] = md5Bytes[9];
        guid.data4[2] = md5Bytes[10];
        guid.data4[3] = md5Bytes[11];
        guid.data4[4] = md5Bytes[12];
        guid.data4[5] = md5Bytes[13];
        guid.data4[6] = md5Bytes[14];
        guid.data4[7] = md5Bytes[15];

        // Swap endian of 1
        long l = guid.data1;
        guid.data1 = ((l & 0x00000000FFFFFFFFL) << 32) | ((l & 0xFFFFFFFF00000000L) >> 32);

        // Swap endian of 2 (int)
        int i = guid.data2;
        guid.data2 = ((i & 0xFF00) << 8) | ((i & 0xFF0000) >> 8);

        // Swap endian of 3 (int)
        i = guid.data3;
        guid.data3 = ((i & 0xFF00) << 8) | ((i & 0xFF0000) >> 8);
        return guid;
    }

}
