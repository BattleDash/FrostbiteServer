package me.battledash.kyber.fs.ebx;

import lombok.Data;

import java.util.List;

@Data
public class EbxClassType implements EbxFieldableType {

    private final List<EbxFieldType> fields;
    private final EbxType<EbxClassType> parent;

    public EbxFieldType getField(EbxReader.EbxField fieldType) {
        for (EbxFieldType ebxFieldType : this.fields) {
            if (ebxFieldType.getFieldType().equals(fieldType)) {
                return ebxFieldType;
            }
        }
        return null;
    }

    public EbxFieldType getField(String name) {
        for (EbxFieldType field : this.fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

}
