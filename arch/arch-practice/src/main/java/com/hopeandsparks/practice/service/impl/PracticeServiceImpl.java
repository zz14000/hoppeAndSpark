package com.hopeandsparks.practice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.practice.dto.BlankAnswer;
import com.hopeandsparks.practice.dto.CoachHintRequest;
import com.hopeandsparks.practice.dto.CodeRunRequest;
import com.hopeandsparks.practice.dto.ExerciseSetFilter;
import com.hopeandsparks.practice.dto.PracticeAnswerRequest;
import com.hopeandsparks.practice.dto.PracticeSubmitAnswerItem;
import com.hopeandsparks.practice.dto.PracticeSubmitRequest;
import com.hopeandsparks.practice.dto.QuestionFlagRequest;
import com.hopeandsparks.practice.entity.EvaluationReport;
import com.hopeandsparks.practice.entity.PracticeQuestion;
import com.hopeandsparks.practice.entity.PracticeSet;
import com.hopeandsparks.practice.entity.UserQuestionRecord;
import com.hopeandsparks.practice.repository.PracticeRepository;
import com.hopeandsparks.practice.service.PracticeService;
import com.hopeandsparks.practice.vo.AnswerConfigVO;
import com.hopeandsparks.practice.vo.AnswerSaveVO;
import com.hopeandsparks.practice.vo.BlankSlotVO;
import com.hopeandsparks.practice.vo.CoachHintVO;
import com.hopeandsparks.practice.vo.CodeRunVO;
import com.hopeandsparks.practice.vo.CodeTestCaseVO;
import com.hopeandsparks.practice.vo.EvaluationReportVO;
import com.hopeandsparks.practice.vo.ExerciseSetCardVO;
import com.hopeandsparks.practice.vo.ExerciseSetDetailVO;
import com.hopeandsparks.practice.vo.FlagVO;
import com.hopeandsparks.practice.vo.KnowledgeScoreVO;
import com.hopeandsparks.practice.vo.PracticeSubmitVO;
import com.hopeandsparks.practice.vo.QuestionAnswerDetailVO;
import com.hopeandsparks.practice.vo.QuestionAnswerVO;
import com.hopeandsparks.practice.vo.QuestionOptionVO;
import com.hopeandsparks.practice.vo.QuestionResultVO;
import com.hopeandsparks.practice.vo.QuestionSummaryVO;
import com.hopeandsparks.practice.vo.SavedAnswerVO;
import com.hopeandsparks.practice.vo.TimerVO;
import com.hopeandsparks.study.dto.PracticeWeakPointFeedback;
import com.hopeandsparks.study.service.StudyFeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 练习评测的主流程实现。
 */
@Service
public class PracticeServiceImpl implements PracticeService {

    private static final int DEFAULT_TIME_LIMIT_SECONDS = 3600;
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[,，;；|/、\\n]+");

    private final PracticeRepository practiceRepository;
    private final StudyFeedbackService studyFeedbackService;
    private final ObjectMapper objectMapper;

