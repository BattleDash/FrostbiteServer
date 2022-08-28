package me.battledash.kyber.fs;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class DbObject implements Iterable<Object> {

    private Map<String, Object> hash;
    private List<Object> list;

    public DbObject() {
        this(true);
    }

    public DbObject(boolean bObject) {
        if (bObject) {
            this.hash = new HashMap<>();
        } else {
            this.list = new ArrayList<>();
        }
    }

    public DbObject(Object inVal) {
        if (inVal instanceof List<?>) {
            this.list = (List<Object>) inVal;
        } else if (inVal instanceof Map<?, ?>) {
            this.hash = (Map<String, Object>) inVal;
        }
    }

    public static DbObject createObject() {
        return new DbObject(true);
    }

    public static DbObject createList() {
        return new DbObject(false);
    }

    public <T> List<T> getList() {
        return this.list.stream().map(o -> (T) o).collect(Collectors.toList());
    }

    public void addValue(String name, Object value) {
        if (this.hash != null) {
            this.hash.put(name, value);
        } else {
            this.list.add(value);
        }
    }

    public <T> T getValue(String name, T defaultValue) {
        if (this.hash == null || !this.hash.containsKey(name)) {
            return defaultValue;
        }
        Object o = this.hash.get(name);
        if (o == null) {
            return null;
        }
        return (T) o;
    }

    public <T> T getValue(String name) {
        return this.getValue(name, null);
    }

    public boolean hasValue(String name) {
        return this.hash.containsKey(name);
    }

    @Override
    public Iterator<Object> iterator() {
        return this.list.iterator();
    }

    @Override
    public String toString() {
        return this.createHierarchyTree("root",this, 0).toString();
    }

    public StringBuilder createHierarchyTree(String name, DbObject obj, int level) {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name).append("/\n");
        }
        if (obj.getHash() != null) {
            for (Map.Entry<String, Object> entry : obj.getHash().entrySet()) {
                sb.append("    ".repeat(name == null ? Math.max(0, level - 2) : level));
                if (name == null) {
                    sb.append("- ");
                }
                if (entry.getValue() instanceof DbObject dbObj) {
                    sb.append(this.createHierarchyTree(entry.getKey(), dbObj, level + 1));
                } else {
                    sb.append(entry.getKey()).append(": ");
                    if (entry.getValue() == null) {
                        sb.append("null\n");
                    } else {
                        sb.append(entry.getValue()).append(" (").append(entry.getValue().getClass().getSimpleName()).append(")").append("\n");
                    }
                }
            }
        } else if (obj.getList() != null) {
            for (Object entry : obj.getList()) {
                sb.append("    ".repeat(level));
                if (entry instanceof DbObject dbObj) {
                    sb.append(this.createHierarchyTree(null, dbObj, level + 1));
                } else {
                    sb.append("- ").append(entry).append("\n");
                }
            }
        }
        return sb;
    }

}
