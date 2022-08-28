package me.battledash.kyber.runtime;

import lombok.Data;

@Data
public class GameTime {

    private final int tickFrequency;

    private int ticks;
    private float deltaTime;
    private float invTickFrequency;
    private float time;

}