    public PracticeServiceImpl(
            PracticeRepository practiceRepository,
            StudyFeedbackService studyFeedbackService,
            ObjectMapper objectMapper
    ) {
        this.practiceRepository = practiceRepository;
        this.studyFeedbackService = studyFeedbackService;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResponse<ExerciseSetCardVO> listSets(AuthenticatedPrincipal principal, Map<String, String> query) {
        Long userId = requireUserId(principal);
        long page = parseLong(queryValue(query, "page")).orElse(1L);
        long pageSize = parseLong(queryValue(query, "pageSize")).orElse(10L);
        page = Math.max(page, 1);
        pageSize = Math.max(1, Math.min(pageSize, 50));

        ExerciseSetFilter filter = new ExerciseSetFilter(
                parseLong(queryValue(query, "planId")).orElse(null),
                queryValue(query, "nodeId"),
                queryValue(query, "status"),
                queryValue(query, "type")
        );
        long total = practiceRepository.countSets(userId, filter);
        List<ExerciseSetCardVO> list = practiceRepository
                .listSets(userId, filter, (page - 1) * pageSize, pageSize)
                .stream()
                .map(set -> toCardVO(userId, set))
                .toList();
        return PageResponse.of(page, pageSize, total, list);
    }

    @Override
    public ExerciseSetDetailVO detail(AuthenticatedPrincipal principal, String exerciseSetId) {
        Long userId = requireUserId(principal);
        PracticeSet set = requireSet(exerciseSetId);
        List<PracticeQuestion> questions = practiceRepository.listQuestions(set.id());
        Map<Long, UserQuestionRecord> records = practiceRepository.latestRecordMap(userId, set.id());
        return toDetailVO(set, questions, records);
    }

    @Override
    public QuestionAnswerDetailVO question(AuthenticatedPrincipal principal, String exerciseSetId, String questionId) {
        Long userId = requireUserId(principal);
        PracticeSet set = requireSet(exerciseSetId);
        Long parsedQuestionId = requireId(questionId, "题目ID格式不正确");
        PracticeQuestion question = practiceRepository.findQuestionInSet(set.id(), parsedQuestionId)
                .orElseThrow(() -> new BusinessException(404, "题目不存在"));
        List<PracticeQuestion> questions = practiceRepository.listQuestions(set.id());
        UserQuestionRecord record = practiceRepository.findLatestRecord(userId, set.id(), question.id()).orElse(null);
        int number = questionNumber(questions, question.id());
        return new QuestionAnswerDetailVO(
                String.valueOf(set.id()),
                new QuestionAnswerVO(
                        String.valueOf(question.id()),
                        number,
                        apiQuestionType(question.questionType()),
                        questionScore(question),
                        question.contentText(),
                        optionVOs(question),
                        blankSlots(question),
                        answerConfig(question),
                        savedAnswer(record)
                ),
                new TimerVO(DEFAULT_TIME_LIMIT_SECONDS)
        );
    }

    @Override
    @Transactional
    public AnswerSaveVO saveAnswer(
            AuthenticatedPrincipal principal,
            String exerciseSetId,
            String questionId,
            PracticeAnswerRequest request
    ) {
        Long userId = requireUserId(principal);
        PracticeSet set = requireSet(exerciseSetId);
        Long parsedQuestionId = requireId(questionId, "题目ID格式不正确");
        PracticeQuestion question = practiceRepository.findQuestionInSet(set.id(), parsedQuestionId)
                .orElseThrow(() -> new BusinessException(404, "题目不存在"));
        UserQuestionRecord record = saveAnswerForQuestion(userId, set.id(), question, request);
        return new AnswerSaveVO(
                String.valueOf(record.id()),
                "answered",
                record.createdAt(),
                practiceRepository.countAnswered(userId, set.id())
        );
    }

    @Override
    @Transactional
    public FlagVO flagQuestion(
            AuthenticatedPrincipal principal,
            String exerciseSetId,
            String questionId,
            QuestionFlagRequest request
    ) {
        Long userId = requireUserId(principal);
        PracticeSet set = requireSet(exerciseSetId);
        Long parsedQuestionId = requireId(questionId, "题目ID格式不正确");
        PracticeQuestion question = practiceRepository.findQuestionInSet(set.id(), parsedQuestionId)
                .orElseThrow(() -> new BusinessException(404, "题目不存在"));
        boolean flagged = request != null && Boolean.TRUE.equals(request.flagged());
        practiceRepository.saveFlag(userId, set.id(), question.id(), flagged);
        return new FlagVO(String.valueOf(question.id()), flagged, practiceRepository.countFlagged(userId, set.id()));
    }

    @Override
    public CoachHintVO hint(
            AuthenticatedPrincipal principal,
            String exerciseSetId,
            String questionId,
            CoachHintRequest request
    ) {
        requireUserId(principal);
        PracticeSet set = requireSet(exerciseSetId);
        Long parsedQuestionId = requireId(questionId, "题目ID格式不正确");
        PracticeQuestion question = practiceRepository.findQuestionInSet(set.id(), parsedQuestionId)
                .orElseThrow(() -> new BusinessException(404, "题目不存在"));

        int level = request == null || request.hintLevel() == null ? 1 : Math.max(1, Math.min(request.hintLevel(), 2));
        String draft = draftText(request);
        String diagnosis = draft.isBlank()
                ? "当前还没有明显作答内容，先从题干关键词入手。"
                : "当前作答已经有思路，下一步要把它和标准概念对齐。";
        String hint1 = "这道题主要考查「" + question.nodeName() + "」。先回忆这个知识点的定义、条件和常见边界。";
        String hint2 = "再对照题干里的限制条件，把答案拆成“结论 + 原因 + 边界情况”三部分。";
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("task_id", "hint_" + set.id() + "_" + question.id());
        payload.put("knowledge_point", question.nodeName());
        payload.put("hint_level", level);
        payload.put("mock", true);
        return new CoachHintVO(
                "hint_" + set.id() + "_" + question.id(),
                "coach",
                "practice_hint",
                "ok",
                draft.isBlank() ? "no_answer" : "needs_refine",
                diagnosis,
                hint1,
                level >= 2 ? hint2 : null,
                question.analysisText(),
                "先按提示补一版答案，再提交给系统评测。",
                List.of(question.nodeName()),
                "慢慢来，先抓住题眼就已经推进了一大步。",
                payload
        );
    }

    @Override
    @Transactional
    public PracticeSubmitVO submit(AuthenticatedPrincipal principal, String exerciseSetId, PracticeSubmitRequest request) {
        Long userId = requireUserId(principal);
        PracticeSet set = requireSet(exerciseSetId);
        List<PracticeQuestion> questions = practiceRepository.listQuestions(set.id());
        if (questions.isEmpty()) {
            throw new BusinessException(400, "当前练习集没有题目");
        }

        saveSubmitAnswers(userId, set.id(), questions, request == null ? null : request.answers());

        Map<Long, UserQuestionRecord> records = practiceRepository.latestRecordMap(userId, set.id());
        List<Integer> unansweredNumbers = unansweredNumbers(questions, records);
        int answeredCount = questions.size() - unansweredNumbers.size();
        int flaggedCount = practiceRepository.countFlagged(userId, set.id());
        ScorePair objective = objectiveScore(questions, records);
        int usedSeconds = request == null || request.durationSeconds() == null ? 0 : Math.max(request.durationSeconds(), 0);

        if (!unansweredNumbers.isEmpty() && (request == null || !Boolean.TRUE.equals(request.forceSubmit()))) {
            return new PracticeSubmitVO(
                    null,
                    false,
                    usedSeconds,
                    answeredCount,
                    unansweredNumbers.size(),
                    flaggedCount,
                    objective.score(),
                    objective.totalScore(),
                    "not_submitted",
                    "还有题目未作答，请确认是否继续提交。",
                    unansweredNumbers
            );
        }

        ReportBuildResult reportResult = buildReport(set, questions, records);
        Long reportId = practiceRepository.insertReport(
                userId,
                set.courseId(),
                set.id(),
                reportResult.overallScore(),
                toJson(reportResult.knowledgeScores()),
                reportResult.abilitySummary(),
                reportResult.improvementSuggestion()
        );
        feedWeakPoints(userId, reportResult.weakPoints());
        return new PracticeSubmitVO(
                String.valueOf(reportId),
                true,
                usedSeconds,
                answeredCount,
                unansweredNumbers.size(),
                flaggedCount,
                objective.score(),
                objective.totalScore(),
                hasSubjective(questions) ? "reviewed" : "none",
                hasSubjective(questions) ? "系统已完成 mock Coach 评阅，报告已生成。" : "客观题已自动批改，报告已生成。",
                unansweredNumbers
        );
    }

    @Override
    public CodeRunVO codeRun(
            AuthenticatedPrincipal principal,
            String exerciseSetId,
            String questionId,
            CodeRunRequest request
    ) {
        requireUserId(principal);
        PracticeSet set = requireSet(exerciseSetId);
        Long parsedQuestionId = requireId(questionId, "题目ID格式不正确");
        PracticeQuestion question = practiceRepository.findQuestionInSet(set.id(), parsedQuestionId)
                .orElseThrow(() -> new BusinessException(404, "题目不存在"));
        if (!"code".equals(question.questionType())) {
            throw new BusinessException(400, "只有代码题可以进行代码评测");
        }
        String code = request == null || request.code() == null ? "" : request.code();
        boolean passed = looksLikeRunnableCode(code);
        int runtime = 40 + Math.min(code.length(), 100);
        int memory = 32 + Math.min(code.length() / 20, 32);
        String expected = "代码结构完整，包含核心分支";
        String actual = passed ? expected : "代码内容不足，暂未看到完整实现";
        String review = passed
                ? "MVP mock：代码结构看起来完整，可以继续补充边界情况说明。"
                : "MVP mock：当前代码还不完整，建议先写出函数主体和关键返回逻辑。";
        return new CodeRunVO(
                passed,
                runtime,
                memory,
                List.of(new CodeTestCaseVO("mock-static-review", expected, actual, passed)),
                review
        );
    }

    @Override
    public EvaluationReportVO report(AuthenticatedPrincipal principal, String attemptId) {
        Long userId = requireUserId(principal);
        Long parsedAttemptId = requireId(attemptId, "报告ID格式不正确");
        EvaluationReport report = practiceRepository.findReport(userId, parsedAttemptId)
                .or(() -> practiceRepository.findRecordById(userId, parsedAttemptId)
                        .flatMap(record -> record.practiceSetId() == null
                                ? Optional.empty()
                                : practiceRepository.findLatestReportForSet(userId, record.practiceSetId())))
                .orElseThrow(() -> new BusinessException(404, "评测报告不存在，请先提交练习"));
        PracticeSet set = practiceRepository.findSet(report.practiceSetId())
                .orElseThrow(() -> new BusinessException(404, "练习集不存在"));
        return toReportVO(report, set);
    }

    private ExerciseSetCardVO toCardVO(Long userId, PracticeSet set) {
        List<PracticeQuestion> questions = practiceRepository.listQuestions(set.id());
        Map<Long, UserQuestionRecord> records = practiceRepository.latestRecordMap(userId, set.id());
        return new ExerciseSetCardVO(
                String.valueOf(set.id()),
                set.setName(),
                apiSetType(set.setType()),
                firstKnowledgeNodeId(questions),
                questions.size(),
                DEFAULT_TIME_LIMIT_SECONDS,
                answeredCount(records),
                practiceRepository.countFlagged(userId, set.id()),
                objectiveScore(questions, records).score(),
                totalScore(questions),
                questionTypeStats(questions)
        );
    }

    private ExerciseSetDetailVO toDetailVO(
            PracticeSet set,
            List<PracticeQuestion> questions,
            Map<Long, UserQuestionRecord> records
    ) {
        List<QuestionSummaryVO> questionVOs = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            PracticeQuestion question = questions.get(i);
            questionVOs.add(toSummaryVO(set.id(), question, i + 1, records.get(question.id())));
        }
        return new ExerciseSetDetailVO(
                String.valueOf(set.id()),
                set.setName(),
                apiSetType(set.setType()),
                firstKnowledgeNodeId(questions),
                set.courseName(),
                DEFAULT_TIME_LIMIT_SECONDS,
                DEFAULT_TIME_LIMIT_SECONDS,
                questions.size(),
                answeredCount(records),
                (int) records.values().stream().filter(record -> Boolean.TRUE.equals(record.flagged())).count(),
                questionVOs
        );
    }

