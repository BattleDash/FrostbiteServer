package me.battledash.kyber.tests.network;

import me.battledash.kyber.streams.InBitStream;
import me.battledash.kyber.streams.OutBitStream;
import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StreamTest {

    @Test
    public void testStreams() throws DecoderException {
        OutBitStream outBitStream = OutBitStream.init();
        outBitStream.write(255, 8);
        outBitStream.write(0, 1);
        outBitStream.write(50, 16);
        outBitStream.write(5, 6);
        outBitStream.write(25, 15);
        outBitStream.write(1, 1);

        outBitStream.debugPrint();
        System.out.println(outBitStream.flush());

        outBitStream.debugPrint();

        OutBitStream stream2 = OutBitStream.init();
        stream2.writeStream(outBitStream.convertToRead());
        stream2.flush();

        stream2.debugPrint();

        InBitStream inBitStream = stream2.convertToRead();
        //InBitStream inBitStream = new InBitStream();
        //inBitStream.initBits(Unpooled.wrappedBuffer(Hex.decodeHex("f827035024035004")), stream2.getStreamBitWritePosition());

        inBitStream.debugPrint();

        System.out.println("test: " + inBitStream.read(8));
        System.out.println("test: " + inBitStream.read(1));
        System.out.println("test: " + inBitStream.read(16));
        System.out.println("test: " + inBitStream.read(6));
        System.out.println("test: " + inBitStream.read(15));
        System.out.println("test: " + inBitStream.read(1));

        /*assertEquals(255, inBitStream.read(8));
        assertEquals(0, inBitStream.read(1));
        assertEquals(50, inBitStream.read(16));
        assertEquals(5, inBitStream.read(7));
        assertEquals(100, inBitStream.read(16));*/
        //assertEquals(25, inBitStream.read(15));
        //assertEquals(1, inBitStream.read(1));
    }

}
