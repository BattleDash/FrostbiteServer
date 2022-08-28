package me.battledash.kyber.fs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.engine.Tickable;
import me.battledash.kyber.util.Fnv1;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
public class ResourceManager implements Tickable {

    private final FrostbiteFileSystem fs;
    
    private final Map<Sha1, CatReader.CatResourceEntry> resourceEntries = new HashMap<>();
    private final Map<Sha1, CatReader.CatPatchEntry> patchEntries = new HashMap<>();
    private final Map<Integer, String> casFiles = new HashMap<>();

    public ResourceManager(String gamePath) {
        this.fs = new FrostbiteFileSystem(gamePath);
    }

    public void init() throws IOException {
        this.fs.init();

        for (FrostbiteFileSystem.CatalogInfo catalog : this.fs.getCatalogs()) {
            this.loadCatalog("native_data/" + catalog.getName() + "/cas.cat");
            this.loadCatalog("native_patch/" + catalog.getName() + "/cas.cat");
        }
    }

    private void loadCatalog(String filename) {
        String fullPath = this.fs.resolvePath(filename);
        File file = new File(fullPath);
        if (!file.exists()) {
            return;
        }

        CatReader reader = new CatReader(this.fs.retrieveFile(file));

        for (int i = 0; i < reader.getResourceCount(); i++) {
            CatReader.CatResourceEntry entry = reader.readResourceEntry();
            entry.setArchiveIndex(this.addCas(filename, entry.getArchiveIndex()));

            if (entry.getLogicalOffset() == 0 && !this.resourceEntries.containsKey(entry.getSha1())) {
                this.resourceEntries.put(entry.getSha1(), entry);
            }
        }

        for (long i = 0; i < reader.getEncryptedCount(); i++) {
            throw new UnsupportedOperationException("Encrypted resources are not supported yet.");
        }

        for (long i = 0; i < reader.getPatchCount(); i++) {
            CatReader.CatPatchEntry entry = reader.readPatchEntry();
            if (!this.patchEntries.containsKey(entry.getSha1())) {
                this.patchEntries.put(entry.getSha1(), entry);
            }
        }
    }

    private int addCas(String catPath, int archiveIndex) {
        String casFilename = catPath.substring(0, catPath.length() - 7) + "cas_" + String.format("%02d", archiveIndex) + ".cas";
        int hash = (int) Fnv1.hashString(casFilename);

        if (!this.casFiles.containsKey(hash)) {
            this.casFiles.put(hash, this.fs.resolvePath(casFilename));
        }

        return hash;
    }

    public ByteBuf getResourceData(Sha1 baseSha1, Sha1 deltaSha1) {
        if (!this.resourceEntries.containsKey(baseSha1) || !this.patchEntries.containsKey(deltaSha1)) {
            return null;
        }

        CatReader.CatResourceEntry baseEntry = this.resourceEntries.get(baseSha1);
        CatReader.CatResourceEntry deltaEntry = this.resourceEntries.get(deltaSha1);

        ByteBuf baseBuf = this.fs.retrieveFile(this.casFiles.get(baseEntry.getArchiveIndex()));
        ByteBuf deltaBuf = (deltaEntry.getArchiveIndex() == baseEntry.getArchiveIndex()) ? baseBuf :
                this.fs.retrieveFile(this.casFiles.get(deltaEntry.getArchiveIndex()));

        ByteBuf baseReadBuf = Unpooled.buffer((int) baseEntry.getSize());
        baseBuf.getBytes((int) baseEntry.getOffset(), baseReadBuf);
        ByteBuf deltaReadBuf = Unpooled.buffer((int) baseEntry.getSize());
        deltaBuf.getBytes((int) baseEntry.getOffset(), deltaReadBuf);

        return new CasReader(baseReadBuf, deltaReadBuf).read();
    }

    @SneakyThrows
    public ByteBuf getResourceData(Sha1 sha1) {
        if (this.patchEntries.containsKey(sha1)) {
            CatReader.CatPatchEntry patchEntry = this.patchEntries.get(sha1);
            return this.getResourceData(patchEntry.getBaseSha1(), patchEntry.getDeltaSha1());
        }

        if (!this.resourceEntries.containsKey(sha1)) {
            return null;
        }

        CatReader.CatResourceEntry entry = this.resourceEntries.get(sha1);

        ByteBuf baseBuf = this.fs.retrieveFile(this.casFiles.get(entry.getArchiveIndex()));
        ByteBuf readBuf = Unpooled.buffer((int) entry.getSize());

        //ProfilingUtil.pushTime();
        baseBuf.getBytes((int) entry.getOffset(), readBuf, (int) entry.getSize());
        //log.info("Stream took {}ms ({}b)", System.currentTimeMillis() - ProfilingUtil.popTime(), entry.getSize());

        return new CasReader(readBuf).read();
    }

    public void tick() {

    }

}
