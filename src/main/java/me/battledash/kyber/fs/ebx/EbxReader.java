package me.battledash.kyber.fs.ebx;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.fs.NativeReader;
import me.battledash.kyber.server.ServerGameContext;
import me.battledash.kyber.util.Fnv1;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Getter
public class EbxReader extends NativeReader {

    private static final Map<Type, EbxPOJO.EbxPOJODeserializer<?>> GSON_TYPES = new LinkedHashMap<>();

    public static Gson EBX_GSON;

    public static void registerTypePOJO(Type type) {
        EbxReader.GSON_TYPES.put(type, new EbxPOJO.EbxPOJODeserializer<>());
    }

    public static Class<?> getRegisteredType(String name) {
        for (Map.Entry<Type, EbxPOJO.EbxPOJODeserializer<?>> entry : EbxReader.GSON_TYPES.entrySet()) {
            if (((Class<?>) entry.getKey()).getSimpleName().equals(name)) {
                return ((Class<?>) entry.getKey());
            }
        }
        return null;
    }

    public static <T extends EbxPOJO> EbxPOJO.EbxPOJODeserializer<T> getRegisteredDeserializer(Class<?> type) {
        return (EbxPOJO.EbxPOJODeserializer<T>) EbxReader.GSON_TYPES.get(type);
    }

    public static Gson getEbxGson() {
        if (EbxReader.EBX_GSON == null) {
            GsonBuilder gsonBuilder = new GsonBuilder()
                    .registerTypeAdapter(EbxClassInstance.class, new EbxClassInstance.EbxClassInstanceSerializer())
                    .registerTypeAdapter(EbxStructInstance.class, new EbxStructInstance.EbxStructInstanceSerializer())
                    .registerTypeAdapter(EbxDataInstance.class, new EbxDataInstance.EbxDataInstanceSerializer())
                    .registerTypeAdapter(PointerRef.class, new PointerRef.PointerRefSerializer());
            gsonBuilder.setPrettyPrinting();
            for (Map.Entry<Type, EbxPOJO.EbxPOJODeserializer<?>> entry : EbxReader.GSON_TYPES.entrySet()) {
                gsonBuilder.registerTypeAdapter(entry.getKey(), entry.getValue());
            }
            EbxReader.EBX_GSON = gsonBuilder.create();
        }
        return EbxReader.EBX_GSON;
    }

    private final List<EbxField> fieldTypes = new ArrayList<>();
    private final List<EbxClass> classTypes = new ArrayList<>();
    private final List<EbxInstance> instances = new ArrayList<>();
    private final List<EbxArray> arrays = new ArrayList<>();
    private final List<EbxBoxedValue> boxedValues = new ArrayList<>();
    private final List<EbxImportReference> imports = new ArrayList<>();
    private final List<UUID> dependencies = new ArrayList<>();
    private final List<EbxInstanceBase> objects = new ArrayList<>();
    private final List<Integer> refCounts = new ArrayList<>();

    private final UUID fileGuid;
    private final long arraysOffset;
    private final long stringsOffset;
    private final long stringsAndDataLen;
    private final long guidCount;
    private final int instanceCount;
    private final int exportedCount;
    private final int uniqueClassCount;
    private final int classTypeCount;
    private final int fieldTypeCount;
    private final int typeNamesLen;
    private final long stringsLen;
    private final long arrayCount;
    private final long dataLen;
    private long boxedValuesCount;
    private long boxedValuesOffset;

    private final EbxVersion magic;
    private boolean isValid;

    public EbxReader(ByteBuf buf) {
        this(buf, true);
    }

