package com.hopeandsparks.profile.vo;

import java.util.List;

public record OnboardingQuestionVO(String id, String title, String type, List<String> options) {
}
