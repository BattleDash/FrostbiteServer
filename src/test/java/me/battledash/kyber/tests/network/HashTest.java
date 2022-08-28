package me.battledash.kyber.tests.network;

import me.battledash.kyber.util.Fnv1;
import org.junit.jupiter.api.Test;

public class HashTest {

    @Test
    public void testNetwork() {
        // 0x9F60F722
        System.out.printf("%016X%n", Fnv1.hashStringNoLowercase("CancelAutoSpawn"));
    }


}