    private QuestionSummaryVO toSummaryVO(
            Long setId,
            PracticeQuestion question,
            int number,
            UserQuestionRecord record
    ) {
        String type = apiQuestionType(question.questionType());
        boolean dedicated = requiresDedicatedPage(question.questionType());
        String questionId = String.valueOf(question.id());
        return new QuestionSummaryVO(
                questionId,
                number,
                type,
                questionScore(question),
                question.contentText(),
                optionVOs(question),
                blankSlots(question),
                answerStatus(record),
                record != null && Boolean.TRUE.equals(record.flagged()),
                dedicated,
                dedicated ? "/app/exercises/" + setId + "/questions/" + questionId + "/answer" : null,
                "essay".equals(question.questionType()),
                "essay".equals(question.questionType()),
                language(question),
                starterCode(question)
        );
    }

    private UserQuestionRecord saveAnswerForQuestion(
            Long userId,
            Long setId,
            PracticeQuestion question,
            PracticeAnswerRequest request
    ) {
        PracticeAnswerRequest safeRequest = request == null
                ? new PracticeAnswerRequest(null, null, null, null, null, null, null, null, null, null, null)
                : request;
        Map<String, Object> payload = answerPayload(question, safeRequest);
        GradeResult grade = grade(question, payload);
        boolean flagged = Boolean.TRUE.equals(safeRequest.flagged())
                || practiceRepository.findLatestRecord(userId, setId, question.id())
                .map(UserQuestionRecord::flagged)
                .orElse(false);
        return practiceRepository.saveRecord(
                userId,
                setId,
                question.id(),
                toJson(payload),
                grade.correct(),
                grade.score(),
                grade.judgeMode(),
                grade.feedback(),
                flagged
        );
    }

