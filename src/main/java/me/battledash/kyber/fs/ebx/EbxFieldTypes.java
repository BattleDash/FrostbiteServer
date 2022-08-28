package me.battledash.kyber.fs.ebx;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EbxFieldTypes {
    INHERITED(0x00),
    DBOBJECT(0x01),
    STRUCT(0x02),
    POINTER(0x03),
    ARRAY(0x04),
    STRING(0x06),
    CSTRING(0x07),
    ENUM(0x08),
    FILEREF(0x09),
    BOOLEAN(0x0A),
    INT8(0x0B),
    UINT8(0x0C),
    INT16(0x0D),
    UINT16(0x0E),
    INT32(0x0F),
    UINT32(0x10),
    INT64(0x12),
    UINT64(0x11),
    FLOAT32(0x13),
    FLOAT64(0x14),
    GUID(0x15),
    SHA1(0x16),
    RESOURCEREF(0x17),
    FUNCTION(0x18),
    TYPEREF(0x19),
    BOXEDVALUEREF(0x1A),
    INTERFACE(0x1B),
    DELEGATE(0x1C);

    private static final EbxFieldTypes[] VALUES = EbxFieldTypes.values();

    private final int value;

    public static EbxFieldTypes fromKey(int key) {
        for (EbxFieldTypes type : EbxFieldTypes.VALUES) {
            if (type.getValue() == key) {
                return type;
            }
        }
        return null;
    }
}
