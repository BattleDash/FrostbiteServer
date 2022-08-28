package me.battledash.kyber.misc;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WeakPtr<T> {

    private T ptr;

    public T get() {
        return this.ptr;
    }

    public void set(T ptr) {
        this.ptr = ptr;
    }

}
