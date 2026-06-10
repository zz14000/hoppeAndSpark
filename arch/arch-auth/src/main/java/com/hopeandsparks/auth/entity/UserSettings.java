package com.hopeandsparks.auth.entity;

public record UserSettings(
        Long id,
        Long userId,
        boolean enableTts,
        boolean enableAvaPopup,
        boolean enableFocusMode,
        boolean publicCollection,
        String themeMode,
        String fontScale
) {
}
