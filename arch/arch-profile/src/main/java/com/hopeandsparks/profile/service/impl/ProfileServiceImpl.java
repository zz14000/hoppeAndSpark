package com.hopeandsparks.profile.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hopeandsparks.common.exception.BusinessException;
import com.hopeandsparks.common.response.PageResponse;
import com.hopeandsparks.infra.security.AuthenticatedPrincipal;
import com.hopeandsparks.infra.security.IdentityType;
import com.hopeandsparks.profile.dto.CollectionToggleRequest;
import com.hopeandsparks.profile.dto.OnboardingAnswerRequest;
import com.hopeandsparks.profile.dto.OnboardingCompleteRequest;
import com.hopeandsparks.profile.dto.SparkProfileRebuildRequest;
import com.hopeandsparks.profile.dto.UserProfileUpdateRequest;
import com.hopeandsparks.profile.entity.CollectionItem;
import com.hopeandsparks.profile.entity.LearningPlanStat;
import com.hopeandsparks.profile.entity.SparkProfile;
import com.hopeandsparks.profile.entity.UserProfileDetail;
import com.hopeandsparks.profile.repository.ProfileRepository;
import com.hopeandsparks.profile.service.ProfileService;
import com.hopeandsparks.profile.vo.CollectionItemVO;
import com.hopeandsparks.profile.vo.LearningPlanStatVO;
import com.hopeandsparks.profile.vo.LearningStatsVO;
import com.hopeandsparks.profile.vo.OnboardingAnswerVO;
import com.hopeandsparks.profile.vo.OnboardingQuestionVO;
import com.hopeandsparks.profile.vo.SparkProfileVO;
import com.hopeandsparks.profile.vo.UserHomeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final List<OnboardingQuestionVO> QUESTIONS = List.of(
            new OnboardingQuestionVO("grade_stage", "你当前的年级或阶段是什么？", "single_choice",
                    List.of("高中生", "大学生", "职场新人", "资深开发者")),
            new OnboardingQuestionVO("learning_domain", "接下来主要想专注的学习领域是什么？", "single_choice",
                    List.of("前端开发", "后端架构", "人工智能", "数据结构与算法")),
            new OnboardingQuestionVO("knowledge_base", "你目前具备怎样的基础知识？", "single_choice",
                    List.of("零基础小白", "了解一些概念", "写过简单 Demo", "有实战经验")),
            new OnboardingQuestionVO("cognitive_style", "你更喜欢怎样理解新知识？", "single_choice",
                    List.of("先看例子", "先看原理", "动手试错", "图示梳理")),
            new OnboardingQuestionVO("learning_preference", "你希望学习材料更偏向哪种形式？", "single_choice",
                    List.of("短文档", "视频讲解", "练习驱动", "项目实战")),
            new OnboardingQuestionVO("learning_goal", "这轮学习最想达成什么目标？", "text", List.of()),
            new OnboardingQuestionVO("self_discipline", "你对自己的学习节奏判断是？", "single_choice",
                    List.of("经常拖延", "需要提醒", "比较自律", "高度自律")),
            new OnboardingQuestionVO("weakness", "目前最担心或最薄弱的部分是什么？", "text", List.of())
    );

    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, OnboardingSession> sessions = new ConcurrentHashMap<>();

    public ProfileServiceImpl(ProfileRepository profileRepository, ObjectMapper objectMapper) {
        this.profileRepository = profileRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<OnboardingQuestionVO> questions() {
        return QUESTIONS;
    }

    @Override
    public OnboardingAnswerVO answer(AuthenticatedPrincipal principal, OnboardingAnswerRequest request) {
        Long userId = requireUserId(principal);
        String sessionId = ensureSessionId(userId, request.sessionId());
        OnboardingSession session = sessions.computeIfAbsent(sessionId, id -> new OnboardingSession(userId));
        if (!session.userId().equals(userId)) {
            throw new BusinessException(403, "不能写入其他用户的画像引导会话");
        }
        session.answers().put(request.questionId(), answerText(request));
        int nextIndex = nextQuestionIndex(request.questionId());
        OnboardingQuestionVO nextQuestion = nextIndex >= QUESTIONS.size() ? null : QUESTIONS.get(nextIndex);
        return new OnboardingAnswerVO(sessionId, nextQuestion, nextQuestion == null);
    }

    @Override
    @Transactional
    public SparkProfileVO complete(AuthenticatedPrincipal principal, OnboardingCompleteRequest request) {
        Long userId = requireUserId(principal);
        Map<String, String> answers = collectAnswers(userId, request == null ? null : request.sessionId());
        if (request != null && request.answers() != null) {
            request.answers().forEach(answer -> answers.put(answer.questionId(), answerText(answer)));
        }
        SparkProfile profile = saveProfileFromAnswers(userId, answers, false);
        if (request != null && request.sessionId() != null) {
            sessions.remove(request.sessionId());
        }
        return toSparkProfileVO(profile);
    }

    @Override
    @Transactional
    public SparkProfileVO rebuild(AuthenticatedPrincipal principal, SparkProfileRebuildRequest request) {
        Long userId = requireUserId(principal);
        Map<String, String> answers = profileRepository.findByUserId(userId)
                .map(this::answersFromProfile)
                .orElseGet(LinkedHashMap::new);
        if (request.answers() != null) {
            request.answers().forEach(answer -> answers.put(answer.questionId(), answerText(answer)));
        }
        if (Boolean.FALSE.equals(request.keepHistory())) {
            profileRepository.invalidateProfileMemories(userId);
        }
        answers.put("rebuild_reason", request.reason());
        SparkProfile profile = saveProfileFromAnswers(userId, answers, true);
        return toSparkProfileVO(profile);
    }

    @Override
    public UserHomeVO userHome(String userId) {
        Long parsedUserId = parseId(userId, "用户ID格式不正确");
        UserProfileDetail detail = profileRepository.findUserDetail(parsedUserId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return toUserHomeVO(detail);
    }

    @Override
    @Transactional
    public UserHomeVO updateProfile(AuthenticatedPrincipal principal, UserProfileUpdateRequest request) {
        Long userId = requireUserId(principal);
        profileRepository.updateUserBasic(userId, request);
        SparkProfile current = profileRepository.findByUserId(userId).orElse(null);
        String learningPreference = firstText(request.learningPreference(), join(request.interests()));
        profileRepository.upsertProfile(
                userId,
                firstText(request.learningDomain(), current == null ? null : current.majorDomain()),
                firstText(request.gradeLevel(), current == null ? null : current.gradeLevel()),
                firstText(request.knowledgeBaseLevel(), current == null ? null : current.knowledgeBaseLevel()),
                firstText(request.cognitiveStyle(), current == null ? null : current.cognitiveStyle()),
                firstText(learningPreference, current == null ? null : current.learningPreference()),
                current == null ? "{}" : firstText(current.errorPreference(), "{}"),
                firstText(firstText(request.learningGoal(), request.bio()), current == null ? null : current.learningGoal()),
                firstText(request.selfDiscipline(), current == null ? null : current.selfDiscipline()),
                firstText(request.currentWeakness(), current == null ? null : current.currentWeakness())
        );
        return userHome(String.valueOf(userId));
    }

    @Override
    public LearningStatsVO learningStats(AuthenticatedPrincipal principal) {
        Long userId = requireUserId(principal);
        long totalStudyHours = profileRepository.totalStudySeconds(userId) / 3600;
        long resourceCount = profileRepository.countDistinctStudiedResources(userId);
        List<LearningPlanStatVO> plans = profileRepository.listPlanStats(userId).stream()
                .map(this::toPlanStatVO)
                .toList();
        int averageProgress = profileRepository.averageKnowledgeProgress(userId);
        return new LearningStatsVO(
                totalStudyHours,
                0,
                averageProgress,
                resourceCount,
                0,
                plans
        );
    }

    @Override
    public PageResponse<CollectionItemVO> collections(AuthenticatedPrincipal principal, String type, long page, long pageSize) {
        Long userId = requireUserId(principal);
        String safeType = type == null || type.isBlank() ? "all" : type;
        List<CollectionItem> items = new ArrayList<>();
        if ("all".equalsIgnoreCase(safeType) || "resource".equalsIgnoreCase(safeType)) {
            items.addAll(profileRepository.listResourceCollections(userId));
        }
        if ("all".equalsIgnoreCase(safeType) || "article".equalsIgnoreCase(safeType)) {
            items.addAll(profileRepository.listArticleCollections(userId));
        }
        items.sort(Comparator.comparing(CollectionItem::createdAt, Comparator.nullsLast(Comparator.reverseOrder())));
        int fromIndex = (int) Math.min(Math.max(page, 1) - 1L, Integer.MAX_VALUE) * (int) Math.max(pageSize, 1);
        int toIndex = Math.min(items.size(), fromIndex + (int) Math.max(pageSize, 1));
        List<CollectionItemVO> pageList = fromIndex >= items.size()
                ? List.of()
                : items.subList(fromIndex, toIndex).stream().map(this::toCollectionVO).toList();
        return PageResponse.of(page, pageSize, items.size(), pageList);
    }

    @Override
    public void toggleCollection(AuthenticatedPrincipal principal, CollectionToggleRequest request) {
        requireUserId(principal);
        throw new BusinessException(501, "收藏写入由资源或社区模块处理，profile 仅做个人中心聚合读取");
    }

    @Override
    public void deleteCollection(AuthenticatedPrincipal principal, String collectionId) {
        requireUserId(principal);
        throw new BusinessException(501, "收藏删除由资源或社区模块处理，profile 仅做个人中心聚合读取");
    }

    private SparkProfile saveProfileFromAnswers(Long userId, Map<String, String> answers, boolean rebuild) {
        String majorDomain = value(answers, "learning_domain", "通用学习");
        String gradeLevel = value(answers, "grade_stage", null);
        String knowledgeBaseLevel = normalizeKnowledgeLevel(value(answers, "knowledge_base", "beginner"));
        String cognitiveStyle = value(answers, "cognitive_style", null);
        String learningPreference = value(answers, "learning_preference", null);
        String learningGoal = value(answers, "learning_goal", null);
        String selfDiscipline = normalizeDiscipline(value(answers, "self_discipline", "B"));
        String weakness = value(answers, "weakness", null);
        String memoryJson = toJson(Map.of(
                "answers", answers,
                "profileBuilder", "mock",
                "rebuild", rebuild
        ));
        SparkProfile profile = profileRepository.upsertProfile(
                userId,
                majorDomain,
                gradeLevel,
                knowledgeBaseLevel,
                cognitiveStyle,
                learningPreference,
                "{}",
                learningGoal,
                selfDiscipline,
                weakness
        );
        profileRepository.insertProfileMemory(userId, profile.id(), summary(profile), memoryJson);
        return profile;
    }

    private Map<String, String> collectAnswers(Long userId, String sessionId) {
        String actualSessionId = sessionId == null || sessionId.isBlank() ? defaultSessionId(userId) : sessionId;
        OnboardingSession session = sessions.get(actualSessionId);
        if (session == null) {
            return new LinkedHashMap<>();
        }
        if (!session.userId().equals(userId)) {
            throw new BusinessException(403, "不能读取其他用户的画像引导会话");
        }
        return new LinkedHashMap<>(session.answers());
    }

    private Map<String, String> answersFromProfile(SparkProfile profile) {
        Map<String, String> answers = new LinkedHashMap<>();
        putIfPresent(answers, "learning_domain", profile.majorDomain());
        putIfPresent(answers, "grade_stage", profile.gradeLevel());
        putIfPresent(answers, "knowledge_base", profile.knowledgeBaseLevel());
        putIfPresent(answers, "cognitive_style", profile.cognitiveStyle());
        putIfPresent(answers, "learning_preference", profile.learningPreference());
        putIfPresent(answers, "learning_goal", profile.learningGoal());
        putIfPresent(answers, "self_discipline", profile.selfDiscipline());
        putIfPresent(answers, "weakness", profile.currentWeakness());
        return answers;
    }

    private SparkProfileVO toSparkProfileVO(SparkProfile profile) {
        return new SparkProfileVO(
                String.valueOf(profile.id()),
                summary(profile),
                profile.majorDomain(),
                profile.gradeLevel(),
                profile.selfDiscipline(),
                null,
                profile.knowledgeBaseLevel(),
                profile.cognitiveStyle(),
                profile.learningPreference(),
                profile.learningGoal(),
                profile.currentWeakness()
        );
    }

    private UserHomeVO toUserHomeVO(UserProfileDetail detail) {
        int progress = profileRepository.averageKnowledgeProgress(detail.userId());
        List<Map<String, Object>> skills = detail.majorDomain() == null
                ? List.of()
                : List.of(Map.of("name", detail.majorDomain(), "level", skillLevel(detail.knowledgeBaseLevel())));
        return new UserHomeVO(
                String.valueOf(detail.userId()),
                detail.username(),
                detail.nickname(),
                detail.avatarUrl(),
                firstText(detail.learningGoal(), detail.currentWeakness()),
                detail.majorDomain(),
                detail.gradeLevel(),
                detail.knowledgeBaseLevel(),
                detail.selfDiscipline(),
                detail.profileId() != null,
                Map.of("followers", 0, "likes", 0, "articles", 0),
                skills,
                progress
        );
    }

    private LearningPlanStatVO toPlanStatVO(LearningPlanStat stat) {
        return new LearningPlanStatVO(
                String.valueOf(stat.id()),
                stat.title(),
                stat.currentStage(),
                stat.finishedCount(),
                stat.totalCount(),
                stat.progress()
        );
    }

    private CollectionItemVO toCollectionVO(CollectionItem item) {
        return new CollectionItemVO(
                item.targetType() + "_" + item.id(),
                item.targetType(),
                String.valueOf(item.targetId()),
                item.title(),
                item.summary(),
                item.createdAt()
        );
    }

    private int nextQuestionIndex(String questionId) {
        for (int index = 0; index < QUESTIONS.size(); index++) {
            if (QUESTIONS.get(index).id().equals(questionId)) {
                return index + 1;
            }
        }
        throw new BusinessException(400, "画像问题不存在");
    }

    private String answerText(OnboardingAnswerRequest request) {
        if (request.selectedOptions() != null && !request.selectedOptions().isEmpty()) {
            return String.join(",", request.selectedOptions());
        }
        if (request.answerText() != null && !request.answerText().isBlank()) {
            return request.answerText();
        }
        if (request.answer() != null) {
            return String.valueOf(request.answer());
        }
        return "";
    }

    private String summary(SparkProfile profile) {
        String stage = firstText(profile.gradeLevel(), "学习者");
        String domain = firstText(profile.majorDomain(), "通用学习");
        String level = firstText(profile.knowledgeBaseLevel(), "beginner");
        String discipline = firstText(profile.selfDiscipline(), "B");
        return stage + "，目标方向为" + domain + "，基础水平为" + level + "，自律等级为" + discipline + "。";
    }

    private String ensureSessionId(Long userId, String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            return sessionId;
        }
        return defaultSessionId(userId);
    }

    private String defaultSessionId(Long userId) {
        return "ob_" + userId + "_" + UUID.nameUUIDFromBytes(String.valueOf(userId).getBytes());
    }

    private Long requireUserId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.type() != IdentityType.USER) {
            throw new BusinessException(401, "请先登录前台账号");
        }
        return principal.id();
    }

    private Long parseId(String id, String message) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException exception) {
            throw new BusinessException(400, message);
        }
    }

    private String normalizeKnowledgeLevel(String value) {
        if (value == null || value.isBlank()) {
            return "beginner";
        }
        if (value.contains("零基础")) {
            return "beginner";
        }
        if (value.contains("概念")) {
            return "basic";
        }
        if (value.contains("Demo")) {
            return "intermediate";
        }
        if (value.contains("实战")) {
            return "advanced";
        }
        return value;
    }

    private String normalizeDiscipline(String value) {
        if (value == null || value.isBlank()) {
            return "B";
        }
        if (value.contains("高度")) {
            return "S";
        }
        if (value.contains("比较")) {
            return "A";
        }
        if (value.contains("提醒")) {
            return "B";
        }
        if (value.contains("拖延")) {
            return "C";
        }
        return value;
    }

    private int skillLevel(String knowledgeBaseLevel) {
        if ("advanced".equalsIgnoreCase(knowledgeBaseLevel)) {
            return 5;
        }
        if ("intermediate".equalsIgnoreCase(knowledgeBaseLevel)) {
            return 4;
        }
        if ("basic".equalsIgnoreCase(knowledgeBaseLevel)) {
            return 2;
        }
        return 1;
    }

    private String value(Map<String, String> answers, String key, String defaultValue) {
        String value = answers.get(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String firstText(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private String join(List<String> values) {
        return values == null || values.isEmpty() ? null : String.join(",", values);
    }

    private void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private record OnboardingSession(Long userId, Map<String, String> answers) {
        private OnboardingSession(Long userId) {
            this(userId, new ConcurrentHashMap<>());
        }
    }
}
