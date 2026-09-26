package com.novamc.launcher.version;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class InstallationManager {
    private final Path root;
    private final VersionManager versions;

    public InstallationManager(Path root, VersionManager versions) { this.root = root; this.versions = versions; }
    public Path root() { return root; }
    public Path instance(String id) { return root.resolve("instances").resolve(id); }

    public boolean isInstalled(String id) { return Files.isRegularFile(instance(id).resolve("version.json")); }

    public Path install(MinecraftVersion version) throws IOException, InterruptedException {
        Path dir = instance(version.id());
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("installation.properties"),
            "id=" + version.id() + "\ntype=" + version.type() + "\nmanagedBy=NovaMC\n",
            StandardCharsets.UTF_8);
        return versions.downloadVersionMetadata(version, dir);
    }

    public void repair(String id) throws IOException, InterruptedException {
        if (!isInstalled(id)) throw new IOException("Installation not found: " + id);
        // Repair is intentionally metadata-only until authenticated runtime download is implemented in Phase 3.
        Files.setLastModifiedTime(instance(id).resolve("version.json"), java.nio.file.attribute.FileTime.fromMillis(System.currentTimeMillis()));
    }

    public void remove(String id) throws IOException {
        Path dir = instance(id);
        if (!Files.exists(dir)) return;
        try (var stream = Files.walk(dir)) {
            stream.sorted(java.util.Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException e) { throw new RuntimeException(e); } });
        }
    }

    public List<String> installed() throws IOException {
        Path instances = root.resolve("instances");
        if (!Files.isDirectory(instances)) return List.of();
        try (var stream = Files.list(instances)) {
            return stream.filter(Files::isDirectory).map(p -> p.getFileName().toString()).sorted().toList();
        }
    }
}