    public EbxReader(ByteBuf buf, boolean readFully) {
        super(buf);
        this.magic = EbxVersion.fromMagic(this.readUInt());
        if (this.magic == null) {
            throw new IllegalStateException("Invalid EBX version");
        }

        this.stringsOffset = this.readUInt();
        this.stringsAndDataLen = this.readUInt();
        this.guidCount = this.readUInt();
        this.instanceCount = this.buf.readUnsignedShortLE();
        this.exportedCount = this.buf.readUnsignedShortLE();
        this.uniqueClassCount = this.buf.readUnsignedShortLE();
        this.classTypeCount = this.buf.readUnsignedShortLE();
        this.fieldTypeCount = this.buf.readUnsignedShortLE();
        this.typeNamesLen = this.buf.readUnsignedShortLE();

        this.stringsLen = this.readUInt();
        this.arrayCount = this.readUInt();
        this.dataLen = this.readUInt();

        this.arraysOffset = this.stringsOffset + this.stringsLen + this.dataLen;

        this.fileGuid = this.readGuid();

        if (!readFully) {
            return;
        }

        if (this.magic == EbxVersion.VERSION_4) {
            this.boxedValuesCount = this.readUInt();
            this.boxedValuesOffset = this.readUInt() + this.stringsOffset + this.stringsLen;
        } else {
            int readerIndex = this.buf.readerIndex() + 1;
            while (readerIndex % 16 != 0) {
                readerIndex++;
            }
            this.buf.readerIndex(readerIndex);
        }

        for (int i = 0; i < this.guidCount; i++) {
            EbxImportReference ebxImport = new EbxImportReference(this.readGuid(), this.readGuid());
            this.imports.add(ebxImport);
            if (!this.dependencies.contains(ebxImport.getFileGuid())) {
                this.dependencies.add(ebxImport.getFileGuid());
            }
        }

        Map<Integer, String> typeNames = new LinkedHashMap<>();

        long typeNamesOffset = this.buf.readerIndex();
        while (this.buf.readerIndex() < typeNamesOffset + this.typeNamesLen) {
            String typeName = this.readNullTerminatedString();
            int hash = this.hashString(typeName);

            if (!typeNames.containsKey(hash)) {
                typeNames.put(hash, typeName);
            }
        }

        for (int i = 0; i < this.fieldTypeCount; i++) {
            EbxField fieldType = new EbxField();

            int hash = this.readInt();
            fieldType.setType(this.magic == EbxVersion.VERSION_2 ? this.buf.readUnsignedShortLE() : this.buf.readUnsignedShortLE() >> 1);
            fieldType.setClassRef(this.buf.readUnsignedShortLE());
            fieldType.setDataOffset(this.readUInt());
            fieldType.setSecondOffset(this.readUInt());
            fieldType.setName(typeNames.get(hash));

            this.fieldTypes.add(fieldType);
        }

        for (int i = 0; i < this.classTypeCount; i++) {
            EbxClass classType = new EbxClass();

            int hash = this.readInt();
            classType.setFieldIndex(this.readInt());
            classType.setFieldCount(this.getBuf().readByte());
            classType.setAlignment(this.getBuf().readByte());
            classType.setType(this.magic == EbxVersion.VERSION_2 ? this.getBuf().readUnsignedShortLE() : this.getBuf().readUnsignedShortLE() >> 1);
            classType.setSize(this.getBuf().readUnsignedShortLE());
            classType.setSecondSize(this.getBuf().readUnsignedShortLE());
            classType.setName(typeNames.get(hash));

            this.classTypes.add(classType);
        }

        int tempExportedCount = this.exportedCount;
        for (int i = 0; i < this.instanceCount; i++) {
            EbxInstance instance = new EbxInstance(this.buf.readUnsignedShortLE(), this.buf.readUnsignedShortLE());

            if (tempExportedCount != 0) {
                instance.setExported(true);
                tempExportedCount--;
            }

            this.instances.add(instance);
        }

        int readerIndex = this.buf.readerIndex();
        while (readerIndex % 16 != 0) {
            readerIndex++;
        }
        this.buf.readerIndex(readerIndex);

        for (long i = 0; i < this.arrayCount; i++) {
            this.arrays.add(new EbxArray(this.readUInt(), this.readUInt(), this.readInt()));
        }

        this.pad(16);

        for (long i = 0; i < this.boxedValuesCount; i++) {
            this.boxedValues.add(new EbxBoxedValue(this.readUInt(), this.buf.readUnsignedShortLE(), this.buf.readUnsignedShortLE()));
        }

        this.buf.readerIndex((int) (this.stringsOffset + this.stringsLen));
        this.isValid = true;
    }

