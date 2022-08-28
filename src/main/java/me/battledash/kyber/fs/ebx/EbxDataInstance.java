package me.battledash.kyber.fs.ebx;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Type;
import java.util.ArrayList;

@Data
@EqualsAndHashCode(callSuper = true)
public class EbxDataInstance extends EbxInstanceBase implements StringSerializable {

    private final EbxType<?> type;
    private final Object object;

    public <T> T getObject() {
        return (T) this.object;
    }

    @Override
    public String toString(int depth) {
        if (this.object instanceof StringSerializable ss) {
            return ss.toString(depth);
        }
        return this.toString();
    }

    @Override
    public String toString() {
        return this.object.toString();
    }

    public static <T> EbxDataInstance boxed(T object) {
        return new EbxDataInstance(EbxType.boxed(object), object);
    }

    public static <T> EbxDataInstance boxed(String typeName, T object) {
        return new EbxDataInstance(EbxType.boxed(typeName, object), object);
    }

    @Override
    public Object deserialize() {
        Object object = this.object;
        if (object instanceof EbxReader.PointerRef ref) {
            object = ((EbxInstanceBase) ref.getObject()).deserialize();
        } else if (object instanceof EbxInstanceBase instance) {
            object = instance.deserialize();
        } else if (object instanceof ArrayList<?> list) {
            ArrayList<Object> newList = new ArrayList<>();
            for (Object o : list) {
                newList.add(o instanceof EbxInstanceBase instance ? instance.deserialize() : o);
            }
            object = newList;
        }
        return object;
    }

    public static class EbxDataInstanceSerializer implements JsonSerializer<EbxDataInstance> {
        @Override
        public JsonElement serialize(EbxDataInstance src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.getObject());
        }
    }

}
