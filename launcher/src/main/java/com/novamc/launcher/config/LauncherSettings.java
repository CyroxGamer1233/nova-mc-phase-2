package com.novamc.launcher.config;

import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LauncherSettings {
    public String selectedVersion = "";
    public int ramMb = 2048;
    public String javaExecutable = "";
    public String instanceRoot = "";

    public static LauncherSettings load(Path file) {
        try {
            if (Files.isRegularFile(file)) return new com.google.gson.Gson().fromJson(Files.readString(file), LauncherSettings.class);
        } catch (Exception ignored) {}
        return new LauncherSettings();
    }

    public void save(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(this), StandardCharsets.UTF_8);
    }
}
