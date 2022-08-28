package me.battledash.kyber.fs.ebx;

import java.util.ArrayList;
import java.util.Collections;

public class EbxArrayList<T> extends ArrayList<T> implements StringSerializable {

    @Override
    public String toString(int depth) {
        if (this.size() == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        sb.append(String.join("", Collections.nCopies(depth + 1, "  ")));
        for (T t : this) {
            sb.append(String.join("", Collections.nCopies(depth - 1, "  ")));
            sb.append(t instanceof StringSerializable ss ? ss.toString(depth + 1) : t.toString());
            sb.append(",\n");
        }
        sb.append(String.join("", Collections.nCopies(depth, "  ")));
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
