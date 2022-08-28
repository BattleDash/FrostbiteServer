package me.battledash.kyber.fs;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.fs.ebx.EbxReader;
import me.battledash.kyber.util.Fnv1;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
public class AssetManager {

    private List<SuperBundleEntry> superBundles = new ArrayList<>();
    private List<BundleEntry> bundles = new ArrayList<>();
    private Map<String, EbxAssetEntry> ebxList = new HashMap<>();
    private Map<UUID, EbxAssetEntry> ebxGuidList = new HashMap<>();
    private Map<UUID, ChunkAssetEntry> chunkList = new HashMap<>();
    private Map<Long, String> stringHashes = new HashMap<>(); // A map of hashes to strings used throughout the engine

    private Cache<UUID, EbxReader.EbxAsset> ebxAssetCache = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES).build();

    private final ResourceManager resourceManager;

    public void init() throws IOException {
        log.info("Initializing resources...");
        this.resourceManager.init();
        log.info("Loading manifest assets...");
        this.loadManifestAssets();
        log.info("Loading hashes...");
        this.loadStringHashes();
    }

    public void loadStringHashes() throws IOException {
        InputStream stream = AssetManager.class.getClassLoader().getResourceAsStream("strings.txt");
        if (stream == null) {
            return;
        }

        IOUtils.readLines(stream, Charset.defaultCharset()).forEach(this::registerString);
    }

    public void registerString(String string) {
        this.stringHashes.put(Fnv1.hashString(string), string);
    }

    public String getHashedString(long hash) {
        return this.stringHashes.get(hash);
    }

    public void loadManifestAssets() {
        SuperBundleEntry sb = new SuperBundleEntry();
        sb.setName("<none>");
        this.superBundles.add(sb);

        for (DbObject bundle : this.resourceManager.getFs().getBundles()) {
            BundleEntry bundleEntry = new BundleEntry();
            bundleEntry.setName(bundle.getValue("name"));
            bundleEntry.setSuperBundleId(0);
            this.bundles.add(bundleEntry);

            this.processBundleEbx(bundle, this.bundles.size() - 1);
            //this.processBundleChunks(bundle, this.bundles.size() - 1);
        }
    }

    public void processBundleEbx(DbObject sb, int bundleId) {
        if (sb.<DbObject>getValue("ebx") == null) {
            return;
        }

        for (Object obj : sb.<DbObject>getValue("ebx")) {
            if (obj instanceof DbObject ebx) {
                EbxAssetEntry entry = this.addEbx(ebx);
                if (entry.getSha1() != ebx.getValue("sha1") && ebx.<Integer>getValue("casPatchType", -1) != 0) {
                    entry.setSha1(ebx.getValue("sha1"));
                    //entry.setSize(ebx.getValue("size"));
                    entry.setOriginalSize(ebx.getValue("originalSize"));
                    entry.setInline(ebx.hasValue("idata"));
                }
            }
        }
    }

    public EbxAssetEntry addEbx(DbObject ebx) {
        String name = ebx.<String>getValue("name").toLowerCase();

        if (this.ebxList.containsKey(name)) {
            return this.ebxList.get(name);
        }

        EbxAssetEntry entry = new EbxAssetEntry();
        entry.setName(name);
        entry.setSha1(ebx.getValue("sha1"));
        //entry.setSize(ebx.getValue("size"));
        entry.setOriginalSize(ebx.getValue("originalSize"));
        entry.setInline(ebx.hasValue("idata"));
        entry.setLocation(AssetDataLocation.CAS);

        //entry.setGuid(Md5Util.getMd5Guid(name.toLowerCase()));

        // TODO: 4/10/2022 Set baseSha1 here

        if (ebx.hasValue("cas")) {
            entry.setLocation(AssetDataLocation.CAS_NON_INDEXED);

            AssetExtraData extraData = new AssetExtraData();
            extraData.setDataOffset(ebx.getValue("offset"));
            extraData.setCasPath(ebx.hasValue("catalog") ? this.resourceManager.getFs().getFilePath(
                    ebx.getValue("catalog"),
                    ebx.getValue("cas"),
                    ebx.hasValue("patch")
            ) : this.resourceManager.getFs().getFilePath(ebx.getValue("cas")));
            entry.setExtraData(extraData);
        } else if (ebx.<Boolean>getValue("sb", false)) {
            entry.setLocation(AssetDataLocation.SUPER_BUNDLE);
            AssetExtraData extraData = new AssetExtraData();
            extraData.setDataOffset(ebx.getValue("offset"));
            extraData.setSuperBundleId(this.superBundles.size() - 1);
            entry.setExtraData(extraData);
        } else if (ebx.<Integer>getValue("casPatchType", -1) == 2) {
            AssetExtraData extraData = new AssetExtraData();
            extraData.setBaseSha1(ebx.getValue("baseSha1"));
            extraData.setDeltaSha1(ebx.getValue("deltaSha1"));
        }

        this.ebxList.put(name, entry);
        return entry;
    }

    public void processBundleChunks(DbObject sb, int bundleId) {
        if (sb.getValue("chunks") == null) {
            return;
        }

        for (Object obj : sb.<DbObject>getValue("chunks")) {
            if (obj instanceof DbObject chunk) {
                ChunkAssetEntry entry = this.addChunk(chunk);

                if (entry.getSize() == 0) {
                    entry.setSize(chunk.getValue("size"));
                    entry.setLogicalOffset(chunk.getValue("logicalOffset"));
                    entry.setLogicalSize(chunk.getValue("logicalSize"));
                    entry.setRangeStart(chunk.getValue("rangeStart"));
                    entry.setRangeEnd(chunk.getValue("rangeEnd"));
                    entry.setBundledSize(chunk.getValue("bundledSize"));
                    entry.setInline(chunk.hasValue("idata"));
                }
            }
        }
    }

    public ChunkAssetEntry addChunk(DbObject chunk) {
        UUID chunkId = chunk.getValue("id");

        if (this.chunkList.containsKey(chunkId)) {
            return this.chunkList.get(chunkId);
        }

        ChunkAssetEntry entry = new ChunkAssetEntry();
        entry.setId(chunkId);
        entry.setSha1(chunk.getValue("sha1"));
        entry.setLogicalOffset(chunk.getValue("logicalOffset"));
        entry.setLogicalSize(chunk.getValue("logicalSize"));
        entry.setRangeStart(chunk.getValue("rangeStart"));
        entry.setRangeEnd(chunk.getValue("rangeEnd"));
        entry.setBundledSize(chunk.getValue("bundledSize"));
        entry.setInline(chunk.hasValue("idata"));
        entry.setLocation(AssetDataLocation.CAS);

        if (chunk.hasValue("cas")) {
            entry.setLocation(AssetDataLocation.CAS_NON_INDEXED);

            AssetExtraData extraData = new AssetExtraData();
            extraData.setDataOffset(chunk.getValue("offset"));
            extraData.setCasPath(chunk.hasValue("catalog") ? this.resourceManager.getFs().getFilePath(
                    chunk.getValue("catalog"),
                    chunk.getValue("cas"),
                    chunk.hasValue("patch")
            ) : this.resourceManager.getFs().getFilePath(chunk.getValue("cas")));
            entry.setExtraData(extraData);
        } else if (chunk.<Boolean>getValue("sb", false)) {
            entry.setLocation(AssetDataLocation.SUPER_BUNDLE);

            AssetExtraData extraData = new AssetExtraData();
            extraData.setDataOffset(chunk.getValue("offset"));
            extraData.setSuperBundleId(this.superBundles.size() - 1);
            entry.setExtraData(extraData);
        }

        this.chunkList.put(chunkId, entry);
        return entry;
    }

    public void indexAsset(EbxAssetEntry entry) {
        ByteBuf stream = this.getAssetData(entry);
        //long nameHash = Fnv1.hashString(entry.getName().toLowerCase());

        if (stream != null) {
            EbxReader reader = new EbxReader(stream, false);
            entry.setGuid(reader.getFileGuid());

            if (this.ebxGuidList.containsKey(entry.getGuid())) {
                return;
            }

            this.ebxGuidList.put(entry.getGuid(), entry);

            reader.getBuf().release();
            stream.release();
        } else {
            log.warn("Failed to load ebx {}", entry.getName());
        }
    }

    public void createIndexCache() throws IOException {
        File file = new File("ebx_index.cache");
        file.createNewFile();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            for (Map.Entry<UUID, EbxAssetEntry> entry : this.ebxGuidList.entrySet()) {
                fos.write((entry.getValue().getName() + ":" + entry.getKey().toString() + "\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadIndexCache() throws IOException {
        File file = new File("ebx_index.cache");

        if (!file.exists()) {
            this.doEbxIndexing();
        }

        for (String line : Files.readAllLines(file.toPath())) {
            String[] split = line.split(":");

            if (split.length == 2) {
                EbxAssetEntry entry = this.ebxList.get(split[0]);
                if (entry == null) {
                    log.error("EBX mismatch detected in cache loading ({}), forcing cache regeneration", split[0]);
                    this.doEbxIndexing();
                    break;
                }
                UUID key = UUID.fromString(split[1]);
                entry.setGuid(key);
                this.ebxGuidList.put(key, entry);
            }
        }
    }

    @SneakyThrows
    public void doEbxIndexing() {
        int i = 0;
        for (Map.Entry<Integer, String> casEntry : this.resourceManager.getCasFiles().entrySet()) {
            List<EbxAssetEntry> ebxAssetEntries = this.ebxList.values().stream().filter(e -> e.getLocation() == AssetDataLocation.CAS &&
                    this.resourceManager.getResourceEntries().get(e.getSha1()).getArchiveIndex() == casEntry.getKey()).toList();
            if (ebxAssetEntries.size() == 0) {
                continue;
            }

            log.info("Indexing {} ebx assets in {}", ebxAssetEntries.size(), casEntry.getValue());
            for (EbxAssetEntry entry : ebxAssetEntries) {
                this.indexAsset(entry);
            }

            i += ebxAssetEntries.size();
            log.info("Indexed {}/{} ebx assets", i, this.ebxList.size());
        }

        this.createIndexCache();
    }

    public EbxAssetEntry getEbxEntry(String name) {
        return this.ebxList.get(name.toLowerCase());
    }

    public EbxAssetEntry getEbxEntry(UUID guid) {
        return this.ebxGuidList.get(guid);
    }

    public ByteBuf getAssetData(AssetEntry entry) {
        if (entry == null) {
            return null;
        }
        if (entry.getLocation() == AssetDataLocation.CAS) {
            if (entry.getExtraData() != null) {
                return this.resourceManager.getResourceData(entry.getExtraData().getBaseSha1(), entry.getExtraData().getDeltaSha1());
            } else {
                return this.resourceManager.getResourceData(entry.getSha1());
            }
        }
        log.warn("Unsupported asset location: {}", entry.getLocation());
        return null;
    }

    public EbxReader.EbxAsset getEbxAsset(EbxAssetEntry entry) {
        EbxReader.EbxAsset asset = this.ebxAssetCache.getIfPresent(entry.getGuid());
        if (asset != null) {
            return asset;
        }

        ByteBuf stream = this.getAssetData(entry);
        if (stream == null) {
            return null;
        }

        EbxReader reader = new EbxReader(stream);
        asset = reader.readAsset(EbxReader.EbxAsset.class);

        this.ebxAssetCache.put(entry.getGuid(), asset);

        log.info("Loaded asset {}", entry.getName());

        return asset;
    }

    public EbxReader.EbxAsset getEbxAsset(EbxAssetEntry entry, UUID instanceId) {
        ByteBuf stream = this.getAssetData(entry);
        if (stream == null) {
            return null;
        }

        EbxReader reader = new EbxReader(stream);
        EbxReader.EbxAsset ebxAsset = reader.readAsset(EbxReader.EbxAsset.class);
        return ebxAsset;
    }

    public EbxReader.EbxAsset getEbxAsset(String name) {
        return this.getEbxAsset(this.getEbxEntry(name));
    }

    public EbxReader.EbxAsset getEbxAsset(UUID guid) {
        return this.getEbxAsset(this.getEbxEntry(guid));
    }

    public enum BundleType {
        NONE,
        SUBLEVEL,
        BLUEPRINT_BUNDLE,
        SHARED_BUNDLE;

        private static final BundleType[] VALUES = BundleType.values();

        public static BundleType fromKey(int key) {
            return BundleType.VALUES[key + 1];
        }
    }

    @Data
    public static class BundleEntry {
        private String name;
        private int superBundleId;
        private EbxAssetEntry blueprint;
        private BundleType type;
        private boolean added;
    }

    @Data
    public static class SuperBundleEntry {
        private String name;
        private boolean added;
    }

    @Data
    public static class AssetEntry {
        private String name;
        private Sha1 sha1;

        private long size;
        private long originalSize;
        private boolean isInline;
        private AssetDataLocation location;
        private AssetExtraData extraData;

        private boolean isAdded;

        public String getFilename() {
            int id = this.name.lastIndexOf('/');
            return id == -1 ? this.name : this.name.substring(id + 1);
        }

        public String getPath() {
            int id = this.name.lastIndexOf('/');
            return id == -1 ? "" : this.name.substring(0, id);
        }

    }

    @Data
    @ToString(callSuper = true)
    public static class EbxAssetEntry extends AssetEntry {
        private UUID guid;
        private List<UUID> dependentAssets = new ArrayList<>();

        public boolean containsDependency(UUID guid) {
            return this.dependentAssets.contains(guid);
        }

        public boolean equals(Object obj) {
            if (obj instanceof EbxAssetEntry) {
                if (this.guid == null) {
                    log.error("{} - guid is null", this.getName());
                    return false;
                }
                return this.guid.equals(((EbxAssetEntry) obj).guid);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.guid);
        }
    }

    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public static class ChunkAssetEntry extends AssetEntry {
        private UUID id;
        private long bundledSize;
        private long logicalOffset;
        private long logicalSize;
        private long rangeStart;
        private long rangeEnd;

        private int h32;
        private int firstMip;
        private boolean isTochunk;
        private boolean tocChunkSpecialHack;
    }

    @Data
    public static class AssetExtraData {
        private Sha1 baseSha1;
        private Sha1 deltaSha1;
        private long dataOffset;
        private int superBundleId;
        private boolean isPatch;
        private String casPath = "";
    }

    public enum AssetDataLocation {
        CAS,
        SUPER_BUNDLE,
        CAS_NON_INDEXED
    }

}
