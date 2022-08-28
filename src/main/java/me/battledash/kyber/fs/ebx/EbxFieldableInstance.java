package me.battledash.kyber.fs.ebx;

import java.util.Map;

public interface EbxFieldableInstance {

    Map<String, Object> getFields();

    Object getField(String name);

    void changeField(String name, Object value);

}
