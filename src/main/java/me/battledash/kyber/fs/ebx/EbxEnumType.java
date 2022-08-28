package me.battledash.kyber.fs.ebx;

import lombok.Data;
import me.battledash.kyber.misc.Tuple;

import java.util.List;

@Data
public class EbxEnumType {

    private final List<Tuple<String, Long>> values;

}
