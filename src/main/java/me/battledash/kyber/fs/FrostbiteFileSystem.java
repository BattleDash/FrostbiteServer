package me.battledash.kyber.fs;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.battledash.kyber.fs.games.BattlefrontIIProfile;
import me.battledash.kyber.util.EndianUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public class FrostbiteFileSystem {

    private static final GameProfile DEFAULT_PROFILE = new BattlefrontIIProfile();

    private long base;
    private long head;
    private String basePath;

    private final List<String> paths = new ArrayList<>();
    private final List<String> superBundles = new ArrayList<>();
    private final List<String> casFiles = new ArrayList<>();
    private final List<CatalogInfo> catalogs = new ArrayList<>();
    private final List<ManifestBundleInfo> manifestBundles = new ArrayList<>();
    private final List<ManifestChunkInfo> manifestChunks = new ArrayList<>();

    private final LoadingCache<String, ByteBuf> fileCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public ByteBuf load(String key) throws Exception {
                    File file = new File(key);
                    return Unpooled.wrappedBuffer(new FileInputStream(file)
                            .getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length()));
                }
            });

    public FrostbiteFileSystem(String inBasePath) {
        this.basePath = inBasePath;
        if (!this.basePath.endsWith("\\") || !this.basePath.endsWith("/")) {
            this.basePath += "/";
        }
    }

    protected ByteBuf retrieveFile(String path) {
        try {
            return this.fileCache.get(path);
        } catch (ExecutionException e) {
            log.error("Failed to retrieve file", e);
            return null;
        }
    }

    protected ByteBuf retrieveFile(File file) {
        return this.retrieveFile(file.getAbsolutePath());
    }

    public String resolvePath(String path) {
        if (path.startsWith("native_patch/")) {
            path = path.replace("native_patch/", "Patch/");
        } else if (path.startsWith("native_data/")) {
            path = path.replace("native_data/", "Data/");
        } else {
            path = "Data/" + path;
        }
        String pathname = (this.basePath + path).trim();
        if (new File(pathname).exists()) {
            return pathname.replace("\\\\", "\\");
        }

        return null;
    }

    public String getFilePath(int index) {
        return index < this.casFiles.size() ? this.casFiles.get(index) : null;
    }

    public String getFilePath(int catalog, int cas, boolean patch) {
        return (patch ? "native_patch/" : "native_data/") +
                this.catalogs.get(catalog).getName() + "/cas_" +
                String.format("%02d", cas) + ".cas";
    }

    public String resolvePath(ManifestFileRef fileRef) {
        String path = this.getFilePath(fileRef.getCatalogIndex(), fileRef.getCasIndex(), fileRef.isInPatch());
        return this.resolvePath(path);
    }

    public void init() throws IOException {
        this.processLayouts();
    }

    public List<DbObject> getBundles() {
        Map<Long, String> sharedBundles = FrostbiteFileSystem.DEFAULT_PROFILE.getSharedBundles();
        List<DbObject> bundles = new ArrayList<>();

        for (ManifestBundleInfo bundleInfo : this.manifestBundles) {
            ManifestFileInfo fileInfo = bundleInfo.getFiles().get(0);
            CatalogInfo catalogInfo = this.catalogs.get(fileInfo.getFile().getCatalogIndex());

            String path = this.resolvePath(fileInfo.getFile());
            File file;
            if (path == null || !(file = new File(path)).exists()) {
                log.warn("Bundle file {} is missing!", fileInfo.getFile().getCasIndex());
                continue;
            }

            ByteBuf buf = this.retrieveFile(file);
            BinarySbReader sbReader = new BaseBinarySbReader();
            byte[] bytes = new byte[(int) fileInfo.getSize()];
            buf.getBytes((int) fileInfo.getOffset(), bytes);
            DbReader reader = new DbReader(Unpooled.wrappedBuffer(bytes), false);
            DbObject bundle = sbReader.readDbObject(reader,
                    false, 0);

            String name = String.format("%08X", bundleInfo.getHash());
            if (sharedBundles.containsKey(bundleInfo.getHash())) {
                name = sharedBundles.get(bundleInfo.getHash());
            }
            bundle.addValue("name", name);
            bundle.addValue("catalog", catalogInfo.getName());

            bundles.add(bundle);
        }

        return bundles;
    }

    private void processLayouts() throws IOException {
        String baseLayoutPath = this.resolvePath("native_data/layout.toc");
        String patchLayoutPath = this.resolvePath("native_patch/layout.toc");

        File file = new File(baseLayoutPath);
        DbReader reader = new DbReader(Unpooled.wrappedBuffer(new FileInputStream(file)
                .getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length())));
        DbObject baseLayout = reader.<DbObject>readDbObject().getA();

        if (patchLayoutPath != null) {
            // TODO: 4/8/2022 Implement patch layout
        } else {
            this.processCatalogs(baseLayout);
            this.processManifest(baseLayout);
        }
    }

    private void processCatalogs(DbObject layout) {
        log.info("Processing superbundles...");
        DbObject installManifest = layout.getValue("installManifest");
        if (installManifest != null) {
            for (Object installChunk : installManifest.<DbObject>getValue("installChunks")) {
                if (installChunk instanceof DbObject dbInstallChunk) {
                    if (dbInstallChunk.hasValue("testDLC") && dbInstallChunk.<Boolean>getValue("testDLC")) {
                        continue;
                    }

                    boolean alwaysInstalled = dbInstallChunk.<Boolean>getValue("alwaysInstalled");
                    String path = "win32/" + dbInstallChunk.<String>getValue("name");

                    String pathname = this.resolvePath(path + "/cas.cat");
                    if (pathname == null || !new File(pathname).exists()) {
                        continue;
                    }

                    UUID catalogId = dbInstallChunk.getValue("id");
                    CatalogInfo info = this.catalogs.stream().filter(catalog -> catalog.getId().equals(catalogId)).findFirst().orElse(null);

                    if (info == null) {
                        info = new CatalogInfo(catalogId, path, alwaysInstalled);

                        for (Object superBundle : dbInstallChunk.<DbObject>getValue("superbundles")) {
                            if (superBundle instanceof String superBundleName) {
                                info.getSuperBundles().put(superBundleName.toLowerCase(), false);
                            }
                        }
                    }

                    this.catalogs.add(info);

                    if (dbInstallChunk.hasValue("files")) {
                        for (Object file : dbInstallChunk.<DbObject>getValue("files")) {
                            if (file instanceof DbObject dbFile) {
                                int index = dbFile.getValue("id");
                                while (this.casFiles.size() <= index) {
                                    this.casFiles.add("");
                                }

                                String casPath = dbFile.getValue("path");
                                casPath = casPath.replace("native_data/Data", "native_data");
                                casPath = casPath.replace("native_data/Patch", "native_patch");

                                this.casFiles.set(index, casPath);
                            }
                        }
                    }

                    if (dbInstallChunk.hasValue("splitSuperBundles")) {
                        for (Object superBundleContainer : dbInstallChunk.<DbObject>getValue("splitSuperBundles")) {
                            if (superBundleContainer instanceof DbObject superBundleContainerObject) {
                                String superBundleName = superBundleContainerObject.<String>getValue("superBundle").toLowerCase();
                                if (!info.getSuperBundles().containsKey(superBundleName)) {
                                    info.getSuperBundles().put(superBundleName, true);
                                }
                            }
                        }
                    }

                    if (dbInstallChunk.hasValue("splitTocs")) {
                        for (Object tocContainer : dbInstallChunk.<DbObject>getValue("splitTocs")) {
                            if (tocContainer instanceof DbObject superBundleContainer) {
                                String superBundle = "win32/" + superBundleContainer.<String>getValue("superbundle").toLowerCase();
                                if (!info.getSuperBundles().containsKey(superBundle)) {
                                    info.getSuperBundles().put(superBundle, true);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            log.warn("No install manifest found");
            CatalogInfo ci = new CatalogInfo(null, "", false);
            for (String sbName : this.superBundles) {
                ci.getSuperBundles().put(sbName, false);
            }
            this.catalogs.add(ci);
        }
    }

    private void processManifest(DbObject layout) throws IOException {
        log.info("Processing manifest...");
        DbObject manifest = layout.getValue("manifest");

        if (manifest != null) {
            List<ManifestFileInfo> manifestFiles = new ArrayList<>();
            ManifestFileRef file = new ManifestFileRef(manifest.<Long>getValue("file").intValue());

            String manifestPath = this.resolvePath(file);
            File temp = new File(manifestPath);
            NativeReader reader = new NativeReader(Unpooled.wrappedBuffer(new FileInputStream(temp)
                    .getChannel().map(FileChannel.MapMode.READ_ONLY, 0, temp.length())));

            long manifestOffset = manifest.getValue("offset");

            reader.getBuf().readerIndex((int) manifestOffset);

            long fileCount = reader.readUInt();
            long bundleCount = reader.readUInt();
            long chunksCount = reader.readUInt();

            for (long i = 0; i < fileCount; i++) {
                ManifestFileInfo info = new ManifestFileInfo(
                        new ManifestFileRef(reader.readInt()),
                        reader.readUInt(),
                        reader.readLong()
                );
                manifestFiles.add(info);
            }

            for (long i = 0; i < bundleCount; i++) {
                ManifestBundleInfo info = new ManifestBundleInfo(reader.readInt());

                long startIndex = reader.readUInt();
                long count = reader.readUInt();

                int unk1 = reader.getBuf().readIntLE();
                int unk2 = reader.getBuf().readIntLE();

                for (int j = 0; j < count; j++) {
                    info.getFiles().add(manifestFiles.get((int) (startIndex + j)));
                }

                this.manifestBundles.add(info);
            }

            for (long i = 0; i < chunksCount; i++) {
                UUID guid = reader.readGuid();
                int fileIndex = reader.getBuf().readIntLE();
                ManifestFileInfo manifestFile = manifestFiles.get(fileIndex);
                manifestFile.setChunk(true);
                this.manifestChunks.add(new ManifestChunkInfo(guid, manifestFile, fileIndex));
            }
        }
    }

    @Data
    @RequiredArgsConstructor
    public static class ManifestFileRef {

        private final int value;

        public ManifestFileRef(int inIndex, boolean inPatch, int inCasIndex) {
            this.value = ((inIndex + 1) << 12) | (inPatch ? 0x100 : 0x00) | ((inCasIndex - 1) & 0xFF);
        }

        public int getCatalogIndex() {
            return (this.value >> 12) - 1;
        }

        public boolean isInPatch() {
            return (this.value & 0x100) != 0;
        }

        public int getCasIndex() {
            return (this.value & 0xFF) + 1;
        }

    }

    @Data
    public static class ManifestFileInfo {
        private final ManifestFileRef file;
        private final long offset;
        private final long size;

        @Setter
        private boolean isChunk;
    }

    @Data
    public static class ManifestBundleInfo {
        private final long hash;
        private List<ManifestFileInfo> files = new ArrayList<>();
    }

    @Data
    public static class ManifestChunkInfo {
        private final UUID guid;
        private final ManifestFileInfo file;
        private final int fileIndex;
    }

    @Data
    public static class CatalogInfo {
        private final UUID id;
        private final String name;
        private final boolean alwaysInstalled;
        private Map<String, Boolean> superBundles = new LinkedHashMap<>();
    }

}
