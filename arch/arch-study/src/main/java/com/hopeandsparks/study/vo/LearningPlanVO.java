package com.hopeandsparks.study.vo;

import java.util.List;

public record LearningPlanVO(String id, String title, String summary, List<String> dailyTasks, boolean mock) {
}