    public List<Object> getRootObjects() {
        List<Object> rootObjects = new ArrayList<>();
        for (int i = 0; i < this.objects.size(); i++) {
            if (this.refCounts.get(i) == 0 || i == 0) {
                rootObjects.add(this.objects.get(i));
            }
        }
        return rootObjects;
    }

    public String getRootType() {
        return this.classTypes.get(this.instances.get(0).getClassRef()).getName();
    }

    private void internalReadObjects() {
        for (EbxInstance instance : this.instances) {
            EbxClass classType = this.classTypes.get(instance.getClassRef());
            log.debug("Reading instance {} ({})", classType.getName(), classType.getFieldCount());
            for (int i = 0; i < instance.getCount(); i++) {
                EbxType<?> type = EbxTypeLibrary.parseClass(this, classType);
                this.objects.add(EbxTypeLibrary.createInstance(type));
                this.refCounts.add(0);
            }
        }

        int typeId = 0;
        int index = 0;

        for (EbxInstance instance : this.instances) {
            EbxClass classType = this.classTypes.get(instance.getClassRef());
            for (int i = 0; i < instance.getCount(); i++) {
                int readerIndex = this.getBuf().readerIndex();
                while (readerIndex % classType.getAlignment() != 0) {
                    readerIndex++;
                }
                this.getBuf().readerIndex(readerIndex);

                UUID instanceGuid = null;
                if (instance.isExported()) {
                    instanceGuid = this.readGuid();
                }

                if (classType.getAlignment() != 0x04) {
                    this.getBuf().skipBytes(8);
                }

                EbxInstanceBase object = this.objects.get(index);
                // TODO: 4/11/2022 Set instance guid

                object.setInstanceGuid(new AssetClassGuid(instanceGuid, index++));

                this.readClass(classType, object, this.getBuf().readerIndex() - 8);
            }
        }

        if (this.boxedValuesCount > 0) {
            this.getBuf().readerIndex((int) this.boxedValuesCount);
        }
    }

    private Object readClass(EbxClass classType, EbxInstanceBase obj, long startOffset) {
        if (obj == null) {
            int position = this.getBuf().readerIndex() + classType.getSize();
            while (position % classType.getAlignment() != 0) {
                position++;
            }
            this.getBuf().readerIndex(position);
            return null;
        }
        EbxType<?> objType = obj.getType();

        for (int j = 0; j < classType.getFieldCount(); j++) {
            EbxField fieldType = this.fieldTypes.get(classType.getFieldIndex() + j);
            int parentLevel = 0;
            EbxFieldType fieldProp = ((EbxFieldableType) objType.getType()).getField(fieldType);
            if (fieldProp == null && objType.getType() instanceof EbxClassType ebxClassType) {
                EbxClassType current = ebxClassType;
                while (fieldProp == null) {
                    EbxType<EbxClassType> currentType = current.getParent();
                    if (currentType == null) {
                        break;
                    }
                    current = currentType.getType();
                    fieldProp = current.getField(fieldType);
                    parentLevel++;
                }
            }

            if (fieldType.getDebugType() == EbxFieldTypes.INHERITED) {
                this.readClass(this.classTypes.get(fieldType.getClassRef()), obj, startOffset);
            } else {
                if (fieldType.getDebugType() == EbxFieldTypes.RESOURCEREF
                        || fieldType.getDebugType() == EbxFieldTypes.TYPEREF
                        || fieldType.getDebugType() == EbxFieldTypes.FILEREF
                        || fieldType.getDebugType() == EbxFieldTypes.BOXEDVALUEREF
                        || fieldType.getDebugType() == EbxFieldTypes.UINT64
                        || fieldType.getDebugType() == EbxFieldTypes.INT64
                        || fieldType.getDebugType() == EbxFieldTypes.FLOAT64) {
                    // Structure alignment
                    int position = this.getBuf().readerIndex();
                    while (position % 8 != 0)
                        position++;
                    this.getBuf().readerIndex(position);
                } else if (fieldType.getDebugType() == EbxFieldTypes.ARRAY
                        || fieldType.getDebugType() == EbxFieldTypes.POINTER) {
                    int position = this.getBuf().readerIndex();
                    while (position % 4 != 0)
                        position++;
                    this.getBuf().readerIndex(position);
                }

                if (fieldType.getDebugType() == EbxFieldTypes.ARRAY) {
                    EbxClass arrayType = this.classTypes.get(fieldType.getClassRef());

                    int index = this.readInt();
                    EbxArray array = this.arrays.get(index);

                    long arrayPos = this.getBuf().readerIndex();
                    this.getBuf().readerIndex((int) (this.arraysOffset + array.getOffset()));

                    for (int i = 0; i < array.getCount(); i++) {
                        EbxField arrayField = this.fieldTypes.get(arrayType.getFieldIndex());
                        EbxInstanceBase value = this.readField(arrayType, arrayField.getDebugType(), arrayField.getClassRef(), false);
                        if (fieldProp != null && obj instanceof EbxFieldableInstance fieldableInstance) {
                            EbxDataInstance field = (EbxDataInstance) fieldableInstance.getField(fieldProp.getName());
                            ((List<Object>) field.getObject()).add(value);
                        }
                    }
                    this.getBuf().readerIndex((int) arrayPos);
                } else {
                    EbxInstanceBase value = this.readField(classType, fieldType.getDebugType(), fieldType.getClassRef(), false);
                    if (fieldProp != null) {
                        if (obj instanceof EbxClassInstance classInstance) {
                            for (int i = 0; i < parentLevel; i++) {
                                classInstance = classInstance.getParent();
                            }
                            classInstance.getFields().put(fieldProp.getName(), value);
                        } else if (obj instanceof EbxFieldableInstance fieldableInstance) {
                            fieldableInstance.getFields().put(fieldProp.getName(), value);
                        }
                    }
                }
            }
        }

        int position = this.getBuf().readerIndex();
        while (position % classType.getAlignment() != 0) {
            position++;
        }
        this.getBuf().readerIndex(position);

        return null;
    }

