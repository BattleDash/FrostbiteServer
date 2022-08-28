package me.battledash.kyber.util;

import java.util.Deque;
import java.util.LinkedList;

public class ProfilingUtil {

    private static final Deque<Long> PUSHED = new LinkedList<>();

    public static long pushTime() {
        long time = System.currentTimeMillis();
        ProfilingUtil.PUSHED.add(time);
        return time;
    }

    public static long popTime() {
        Long poll = ProfilingUtil.PUSHED.pollLast();
        return poll == null ? 0 : poll;
    }

    public static long popAndCalculateTime() {
        return System.currentTimeMillis() - ProfilingUtil.popTime();
    }

}
