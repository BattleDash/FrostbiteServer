package me.battledash.kyber.tests.network;

import me.battledash.kyber.misc.StateMask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateMaskTest {

    @Test
    public void testStateMask() {
        StateMask mask = new StateMask();
        mask.setBits(StateMask.GHOST_MASK_INIT, StateMask.RESERVED_STATES, 0);
        assertTrue(mask.testBits(StateMask.GHOST_MASK_INIT, StateMask.RESERVED_STATES, 0));
    }

}
