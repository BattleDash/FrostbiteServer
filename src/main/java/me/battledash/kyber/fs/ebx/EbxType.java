package me.battledash.kyber.fs.ebx;

import lombok.Data;

@Data
public class EbxType<T> {

    private final String name;
    private final T type;

    public static <T> EbxType<T> boxed(String name, T object) {
        return new EbxType<>(name, object);
    }

    public static <T> EbxType<T> boxed(T object) {
        return EbxType.boxed(object.getClass().getSimpleName(), object);
    }

    public boolean isKindOf(String name) {
        if (this.name.equalsIgnoreCase(name)) {
            return true;
        } else if (this.type instanceof EbxClassType classType && classType.getParent() != null) {
            return classType.getParent().isKindOf(name);
        }
        return false;
    }

}
