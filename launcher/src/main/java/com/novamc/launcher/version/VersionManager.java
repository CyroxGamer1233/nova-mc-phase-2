package com.novamc.launcher.version;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class VersionManager {
    public static final String MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private final Gson gson = new Gson();
    private List<MinecraftVersion> cached = List.of();

    public List<MinecraftVersion> fetchVersions() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(MANIFEST_URL))
            .timeout(Duration.ofSeconds(30)).header("User-Agent", "NovaMC/0.2.0").GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) throw new IOException("Version manifest HTTP " + response.statusCode());
        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray versions = root.getAsJsonArray("versions");
        List<MinecraftVersion> result = new ArrayList<>();
        for (var element : versions) {
            JsonObject v = element.getAsJsonObject();
            result.add(new MinecraftVersion(
                v.get("id").getAsString(),
                v.get("type").getAsString(),
                v.get("url").getAsString(),
                v.get("releaseTime").getAsString()));
        }
        cached = result.stream().sorted(Comparator.comparing(MinecraftVersion::id).reversed()).toList();
        return cached;
    }

    public List<MinecraftVersion> cachedVersions() { return cached; }

    public Path downloadVersionMetadata(MinecraftVersion version, Path instanceDir) throws IOException, InterruptedException {
        Files.createDirectories(instanceDir);
        HttpRequest request = HttpRequest.newBuilder(URI.create(version.url()))
            .timeout(Duration.ofSeconds(30)).header("User-Agent", "NovaMC/0.2.0").GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) throw new IOException("Version metadata HTTP " + response.statusCode());
        Path target = instanceDir.resolve("version.json");
        Files.writeString(target, response.body(), StandardCharsets.UTF_8);
        return target;
    }

    public String latestRelease() throws IOException, InterruptedException {
        if (cached.isEmpty()) fetchVersions();
        return cached.stream().filter(v -> "release".equalsIgnoreCase(v.type())).findFirst()
            .map(MinecraftVersion::id).orElse(cached.isEmpty() ? "Unknown" : cached.getFirst().id());
    }
}
