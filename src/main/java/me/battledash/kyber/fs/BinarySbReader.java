package me.battledash.kyber.fs;

public interface BinarySbReader {

    DbObject readDbObject(DbReader reader, boolean containsUncompressedData, long bundleOffset);

    void readDataBlock(DbReader reader, DbObject list, boolean containsUncompressedData, long bundleOffset);

}