    private Map<String, Object> answerPayload(PracticeQuestion question, PracticeAnswerRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", apiQuestionType(question.questionType()));
        if (request.answer() != null) {
            payload.put("answer", request.answer());
        }
        if (request.selectedOptionKeys() != null) {
            payload.put("selectedOptionKeys", request.selectedOptionKeys());
        }
        if (request.blanks() != null) {
            payload.put("blanks", request.blanks());
        }
        if (request.content() != null) {
            payload.put("content", request.content());
        }
        if (request.contentFormat() != null) {
            payload.put("contentFormat", request.contentFormat());
        }
        if (request.attachments() != null) {
            payload.put("attachments", request.attachments());
        }
        if (request.language() != null) {
            payload.put("language", request.language());
        }
        if (request.code() != null) {
            payload.put("code", request.code());
        }
        if (request.durationSeconds() != null) {
            payload.put("durationSeconds", request.durationSeconds());
        }
        return payload;
    }

    private GradeResult grade(PracticeQuestion question, Object payload) {
        BigDecimal total = questionScore(question);
        if (!hasAnswer(payload)) {
            return new GradeResult(false, BigDecimal.ZERO, judgeMode(question), "未作答，暂时记为 0 分。");
        }
        if (isObjective(question.questionType())) {
            List<String> userValues = answerValues(payload, question.questionType());
            List<String> standardValues = standardValues(question.standardAnswer(), question.questionType());
            boolean correct = !standardValues.isEmpty() && compareAnswer(userValues, standardValues, question.questionType());
            return new GradeResult(
                    correct,
                    correct ? total : BigDecimal.ZERO,
                    "auto",
                    correct ? "回答正确，继续保持。" : objectiveFeedback(question)
            );
        }
        return mockSubjectiveGrade(question, payload, total);
    }

    private GradeResult mockSubjectiveGrade(PracticeQuestion question, Object payload, BigDecimal total) {
        String answer = answerText(payload, question.questionType());
        int percent = 55;
        if (answer.length() >= 30) {
            percent += 15;
        }
        if (answer.length() >= 100) {
            percent += 10;
        }
        if (containsStandardKeyword(answer, question.standardAnswer())) {
            percent += 10;
        }
        if ("code".equals(question.questionType()) && looksLikeRunnableCode(answer)) {
            percent += 10;
        }
        percent = Math.min(percent, 90);
        BigDecimal score = total.multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String feedback = "Coach mock 评阅：答案覆盖度约 " + percent + "%。"
                + ("code".equals(question.questionType()) ? "当前未接入真实沙箱，只做代码文本评阅。" : "建议继续补充关键步骤和边界条件。");
        return new GradeResult(percent >= 70, score, "ai", feedback);
    }

