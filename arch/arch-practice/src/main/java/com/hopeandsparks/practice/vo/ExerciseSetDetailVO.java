package com.hopeandsparks.practice.vo;

import java.util.List;

public record ExerciseSetDetailVO(String id, String title, List<String> questionIds, boolean mock) {
}