    private EbxInstanceBase readField(EbxClass parentClass, EbxFieldTypes fieldType, int fieldClassRef, boolean dontRefCount) {
        switch (fieldType) {
            case BOOLEAN -> {
                return EbxDataInstance.boxed(this.getBuf().readByte() > 0);
            }
            case INT8 -> {
                return EbxDataInstance.boxed(this.getBuf().readByte());
            }
            case UINT8 -> {
                return EbxDataInstance.boxed(this.getBuf().readByte());
            }
            case INT16 -> {
                return EbxDataInstance.boxed(this.getBuf().readShortLE());
            }
            case UINT16 -> {
                return EbxDataInstance.boxed(this.readUShort());
            }
            case INT32 -> {
                return EbxDataInstance.boxed(this.readInt());
            }
            case UINT32 -> {
                return EbxDataInstance.boxed(this.readUInt());
            }
            case INT64 -> {
                return EbxDataInstance.boxed(this.readLong());
            }
            case UINT64 -> {
                return EbxDataInstance.boxed(this.readLong());
            }
            case FLOAT32 -> {
                return EbxDataInstance.boxed(this.getBuf().readFloatLE());
            }
            case FLOAT64 -> {
                return EbxDataInstance.boxed(this.getBuf().readDoubleLE());
            }
            case GUID -> {
                return EbxDataInstance.boxed(this.readGuid());
            }
            case RESOURCEREF -> {
                return EbxDataInstance.boxed(this.readLong());
                //throw new UnsupportedOperationException("Resource references are not supported yet");
            }
            case FUNCTION -> {
                throw new UnsupportedOperationException("Functions are not supported yet");
            }
            case DELEGATE -> {
                throw new UnsupportedOperationException("Delegates are not supported yet");
            }
            case SHA1 -> {
                return EbxDataInstance.boxed(this.readSha1());
            }
            case STRING -> {
                return EbxDataInstance.boxed(this.readSizedString(32));
            }
            case CSTRING -> {
                return EbxDataInstance.boxed(this.readString(this.readUInt()));
            }
            case FILEREF -> {
                // TODO: 4/12/2022 Implement file references
                long index = this.readUInt();
                this.getBuf().skipBytes(4);
                String str = this.readString(index);
                return EbxDataInstance.boxed("FileRef", str);
            }
            case TYPEREF -> {
                // TODO: 4/12/2022 Implement type references
                String str = this.readString(this.readUInt());
                this.getBuf().skipBytes(4);
                return EbxDataInstance.boxed("TypeRef", str);
            }
            case BOXEDVALUEREF -> {
                throw new UnsupportedOperationException("Boxed value references are not supported yet");
            }
            case STRUCT -> {
                EbxClass structType = this.classTypes.get(fieldClassRef);
                int position = this.getBuf().readerIndex();
                while (position % structType.getAlignment() != 0) {
                    position++;
                }
                this.getBuf().readerIndex(position);
                EbxInstanceBase structObj = EbxTypeLibrary.createInstance(structType.getName());
                this.readClass(structType, structObj, this.getBuf().readerIndex());
                return structObj;
            }
            case ENUM -> {
                return EbxDataInstance.boxed("Enum", this.readInt());
            }
            case POINTER -> {
                long index = this.readUInt();
                if ((index >> 0x1F) == 1) {
                    return EbxDataInstance.boxed(new PointerRef(this.imports.get((int) (index & 0x7FFFFFFF))));
                } else if (index == 0) {
                    return EbxDataInstance.boxed(new PointerRef());
                } else {
                    int l = (int) index - 1;
                    if (!dontRefCount) {
                        this.refCounts.set(l, this.refCounts.get(l) + 1);
                    }
                    return EbxDataInstance.boxed(new PointerRef(this.objects.get(l)));
                }
            }
            case DBOBJECT -> {
                throw new UnsupportedOperationException("DBObjects are not supported yet");
            }
            default -> throw new UnsupportedOperationException("Unknown field type: " + fieldType);
        }
    }

