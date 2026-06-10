package com.hopeandsparks.auth.dto;

import java.util.Map;

public record UserSettingsUpdateRequest(
        Boolean enableTts,
        Boolean enableAvaPopup,
        Boolean enableFocusMode,
        Boolean publicCollection,
        String themeMode,
        String fontScale,
        Map<String, Object> base,
        Map<String, Object> agent,
        Map<String, Object> theme,
        Map<String, Object> notification,
        Map<String, Object> privacy,
        Map<String, Object> learningPreference
) {
}
