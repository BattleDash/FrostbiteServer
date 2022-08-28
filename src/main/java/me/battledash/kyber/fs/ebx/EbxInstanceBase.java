package me.battledash.kyber.fs.ebx;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.server.ServerGameContext;
import me.battledash.kyber.util.Fnv1;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public abstract class EbxInstanceBase {

    private AssetClassGuid instanceGuid;

    public abstract EbxType<?> getType();

    public <T extends EbxPOJO> T convertToPOJO() {
        return (T) this.deserialize();
    }

    protected abstract Object deserialize();

    protected Field findField(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            for (Field declaredField : c.getDeclaredFields()) {
                String fieldName = declaredField.getName();
                if (declaredField.isAnnotationPresent(SerializedName.class)) {
                    fieldName = declaredField.getAnnotation(SerializedName.class).value();
                }
                if (fieldName.equals(name)) {
                    declaredField.setAccessible(true);
                    return declaredField;
                }
            }
        }
        return null;
    }

    protected void setFields(Class<?> clazz, Object instance, Map<String, Object> fields) {
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            Field field = this.findField(clazz, entry.getKey());
            if (field == null) {
                log.debug("Field {} not found in class {}", entry.getKey(), clazz.getName());
            } else {
                try {
                    String name = field.getName();
                    if (field.isAnnotationPresent(SerializedName.class)) {
                        name = field.getAnnotation(SerializedName.class).value();
                    }
                    ServerGameContext.context()
                            .getServer().getAssetManager()
                            .getStringHashes().put(Fnv1.hashString(name), name);
                    Object deserialized = ((EbxInstanceBase) entry.getValue()).deserialize();
                    if (deserialized instanceof List list && field.getType().isArray()) {
                        Object array = Array.newInstance(field.getType().getComponentType(), list.size());
                        for (int i = 0; i < list.size(); i++) {
                            try {
                                Array.set(array, i, list.get(i));
                            } catch (IllegalArgumentException e) {
                                log.warn("Could not set element {} of array {}. Type should be {}",
                                        i, field.getName(), list.get(i).getClass().getName());
                            }
                        }
                        deserialized = array;
                    } else if (deserialized.getClass().isArray() && field.getType().isAssignableFrom(List.class)) {
                        List<Object> list = new ArrayList<>();
                        for (int i = 0; i < Array.getLength(deserialized); i++) {
                            list.add(Array.get(deserialized, i));
                        }
                        deserialized = list;
                    } else if (field.getType().isEnum()) {
                        deserialized = field.getType().getEnumConstants()[(int) deserialized];
                    }
                    field.set(instance, deserialized);
                } catch (IllegalAccessException e) {
                    log.error("Failed to set field {} in class {}", entry.getKey(), clazz.getName(), e);
                } catch (NullPointerException e) {
                    log.debug("Field {} in class {} is null", entry.getKey(), clazz.getName(), e);
                }
            }
        }
        if (instance instanceof NotifyDeserialized notify) {
            notify.onDeserialized();
        }
    }

}
