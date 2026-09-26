package com.novamc.launcher.version;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class JavaRuntimeDetector {
    public record JavaRuntime(String executable, String version, String vendor) {
        @Override public String toString() { return version + " — " + vendor + " (" + executable + ")"; }
    }

    public List<JavaRuntime> detect() {
        List<JavaRuntime> result = new ArrayList<>();
        String javaHome = System.getProperty("java.home");
        if (javaHome != null) result.add(read(javaHome + javaBin()));
        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(java.io.File.pathSeparator)) {
                String candidate = dir + javaBin();
                if (result.stream().noneMatch(r -> r.executable().equalsIgnoreCase(candidate))) result.add(read(candidate));
            }
        }
        return result.stream().filter(r -> r != null).toList();
    }

    private String javaBin() { return System.getProperty("os.name").toLowerCase().contains("win") ? "\\java.exe" : "/java"; }

    private JavaRuntime read(String executable) {
        try {
            Process p = new ProcessBuilder(executable, "-version").redirectErrorStream(true).start();
            StringBuilder out = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line; while ((line = r.readLine()) != null) out.append(line).append('\n');
            }
            p.waitFor();
            String first = out.toString().lines().findFirst().orElse("unknown");
            return new JavaRuntime(executable, first, "Detected JVM");
        } catch (Exception ignored) { return null; }
    }
}
