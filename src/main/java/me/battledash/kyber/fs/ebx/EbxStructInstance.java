package me.battledash.kyber.fs.ebx;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class EbxStructInstance extends EbxInstanceBase implements StringSerializable, EbxFieldableInstance {

    private final EbxType<EbxStructType> type;
    private final Map<String, Object> fields;

    @Override
    public Object getField(String name) {
        return this.fields.get(name);
    }

    @Override
    public void changeField(String name, Object value) {
        this.fields.put(name, value);
    }

    public String toString(int depth) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.type.getName()).append("{");
        if (this.fields.size() > 0) {
            sb.append("\n");
        }
        for (Map.Entry<String, Object> entry : this.fields.entrySet()) {
            sb.append(String.join("", Collections.nCopies(depth + 1, "  ")));
            sb.append(entry.getKey()).append("=").append(entry.getValue() == null ? "null" :
                    (entry.getValue() instanceof StringSerializable ss ? ss.toString(depth + 1) : entry.getValue())).append(",\n");
        }
        int index = sb.lastIndexOf(",\n");
        if (index != -1) {
            sb.delete(index, index + 1);
        }
        if (this.fields.size() > 0) {
            sb.append(String.join("", Collections.nCopies(depth, "  ")));
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.toString(0);
    }

    @Override
    public Object deserialize() {
        Class<?> clazz = EbxReader.getRegisteredType(this.getType().getName());
        Preconditions.checkNotNull(clazz, "No class registered for type: " + this.getType().getName());
        try {
            Object instance = clazz.getConstructor().newInstance();
            this.setFields(clazz, instance, this.fields);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static class EbxStructInstanceSerializer implements JsonSerializer<EbxStructInstance> {
        @Override
        public JsonElement serialize(EbxStructInstance src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("type", src.getType().getName());
            for (Map.Entry<String, Object> entry : src.getFields().entrySet()) {
                object.add(entry.getKey(), context.serialize(entry.getValue()));
            }
            return object;
        }
    }

}