    private String readString(long offset) {
        if (offset == 0xFFFFFFFF)
            return "";

        long pos = this.getBuf().readerIndex();
        this.getBuf().readerIndex((int) (this.stringsOffset + offset));

        String retStr = this.readNullTerminatedString();
        this.getBuf().readerIndex((int) pos);

        ServerGameContext.context()
                .getServer().getAssetManager()
                .getStringHashes().put(Fnv1.hashString(retStr), retStr);

        return retStr;
    }

    public <T extends EbxAsset> T readAsset(Class<T> clazz) {
        T asset;
        try {
            asset = clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }

        this.internalReadObjects();

        asset.setFileGuid(this.fileGuid);
        asset.setObjects(this.objects);
        asset.setDependencies(this.dependencies);
        asset.setRefCounts(this.refCounts);
        asset.onLoadComplete();

        return asset;
    }

    public int hashString(String str) {
        int hash = 5381;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash * 33) ^ str.charAt(i);
        }
        return hash;
    }

    public enum PointerRefType {
        NULL,
        INTERNAL,
        EXTERNAL
    }

    @Data
    @NoArgsConstructor
    public static class PointerRef implements StringSerializable {
        private EbxImportReference external;
        private Object internal;
        private PointerRefType type;

        public PointerRef(EbxImportReference externalRef) {
            this.external = externalRef;
            this.type = PointerRefType.EXTERNAL;
        }

        public PointerRef(UUID guid) {
            this.external = new EbxImportReference(guid, null);
            this.type = guid != null ? PointerRefType.EXTERNAL : PointerRefType.NULL;
        }

        public PointerRef(Object internalRef) {
            this.external = new EbxImportReference();
            this.internal = internalRef;
            this.type = PointerRefType.INTERNAL;
        }

        public <T> T getObject() {
            if (this.type == PointerRefType.EXTERNAL) {
                //throw new UnsupportedOperationException("External references are not supported yet");
                return ServerGameContext.context().getServer().getAssetManager()
                        .getEbxAsset(this.external.getFileGuid()).getObjectWithGuid(this.external.getClassGuid());
            } else if (this.type == PointerRefType.INTERNAL) {
                return (T) this.internal;
            } else {
                return null;
            }
        }

        @Override
        public String toString(int depth) {
            return this.internal != null && this.internal instanceof StringSerializable ss ? ss.toString(depth + 1) : this.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof PointerRef reference) {
                return (this.type == reference.getType() && this.internal.equals(reference.getInternal()) && this.external.equals(reference.getExternal()));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(external, internal, type);
        }

        public static class PointerRefSerializer implements JsonSerializer<PointerRef> {
            @Override
            public JsonElement serialize(PointerRef src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", "PointerRef");
                if (src.getInternal() != null) {
                    jsonObject.add("internal", context.serialize(src.getInternal()));
                } else if (src.getExternal() != null) {
                    jsonObject.add("external", context.serialize(src.getExternal()));
                } else {
                    jsonObject.addProperty("type", "NULL");
                }
                return jsonObject;
            }
        }
    }

    @Getter
    private enum EbxVersion {
        VERSION_2(0x0FB2D1CE),
        VERSION_4(0x0FB4D1CE);

        private static final EbxVersion[] VALUES = EbxVersion.values();

        private final long magic;

        EbxVersion(long magic) {
            this.magic = magic;
        }

        public static EbxVersion fromMagic(long magic) {
            for (EbxVersion version : EbxVersion.VALUES) {
                if (version.getMagic() == magic) {
                    return version;
                }
            }
            return null;
        }
    }

    @Data
    public static class EbxField {
        private String name;
        private long nameHash;
        private int type;
        private int classRef;
        private long dataOffset;
        private long secondOffset;

        public EbxFieldTypes getDebugType() {
            return EbxFieldTypes.fromKey((this.type >> 4) & 0x1F);
        }
    }

    @Data
    public static class EbxClass {
        private String name;
        private long nameHash;
        private int fieldIndex;
        private byte fieldCount;
        private byte alignment;
        private int type;
        private int size;
        private int secondSize;
        private String namespace;
        private int index;

        public EbxFieldTypes getDebugType() {
            return EbxFieldTypes.fromKey((this.type >> 4) & 0x1F);
        }
    }

    @Data
    public static class EbxInstance {
        private final int classRef;
        private final int count;
        private boolean isExported;
    }

    @Data
    public static class EbxArray {
        private final long offset;
        private final long count;
        private final int classRef;
    }

    @Data
    public static class EbxBoxedValue {
        private final long offset;
        private final int classRef;
        private final int type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EbxImportReference {
        private UUID fileGuid;
        private UUID classGuid;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EbxImportReference that)) return false;
            return Objects.equals(this.fileGuid, that.fileGuid) && Objects.equals(this.classGuid, that.classGuid);
        }

        @Override
        public int hashCode() {
            long hash = 2166136261L;
            hash = (hash * 16777619) ^ this.fileGuid.hashCode();
            hash = (hash * 16777619) ^ this.classGuid.hashCode();
            return (int) hash;
        }
    }

    @Data
    @NoArgsConstructor
    public static class EbxAsset {
        private UUID fileGuid;
        private List<EbxInstanceBase> objects;
        private List<UUID> dependencies;
        private List<Integer> refCounts;

        public EbxAsset(EbxInstanceBase[] rootObjects) {
            this.fileGuid = UUID.randomUUID();

            this.objects = new ArrayList<>();
            this.dependencies = new ArrayList<>();
            this.refCounts = new ArrayList<>();

            for (EbxInstanceBase obj : rootObjects) {
                this.objects.add(obj);
                this.refCounts.add(0);
            }
        }

        public void onLoadComplete() {
        }

        private UUID getRootInstanceGuid() {
            return null;
        }

        public <T> T getObjectOfType(String type) {
            for (EbxInstanceBase object : this.objects) {
                if (type.equals(object.getType().getName())) {
                    return (T) object;
                }
            }
            return null;
        }

        public <T> T getObjectWithGuid(UUID guid) {
            for (EbxInstanceBase object : this.objects) {
                if (guid.equals(object.getInstanceGuid().getExportedGuid())) {
                    return (T) object;
                }
            }
            return null;
        }
    }

}
