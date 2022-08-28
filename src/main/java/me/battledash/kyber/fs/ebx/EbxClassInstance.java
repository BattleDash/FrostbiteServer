package me.battledash.kyber.fs.ebx;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
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
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class EbxClassInstance extends EbxInstanceBase implements StringSerializable, EbxFieldableInstance {

    private final EbxType<EbxClassType> type;
    private EbxClassInstance parent;
    private final Map<String, Object> fields;

    public void changeField(String name, Object value) {
        if (this.fields.containsKey(name)) {
            this.fields.put(name, value);
        } else if (this.parent != null) {
            this.parent.changeField(name, value);
        }
        throw new IllegalStateException("Field " + name + " not found");
    }

    @Override
    public Object getField(String name) {
        Object object = this.fields.get(name);
        if (object == null && this.parent != null) {
            return this.parent.getField(name);
        }
        return object;
    }

    public String toString(int depth) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.type.getName()).append("{");
        if (this.parent != null || this.fields.size() > 0) {
            sb.append("\n");
        }
        if (this.parent != null) {
            sb.append(String.join("", Collections.nCopies(depth + 1, "  ")));
            sb.append("Parent=").append(this.parent.toString(depth + 1)).append(",\n");
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
        if (this.parent != null || this.fields.size() > 0) {
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
        Class<?> clazz = null;
        EbxClassInstance current = this;
        while (clazz == null && current != null) {
            clazz = EbxReader.getRegisteredType(current.getType().getName());
            current = current.getParent();
        }
        Preconditions.checkNotNull(clazz, "No class registered for type: " + this.getType().getName());
        Preconditions.checkState(!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()),
                "No class registered for type: " + this.getType().getName() +
                        " (closest defined superclass is " + clazz.getSimpleName() + ", but it is abstract)");
        try {
            Object instance = clazz.getConstructor().newInstance();
            Map<String, Object> fields = new HashMap<>();
            current = this;
            while (current != null) {
                fields.putAll(current.getFields());
                current = current.getParent();
            }
            this.setFields(clazz, instance, fields);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static class EbxClassInstanceSerializer implements JsonSerializer<EbxClassInstance> {
        @Override
        public JsonElement serialize(EbxClassInstance src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            EbxClassInstance current = src.getParent();
            JsonArray parents = new JsonArray();
            while (current != null) {
                JsonObject parent = context.serialize(current).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : parent.entrySet()) {
                    object.add(entry.getKey(), entry.getValue());
                }
                parents.add(current.getType().getName());
                current = current.getParent();
            }
            object.add("parents", parents);
            object.addProperty("type", src.getType().getName());
            if (src.getParent() != null) {
                //object.add("parent", context.serialize(src.getParent()));
            }
            for (Map.Entry<String, Object> entry : src.getFields().entrySet()) {
                object.add(entry.getKey(), context.serialize(entry.getValue()));
            }
            return object;
        }
    }

}
