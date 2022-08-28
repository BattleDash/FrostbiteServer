package me.battledash.kyber.misc;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@Setter(AccessLevel.NONE)
public class Tuple<A, B> {

    public final A a;
    public final B b;

    public static <A, B> Tuple<A, B> of(A a) {
        return new Tuple<>(a, null);
    }

    public static <A, B> Tuple<A, B> of(A a, B b) {
        return new Tuple<>(a, b);
    }

}
