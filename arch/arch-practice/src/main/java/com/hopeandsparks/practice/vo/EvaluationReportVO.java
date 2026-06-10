package com.hopeandsparks.practice.vo;

import java.util.List;

public record EvaluationReportVO(String attemptId, String summary, List<String> weakPoints, boolean mock) {
}
