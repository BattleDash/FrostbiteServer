package me.battledash.kyber.fs.ebx;

import lombok.Data;

@Data
public class EbxFieldType {

    private final String name;
    private final EbxType<?> type;
    private final EbxType<?> baseType;
    private final EbxReader.EbxField fieldType;
    private final EbxReader.EbxField arrayType;

}
