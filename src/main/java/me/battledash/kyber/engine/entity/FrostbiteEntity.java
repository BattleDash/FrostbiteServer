package me.battledash.kyber.engine.entity;

import me.battledash.kyber.types.pojo.entities.EntityData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the data type of the entity.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FrostbiteEntity {
    Class<? extends EntityData> value();
}
