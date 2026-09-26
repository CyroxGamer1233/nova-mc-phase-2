package com.novamc.common;

public record NovaConfig(String version, String profile, int ramMb) {
    public static NovaConfig defaults() {
        return new NovaConfig("0.1.0-phase1", "Default", 2048);
    }
}
