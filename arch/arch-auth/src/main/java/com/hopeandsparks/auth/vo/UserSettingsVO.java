package com.hopeandsparks.auth.vo;

import java.util.Map;

public record UserSettingsVO(
        Map<String, Object> base,
        Map<String, Object> agent,
        Map<String, Object> theme,
        Map<String, Object> notification,
        Map<String, Object> privacy,
        Map<String, Object> learningPreference
) {
}
