package me.battledash.kyber.util;

import lombok.Data;

@Data
public class Guid {

    public long data1;
    public int data2;
    public int data3;
    public byte[] data4;

    public Guid() {
        data1 = 0;
        data2 = 0;
        data3 = 0;
        data4 = new byte[8];
    }

    public Guid(long data1, int data2, int data3, byte[] data4) {
        this.data1 = data1;
        this.data2 = data2;
        this.data3 = data3;
        this.data4 = data4;
    }

    public String toString() {
        return String.format("%08X-%04X-%04X-%02X%02X-%02X%02X%02X%02X%02X%02X",
                data1, data2, data3, data4[0], data4[1], data4[2], data4[3], data4[4], data4[5], data4[6], data4[7]);
    }

}
