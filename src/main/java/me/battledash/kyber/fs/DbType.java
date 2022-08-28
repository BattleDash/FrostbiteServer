package me.battledash.kyber.fs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DbType {
    INVALID(0),
    LIST(1),
    OBJECT(2),
    BOOLEAN(6),
    STRING(7),
    INT(8),
    LONG(9),
    FLOAT(11),
    DOUBLE(12),
    GUID(15),
    SHA1(16),
    BYTEARRAY(19);

    private static final DbType[] VALUES = values();

    public static DbType fromKey(int key) {
        for (DbType value : DbType.VALUES) {
            if (value.getKey() == key) {
                return value;
            }
        }
        return DbType.INVALID;
    }

    @Getter
    private final int key;
}
