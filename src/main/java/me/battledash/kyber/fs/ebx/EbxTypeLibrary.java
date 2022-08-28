package me.battledash.kyber.fs.ebx;

import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.misc.Tuple;
import me.battledash.kyber.fs.Sha1;
import me.battledash.kyber.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class EbxTypeLibrary {

    private static final Map<String, EbxType<?>> TYPE_REGISTRY = new LinkedHashMap<>();

    public static <T> EbxType<T> getType(String name) {
        return (EbxType<T>) EbxTypeLibrary.TYPE_REGISTRY.get(name);
    }

    public static EbxType<?> finalizeStruct(String name, List<EbxFieldType> fields, EbxReader.EbxClass classType) {
        EbxType<EbxStructType> value = new EbxType<>(name, new EbxStructType(fields));
        EbxTypeLibrary.TYPE_REGISTRY.put(name, value);
        return value;
    }

    public static EbxType<?> finalizeClass(String name, List<EbxFieldType> fields, EbxType<EbxClassType> parent) {
        EbxType<EbxClassType> value = new EbxType<>(name, new EbxClassType(fields, parent));
        EbxTypeLibrary.TYPE_REGISTRY.put(name, value);
        return value;
    }

    public static EbxType<?> finalizeEnum(String name, List<Tuple<String, Long>> enumValues) {
        EbxType<EbxEnumType> value = new EbxType<>(name, new EbxEnumType(enumValues));
        EbxTypeLibrary.TYPE_REGISTRY.put(name, value);
        return value;
    }

    public static EbxClassInstance createClassInstance(EbxType<EbxClassType> type) {
        Map<String, Object> fields = new LinkedHashMap<>();
        for (EbxFieldType field : type.getType().getFields()) {
            fields.put(field.getName(), EbxTypeLibrary.createInstance(field.getType()));
        }
        EbxClassInstance instance = new EbxClassInstance(type, fields);
        EbxType<EbxClassType> parentType = type.getType().getParent();
        if (parentType != null) {
            instance.setParent(EbxTypeLibrary.createClassInstance(parentType));
        }
        return instance;
    }

    public static EbxInstanceBase createInstance(EbxType<?> type) {
        //log.info("Creating instance of type {}", type.getName());
        if (type.getType() instanceof EbxStructType structType) {
            Map<String, Object> fields = new LinkedHashMap<>();
            for (EbxFieldType field : structType.getFields()) {
                fields.put(field.getName(), EbxTypeLibrary.createInstance(field.getType()));
            }
            return new EbxStructInstance((EbxType<EbxStructType>) type, fields);
        } else if (type.getType() instanceof EbxClassType) {
            return EbxTypeLibrary.createClassInstance((EbxType<EbxClassType>) type);
        } else if (type.getType() instanceof Class<?> clazz) {
            try {
                Constructor<?> constructor = clazz.getConstructor();
                if (constructor.getParameterCount() != 0) {
                    return null;
                }
                return new EbxDataInstance(type, constructor.newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            }
        } else {
            log.debug("Unknown type {}", type.getType());
        }
        return null;
    }

    public static EbxInstanceBase createInstance(String name) {
        EbxType<?> ebxType = EbxTypeLibrary.TYPE_REGISTRY.get(name);
        return ebxType == null ? null : EbxTypeLibrary.createInstance(ebxType);
    }

    public static EbxType<?> parseClass(EbxReader reader, EbxReader.EbxClass classType) {
        if (EbxTypeLibrary.TYPE_REGISTRY.containsKey(classType.getName())) {
            return EbxTypeLibrary.TYPE_REGISTRY.get(classType.getName());
        }

        List<EbxFieldType> fields = new ArrayList<>();
        EbxType<EbxClassType> parent = null;

        for (int j = 0; j < classType.getFieldCount(); j++) {
            EbxReader.EbxField fieldType = reader.getFieldTypes().get(classType.getFieldIndex() + j);
            if (fieldType.getDebugType() == EbxFieldTypes.INHERITED) {
                parent = (EbxType<EbxClassType>) EbxTypeLibrary.parseClass(reader, reader.getClassTypes().get(fieldType.getClassRef()));
            } else {
                EbxType<?> type = EbxTypeLibrary.getTypeFromEbxField(reader, fieldType);
                fields.add(new EbxFieldType(fieldType.getName(), type, null, fieldType,
                        (fieldType.getDebugType() == EbxFieldTypes.ARRAY)
                                ? reader.getFieldTypes().get(reader.getClassTypes().get(fieldType.getClassRef()).getFieldIndex())
                                : null));
            }
        }

        return classType.getDebugType() == EbxFieldTypes.STRUCT ? EbxTypeLibrary.finalizeStruct(classType.getName(), fields, classType)
                : EbxTypeLibrary.finalizeClass(classType.getName(), fields, parent);
    }

    private static EbxType<?> getTypeFromEbxField(EbxReader reader, EbxReader.EbxField fieldType) {
        switch (fieldType.getDebugType()) {
            case STRUCT -> {
                return EbxTypeLibrary.parseClass(reader, reader.getClassTypes().get(fieldType.getClassRef()));
            }
            case STRING -> {
                return new EbxType<>(fieldType.getName(), String.class);
            }
            case INT8 -> {
                return new EbxType<>(fieldType.getName(), Byte.class);
            }
            case UINT8 -> {
                return new EbxType<>(fieldType.getName(), Integer.class);
            }
            case BOOLEAN -> {
                return new EbxType<>(fieldType.getName(), Boolean.class);
            }
            case UINT16 -> {
                return new EbxType<>(fieldType.getName(), Integer.class);
            }
            case INT16 -> {
                return new EbxType<>(fieldType.getName(), Short.class);
            }
            case UINT32 -> {
                return new EbxType<>(fieldType.getName(), Long.class);
            }
            case INT32 -> {
                return new EbxType<>(fieldType.getName(), Integer.class);
            }
            case UINT64 -> {
                return new EbxType<>(fieldType.getName(), Long.class);
            }
            case INT64 -> {
                return new EbxType<>(fieldType.getName(), Long.class);
            }
            case FLOAT32 -> {
                return new EbxType<>(fieldType.getName(), Float.class);
            }
            case FLOAT64 -> {
                return new EbxType<>(fieldType.getName(), Double.class);
            }
            case POINTER -> {
                return EbxTypeLibrary.parseClass(reader, reader.getClassTypes().get(fieldType.getClassRef()));
            }
            case GUID -> {
                return new EbxType<>(fieldType.getName(), UUID.class);
            }
            case SHA1 -> {
                return new EbxType<>(fieldType.getName(), Sha1.class);
            }
            case CSTRING -> {
                return new EbxType<>(fieldType.getName(), String.class);
            }
            case RESOURCEREF -> {
                return new EbxType<>(fieldType.getName(), null);
            }
            case FILEREF -> {
                return new EbxType<>(fieldType.getName(), null);
            }
            case TYPEREF -> {
                return new EbxType<>(fieldType.getName(), null);
            }
            case BOXEDVALUEREF -> {
                return new EbxType<>(fieldType.getName(), Double.class);
            }
            case ARRAY -> {
                EbxReader.EbxClass arrayType = reader.getClassTypes().get(fieldType.getClassRef());
                EbxTypeLibrary.getTypeFromEbxField(reader, reader.getFieldTypes().get(arrayType.getFieldIndex()));
                return new EbxType<>(fieldType.getName(), EbxArrayList.class);
            }
            case ENUM -> {
                EbxReader.EbxClass enumType = reader.getClassTypes().get(fieldType.getClassRef());
                List<Tuple<String, Long>> enumValues = new EbxArrayList<>();
                for (byte i = 0; i < enumType.getFieldCount(); i++) {
                    EbxReader.EbxField ebxField = reader.getFieldTypes().get(enumType.getFieldIndex() + i);
                    enumValues.add(Tuple.of(ebxField.getName(), ebxField.getDataOffset()));
                    if (ebxField.getName() != null && ebxField.getName().contains("OnCommand")) {
                        log.info("Found enum field {}", ebxField);
                    }
                }
                return EbxTypeLibrary.finalizeEnum(fieldType.getName(), enumValues);
            }
            default -> throw new IllegalArgumentException("Unknown field type: " + fieldType.getDebugType());
        }
    }

    public static void initEBXTypes(String packageName) {
        for (Class<?> clazz : ClassUtils.findAllClasses(packageName)) {
            if (EbxPOJO.class.isAssignableFrom(clazz)) {
                EbxReader.registerTypePOJO(clazz);
            }
        }
    }

}