    private void saveSubmitAnswers(
            Long userId,
            Long setId,
            List<PracticeQuestion> questions,
            List<PracticeSubmitAnswerItem> answers
    ) {
        if (answers == null || answers.isEmpty()) {
            return;
        }
        Map<Long, PracticeQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(PracticeQuestion::id, question -> question));
        for (PracticeSubmitAnswerItem item : answers) {
            if (item == null || item.questionId() == null) {
                continue;
            }
            Long questionId = requireId(item.questionId(), "题目ID格式不正确");
            PracticeQuestion question = questionMap.get(questionId);
            if (question == null) {
                throw new BusinessException(404, "提交答案中包含不属于本练习集的题目");
            }
            saveAnswerForQuestion(userId, setId, question, item.toAnswerRequest());
        }
    }

    private ReportBuildResult buildReport(
            PracticeSet set,
            List<PracticeQuestion> questions,
            Map<Long, UserQuestionRecord> records
    ) {
        List<KnowledgeScoreVO> knowledgeScores = knowledgeScores(questions, records);
        BigDecimal totalScore = totalScore(questions);
        BigDecimal score = records.values().stream()
                .map(UserQuestionRecord::score)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overall = totalScore.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : score.multiply(BigDecimal.valueOf(100)).divide(totalScore, 2, RoundingMode.HALF_UP);
        List<KnowledgeScoreVO> weakPoints = weakPoints(knowledgeScores);
        String abilitySummary = abilitySummary(overall, set.setName());
        String improvement = improvementSuggestion(weakPoints);
        return new ReportBuildResult(overall, knowledgeScores, weakPoints, abilitySummary, improvement);
    }

    private EvaluationReportVO toReportVO(EvaluationReport report, PracticeSet set) {
        List<PracticeQuestion> questions = practiceRepository.listQuestions(set.id());
        Map<Long, UserQuestionRecord> records = practiceRepository.latestRecordMap(report.userId(), set.id());
        List<KnowledgeScoreVO> knowledgeScores = parseKnowledgeScores(report.knowledgeScoreJson())
                .orElseGet(() -> knowledgeScores(questions, records));
        List<KnowledgeScoreVO> weakPoints = weakPoints(knowledgeScores);
        ScorePair objective = objectiveScore(questions, records);
        BigDecimal totalScore = totalScore(questions);
        BigDecimal submittedScore = records.values().stream()
                .map(UserQuestionRecord::score)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subjectiveScore = submittedScore.subtract(objective.score()).max(BigDecimal.ZERO);
        return new EvaluationReportVO(
                String.valueOf(report.id()),
                String.valueOf(report.id()),
                String.valueOf(set.id()),
                set.setName(),
                report.overallScore(),
                totalScore,
                objective.score(),
                subjectiveScore,
                report.abilitySummary(),
                report.improvementSuggestion(),
                knowledgeScores,
                weakPoints,
                questionResults(questions, records),
                report.generatedBy() == null ? "Coach" : report.generatedBy(),
                report.createdAt()
        );
    }

    private List<QuestionResultVO> questionResults(
            List<PracticeQuestion> questions,
            Map<Long, UserQuestionRecord> records
    ) {
        List<QuestionResultVO> result = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            PracticeQuestion question = questions.get(i);
            UserQuestionRecord record = records.get(question.id());
            result.add(new QuestionResultVO(
                    String.valueOf(question.id()),
                    i + 1,
                    apiQuestionType(question.questionType()),
                    String.valueOf(question.nodeId()),
                    question.nodeName(),
                    record == null ? null : readAnswer(record.userAnswer()),
                    record == null ? false : Boolean.TRUE.equals(record.correct()),
                    record == null || record.score() == null ? BigDecimal.ZERO : record.score(),
                    questionScore(question),
                    record == null ? judgeMode(question) : record.judgeMode(),
                    record == null ? "未作答。" : record.feedbackText(),
                    record != null && Boolean.TRUE.equals(record.flagged())
            ));
        }
        return result;
    }

    private List<KnowledgeScoreVO> knowledgeScores(
            List<PracticeQuestion> questions,
            Map<Long, UserQuestionRecord> records
    ) {
        Map<Long, KnowledgeScoreBuilder> builders = new LinkedHashMap<>();
        for (PracticeQuestion question : questions) {
            KnowledgeScoreBuilder builder = builders.computeIfAbsent(
                    question.nodeId(),
                    key -> new KnowledgeScoreBuilder(question.nodeId(), question.nodeName())
            );
            UserQuestionRecord record = records.get(question.id());
            builder.add(questionScore(question), record == null ? null : record.score(), record == null || !Boolean.TRUE.equals(record.correct()));
        }
        return builders.values().stream()
                .map(KnowledgeScoreBuilder::toVO)
                .toList();
    }

    private ScorePair objectiveScore(List<PracticeQuestion> questions, Map<Long, UserQuestionRecord> records) {
        BigDecimal score = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (PracticeQuestion question : questions) {
            if (!isObjective(question.questionType())) {
                continue;
            }
            total = total.add(questionScore(question));
            UserQuestionRecord record = records.get(question.id());
            if (record != null && record.score() != null) {
                score = score.add(record.score());
            }
        }
        return new ScorePair(score, total);
    }

    private BigDecimal totalScore(List<PracticeQuestion> questions) {
        return questions.stream()
                .map(this::questionScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<KnowledgeScoreVO> weakPoints(List<KnowledgeScoreVO> knowledgeScores) {
        return knowledgeScores.stream()
                .filter(score -> score.accuracy().compareTo(BigDecimal.valueOf(80)) < 0 || score.wrongCount() > 0)
                .toList();
    }

    private void feedWeakPoints(Long userId, List<KnowledgeScoreVO> weakPoints) {
        List<PracticeWeakPointFeedback> feedback = weakPoints.stream()
                .map(point -> new PracticeWeakPointFeedback(
                        parseLong(point.nodeId()).orElse(null),
                        point.nodeName(),
                        point.accuracy(),
                        point.score(),
                        point.totalScore(),
                        point.wrongCount(),
                        point.questionCount(),
                        point.suggestion()
                ))
                .toList();
        studyFeedbackService.acceptPracticeWeakPoints(userId, feedback);
    }

    private List<Integer> unansweredNumbers(List<PracticeQuestion> questions, Map<Long, UserQuestionRecord> records) {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            UserQuestionRecord record = records.get(questions.get(i).id());
            if (!isAnswered(record)) {
                numbers.add(i + 1);
            }
        }
        return numbers;
    }

    private int answeredCount(Map<Long, UserQuestionRecord> records) {
        return (int) records.values().stream().filter(this::isAnswered).count();
    }

    private boolean isAnswered(UserQuestionRecord record) {
        if (record == null || record.userAnswer() == null) {
            return false;
        }
        String answer = record.userAnswer().trim();
        return !answer.isBlank() && !"{}".equals(answer);
    }

    private String answerStatus(UserQuestionRecord record) {
        return isAnswered(record) ? "answered" : "not_started";
    }

    private SavedAnswerVO savedAnswer(UserQuestionRecord record) {
        if (!isAnswered(record)) {
            return null;
        }
        return new SavedAnswerVO(String.valueOf(record.id()), readAnswer(record.userAnswer()), List.of(), record.createdAt());
    }

    private List<QuestionOptionVO> optionVOs(PracticeQuestion question) {
        if (question.optionsJson() == null || question.optionsJson().isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(question.optionsJson());
            JsonNode options = root.has("options") ? root.get("options") : root;
            if (options.isArray()) {
                List<QuestionOptionVO> result = new ArrayList<>();
                int index = 0;
                for (JsonNode item : options) {
                    String key = text(item, "key");
                    if (key == null) {
                        key = String.valueOf((char) ('A' + index));
                    }
                    String content = text(item, "content");
                    if (content == null) {
                        content = item.isTextual() ? item.asText() : item.toString();
                    }
                    result.add(new QuestionOptionVO(key, content));
                    index++;
                }
                return result;
            }
            if (options.isObject()) {
                List<QuestionOptionVO> result = new ArrayList<>();
                options.fields().forEachRemaining(entry -> result.add(new QuestionOptionVO(entry.getKey(), entry.getValue().asText())));
                return result;
            }
        } catch (JsonProcessingException ignored) {
            return List.of();
        }
        return List.of();
    }

    private List<BlankSlotVO> blankSlots(PracticeQuestion question) {
        if (!"fill".equals(question.questionType())) {
            return List.of();
        }
        int count = Math.max(1, standardValues(question.standardAnswer(), question.questionType()).size());
        List<BlankSlotVO> blanks = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            blanks.add(new BlankSlotVO("b" + i, "空 " + i));
        }
        return blanks;
    }

    private AnswerConfigVO answerConfig(PracticeQuestion question) {
        boolean essay = "essay".equals(question.questionType());
        boolean code = "code".equals(question.questionType());
        return new AnswerConfigVO(
                essay,
                essay,
                essay ? 4 : 0,
                essay ? 5 : 0,
                30,
                code ? language(question) : null,
                code ? starterCode(question) : null
        );
    }

    private List<String> answerValues(Object payload, String questionType) {
        Object value = payload;
        if (payload instanceof Map<?, ?> map) {
            if (map.get("selectedOptionKeys") != null) {
                value = map.get("selectedOptionKeys");
            } else if (map.get("blanks") != null) {
                return blankValues(map.get("blanks"));
            } else if (map.get("answer") != null) {
                value = map.get("answer");
            } else if (map.get("content") != null) {
                value = map.get("content");
            } else if (map.get("code") != null) {
                value = map.get("code");
            }
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(item -> normalizeAnswer(String.valueOf(item))).filter(text -> !text.isBlank()).toList();
        }
        String text = value == null ? "" : String.valueOf(value);
        if ("multi".equals(questionType) || "fill".equals(questionType)) {
            return splitValues(text);
        }
        String normalized = normalizeAnswer(text);
        return normalized.isBlank() ? List.of() : List.of(normalized);
    }

    private List<String> blankValues(Object blanks) {
        if (blanks instanceof Collection<?> collection) {
            return collection.stream()
                    .map(item -> {
                        if (item instanceof BlankAnswer blank) {
                            return blank.content();
                        }
                        if (item instanceof Map<?, ?> map && map.get("content") != null) {
                            return String.valueOf(map.get("content"));
                        }
                        return String.valueOf(item);
                    })
                    .map(this::normalizeAnswer)
                    .filter(text -> !text.isBlank())
                    .toList();
        }
        return splitValues(String.valueOf(blanks));
    }

    private List<String> standardValues(String standardAnswer, String questionType) {
        if (standardAnswer == null || standardAnswer.isBlank()) {
            return List.of();
        }
        try {
            Object parsed = objectMapper.readValue(standardAnswer, Object.class);
            List<String> values = answerValues(parsed, questionType);
            if (!values.isEmpty()) {
                return values;
            }
        } catch (JsonProcessingException ignored) {
        }
        if ("multi".equals(questionType) || "fill".equals(questionType)) {
            return splitValues(standardAnswer);
        }
        return List.of(normalizeAnswer(standardAnswer));
    }

    private boolean compareAnswer(List<String> userValues, List<String> standardValues, String questionType) {
        if ("multi".equals(questionType)) {
            Set<String> userSet = new LinkedHashSet<>(userValues);
            Set<String> standardSet = new LinkedHashSet<>(standardValues);
            return userSet.equals(standardSet);
        }
        return userValues.equals(standardValues);
    }

    private List<String> splitValues(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return SPLIT_PATTERN.splitAsStream(text)
                .map(this::normalizeAnswer)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String answerText(Object payload, String questionType) {
        if (payload instanceof Map<?, ?> map) {
            Object value = "code".equals(questionType) ? map.get("code") : map.get("content");
            if (value == null) {
                value = map.get("answer");
            }
            return value == null ? "" : String.valueOf(value);
        }
        return payload == null ? "" : String.valueOf(payload);
    }

    private boolean hasAnswer(Object payload) {
        if (payload instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .filter(entry -> !"type".equals(entry.getKey()))
                    .anyMatch(entry -> entry.getValue() != null && !String.valueOf(entry.getValue()).isBlank());
        }
        return payload != null && !String.valueOf(payload).isBlank();
    }

    private Object readAnswer(String answerJson) {
        if (answerJson == null || answerJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(answerJson, Object.class);
        } catch (JsonProcessingException exception) {
            return answerJson;
        }
    }

    private Optional<List<KnowledgeScoreVO>> parseKnowledgeScores(String json) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, new TypeReference<>() {
            }));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(500, "练习数据序列化失败");
        }
    }

    private String objectiveFeedback(PracticeQuestion question) {
        if (question.analysisText() != null && !question.analysisText().isBlank()) {
            return "回答不正确。解析：" + question.analysisText();
        }
        return "回答不正确，建议回看「" + question.nodeName() + "」相关内容。";
    }

    private boolean containsStandardKeyword(String answer, String standardAnswer) {
        if (answer == null || standardAnswer == null || standardAnswer.isBlank()) {
            return false;
        }
        String lowerAnswer = answer.toLowerCase();
        return splitValues(standardAnswer).stream().anyMatch(value -> value.length() >= 2 && lowerAnswer.contains(value.toLowerCase()));
    }

    private boolean looksLikeRunnableCode(String code) {
        if (code == null) {
            return false;
        }
        String safeCode = code.trim();
        return safeCode.length() >= 20
                && safeCode.contains("{")
                && safeCode.contains("}")
                && (safeCode.contains("return") || safeCode.contains("=") || safeCode.contains("class"));
    }

    private String draftText(CoachHintRequest request) {
        if (request == null) {
            return "";
        }
        if (request.currentCode() != null && !request.currentCode().isBlank()) {
            return request.currentCode();
        }
        if (request.currentAnswer() != null) {
            return String.valueOf(request.currentAnswer());
        }
        return request.question() == null ? "" : request.question();
    }

    private boolean hasSubjective(List<PracticeQuestion> questions) {
        return questions.stream().anyMatch(question -> !isObjective(question.questionType()));
    }

    private boolean isObjective(String questionType) {
        return "single".equals(questionType) || "multi".equals(questionType) || "fill".equals(questionType);
    }

    private boolean requiresDedicatedPage(String questionType) {
        return "fill".equals(questionType) || "essay".equals(questionType) || "code".equals(questionType);
    }

    private BigDecimal questionScore(PracticeQuestion question) {
        return switch (question.questionType()) {
            case "single" -> BigDecimal.valueOf(2);
            case "multi" -> BigDecimal.valueOf(3);
            case "fill" -> BigDecimal.valueOf(2);
            case "essay" -> BigDecimal.valueOf(5);
            case "code" -> BigDecimal.valueOf(10);
            default -> BigDecimal.valueOf(2);
        };
    }

    private String judgeMode(PracticeQuestion question) {
        return isObjective(question.questionType()) ? "auto" : "ai";
    }

    private String apiQuestionType(String dbType) {
        return switch (dbType == null ? "" : dbType) {
            case "single" -> "single_choice";
            case "multi" -> "multiple_choice";
            case "fill" -> "fill_blank";
            case "essay" -> "short_answer";
            case "code" -> "code";
            default -> dbType;
        };
    }

    private String apiSetType(String dbType) {
        return switch (dbType == null ? "" : dbType) {
            case "daily", "review", "practice" -> "practice";
            case "mock", "exam", "test" -> "test";
            case "challenge" -> "challenge";
            case "code_lab" -> "code_lab";
            default -> dbType;
        };
    }

    private String firstKnowledgeNodeId(List<PracticeQuestion> questions) {
        return questions.stream()
                .findFirst()
                .map(question -> question.nodeCode() == null ? String.valueOf(question.nodeId()) : question.nodeCode())
                .orElse(null);
    }

    private Map<String, Long> questionTypeStats(List<PracticeQuestion> questions) {
        return questions.stream()
                .collect(Collectors.groupingBy(
                        question -> apiQuestionType(question.questionType()),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    private int questionNumber(List<PracticeQuestion> questions, Long questionId) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).id().equals(questionId)) {
                return i + 1;
            }
        }
        return 1;
    }

    private String language(PracticeQuestion question) {
        return optionField(question, "language").orElse("java");
    }

    private String starterCode(PracticeQuestion question) {
        return optionField(question, "starterCode")
                .or(() -> optionField(question, "starter_code"))
                .orElse("class Solution {\n    // write your answer here\n}");
    }

    private Optional<String> optionField(PracticeQuestion question, String field) {
        if (question.optionsJson() == null || question.optionsJson().isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(question.optionsJson());
            JsonNode value = root.get(field);
            return value == null || value.isNull() ? Optional.empty() : Optional.of(value.asText());
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String normalizeAnswer(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String abilitySummary(BigDecimal overall, String setName) {
        if (overall.compareTo(BigDecimal.valueOf(85)) >= 0) {
            return "本次《" + setName + "》整体掌握较好，可以进入更高难度练习。";
        }
        if (overall.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "本次《" + setName + "》基础已建立，但部分知识点还需要复盘。";
        }
        return "本次《" + setName + "》暴露出明显薄弱点，建议先回看相关知识点再继续刷题。";
    }

    private String improvementSuggestion(List<KnowledgeScoreVO> weakPoints) {
        if (weakPoints.isEmpty()) {
            return "保持当前节奏，下一轮可以尝试限时练习或综合题。";
        }
        String names = weakPoints.stream().map(KnowledgeScoreVO::nodeName).limit(3).collect(Collectors.joining("、"));
        return "建议优先复盘：" + names + "。先看讲义，再做 2-3 道同类题巩固。";
    }

    private PracticeSet requireSet(String exerciseSetId) {
        Long setId = requireId(exerciseSetId, "练习集ID格式不正确");
        return practiceRepository.findSet(setId)
                .orElseThrow(() -> new BusinessException(404, "练习集不存在"));
    }

    private Long requireId(String value, String message) {
        return parseLong(value).orElseThrow(() -> new BusinessException(400, message));
    }

    private Optional<Long> parseLong(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String safeValue = value.trim();
        String digits = safeValue.matches("\\d+") ? safeValue : safeValue.replaceFirst("^.*_(\\d+)$", "$1");
        if (!digits.matches("\\d+")) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(digits));
    }

    private String queryValue(Map<String, String> query, String key) {
        return query == null ? null : query.get(key);
    }

    private Long requireUserId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.USER) {
            throw new BusinessException(401, "请先登录前台账号");
        }
        return principal.id();
    }

    private record GradeResult(Boolean correct, BigDecimal score, String judgeMode, String feedback) {
    }

    private record ScorePair(BigDecimal score, BigDecimal totalScore) {
    }

    private record ReportBuildResult(
            BigDecimal overallScore,
            List<KnowledgeScoreVO> knowledgeScores,
            List<KnowledgeScoreVO> weakPoints,
            String abilitySummary,
            String improvementSuggestion
    ) {
    }

    private static final class KnowledgeScoreBuilder {
        private final Long nodeId;
        private final String nodeName;
        private BigDecimal score = BigDecimal.ZERO;
        private BigDecimal total = BigDecimal.ZERO;
        private int wrongCount;
        private int questionCount;

        private KnowledgeScoreBuilder(Long nodeId, String nodeName) {
            this.nodeId = nodeId;
            this.nodeName = nodeName;
        }

        private void add(BigDecimal questionTotal, BigDecimal questionScore, boolean wrong) {
            questionCount++;
            total = total.add(questionTotal == null ? BigDecimal.ZERO : questionTotal);
            score = score.add(questionScore == null ? BigDecimal.ZERO : questionScore);
            if (wrong) {
                wrongCount++;
            }
        }

        private KnowledgeScoreVO toVO() {
            BigDecimal accuracy = total.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : score.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
            String status;
            String suggestion;
            if (accuracy.compareTo(BigDecimal.valueOf(85)) >= 0) {
                status = "mastered";
                suggestion = "掌握较好，可做综合题保持手感。";
            } else if (accuracy.compareTo(BigDecimal.valueOf(60)) >= 0) {
                status = "learning";
                suggestion = "基本理解，建议再做同类题巩固。";
            } else {
                status = "review";
                suggestion = "薄弱点明显，建议先回看概念和例题。";
            }
            return new KnowledgeScoreVO(
                    String.valueOf(nodeId),
                    nodeName,
                    score,
                    total,
                    accuracy,
                    status,
                    wrongCount,
                    questionCount,
                    suggestion
            );
        }
    }
}
