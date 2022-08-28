package me.battledash.kyber.engine.entity;

import lombok.Data;

@Data
public class EntityEvent implements Cloneable {

    private EventId eventId;
    private final Sender sender;

    @Override
    public EntityEvent clone() {
        try {
            return (EntityEvent) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported");
        }
    }


    public enum Sender {
        EXTERNAL,
        PARENT,
        CHILD
    }

}
