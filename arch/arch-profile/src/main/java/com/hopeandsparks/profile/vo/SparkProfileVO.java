package com.hopeandsparks.profile.vo;

import java.util.List;

public record SparkProfileVO(
        String userId,
        String majorDomain,
        String learningGoal,
        String currentLevel,
        List<String> weakPoints,
        boolean mock
) {
}
