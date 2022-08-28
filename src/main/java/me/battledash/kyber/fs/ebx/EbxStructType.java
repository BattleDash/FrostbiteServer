package me.battledash.kyber.fs.ebx;

import lombok.Data;

import java.util.List;

@Data
public class EbxStructType implements EbxFieldableType {

    private final List<EbxFieldType> fields;

    public EbxFieldType getField(EbxReader.EbxField fieldType) {
        for (EbxFieldType ebxFieldType : this.fields) {
            if (ebxFieldType.getFieldType().equals(fieldType)) {
                return ebxFieldType;
            }
        }
        return null;
    }

}
