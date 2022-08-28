package me.battledash.kyber.fs.ebx;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;

public interface EbxPOJO {

    default void deserialize(JsonElement json) {
    }

    default String getType() {
        return this.getClass().getSimpleName();
    }

    @Slf4j
    class EbxPOJODeserializer<T extends EbxPOJO> implements JsonDeserializer<T> {

        private static final Gson GSON = new Gson();

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            T type = (T) EbxPOJODeserializer.GSON.fromJson(json, EbxReader.getRegisteredType(json.getAsJsonObject().get("type").getAsString()));
            type.deserialize(json);
            return type;
        }

    }

}
