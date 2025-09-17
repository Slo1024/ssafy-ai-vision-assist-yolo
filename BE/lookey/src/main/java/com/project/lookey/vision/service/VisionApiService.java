package com.project.lookey.vision.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VisionApiService { // Updated response format

    private ImageAnnotatorClient visionClient;

    @Value("${google.cloud.project.id}")
    private String projectId;

    // 카테고리 분류용 키워드 맵
    private static final Set<String> BEVERAGE_KEYWORDS = Set.of(
            "beverage", "drink", "juice", "water", "soda", "coffee", "tea", "milk",
            "beer", "wine", "alcohol", "cocktail", "smoothie", "shake", "cola"
    );

    private static final Set<String> SNACK_KEYWORDS = Set.of(
            "snack", "food", "candy", "chocolate", "chip", "cookie", "cracker",
            "biscuit", "gum", "cereal", "bar", "nut", "pretzel", "popcorn"
    );

    @PostConstruct
    public void initializeVisionClient() {
        try {
            // GOOGLE_APPLICATION_CREDENTIALS 환경변수 확인
            String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (credentialsPath == null) {
                credentialsPath = System.getProperty("GOOGLE_APPLICATION_CREDENTIALS");
            }

            // 환경변수에서 못 찾으면 .env에서 설정한 값 사용
            if (credentialsPath == null) {
                credentialsPath = "c:/Users/SSAFY/Desktop/infra/ssafy-finance-6921c01f57ed.json";
                System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", credentialsPath);
                log.info("Set GOOGLE_APPLICATION_CREDENTIALS from fallback: {}", credentialsPath);
            }

            log.info("Google Cloud credentials path: {}", credentialsPath);

            // 파일 존재 확인
            java.io.File credFile = new java.io.File(credentialsPath);
            if (!credFile.exists()) {
                throw new RuntimeException("Credentials file not found: " + credentialsPath);
            }

            // 직접 파일에서 인증 정보 로드
            com.google.auth.oauth2.GoogleCredentials credentials =
                com.google.auth.oauth2.GoogleCredentials.fromStream(
                    new java.io.FileInputStream(credentialsPath)
                );

            com.google.cloud.vision.v1.ImageAnnotatorSettings settings =
                com.google.cloud.vision.v1.ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();

            this.visionClient = ImageAnnotatorClient.create(settings);
            log.info("Google Cloud Vision API client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Vision API client", e);
            log.warn("Vision API will be unavailable. Application will continue without Vision API functionality.");
            // 애플리케이션 시작을 위해 예외를 던지지 않음
            this.visionClient = null;
        }
    }

    public Mono<Map<String, Object>> analyzeImage(byte[] imageBytes) {
        return Mono.fromCallable(() -> {
            if (visionClient == null) {
                throw new RuntimeException("Vision API 클라이언트가 초기화되지 않았습니다. 인증 설정을 확인해주세요.");
            }

            log.info("Starting Vision API image analysis");
            long startTime = System.currentTimeMillis();

            // Vision API 호출
            DetectionResult result = detectObjectsAndLabels(imageBytes);

            // 결과 분석 및 변환
            Map<String, Object> analysisResult = processDetectionResult(result);

            long endTime = System.currentTimeMillis();
            log.info("Vision API analysis completed in {}ms", endTime - startTime);

            return analysisResult;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private DetectionResult detectObjectsAndLabels(byte[] imageBytes) {
        try {
            ByteString imgBytes = ByteString.copyFrom(imageBytes);
            Image img = Image.newBuilder().setContent(imgBytes).build();

            // Object Detection Feature
            Feature objectFeature = Feature.newBuilder()
                    .setType(Feature.Type.OBJECT_LOCALIZATION)
                    .setMaxResults(20)
                    .build();

            // Label Detection Feature
            Feature labelFeature = Feature.newBuilder()
                    .setType(Feature.Type.LABEL_DETECTION)
                    .setMaxResults(20)
                    .build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(objectFeature)
                    .addFeatures(labelFeature)
                    .setImage(img)
                    .build();

            BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(
                    Collections.singletonList(request));

            AnnotateImageResponse imageResponse = response.getResponsesList().get(0);

            if (imageResponse.hasError()) {
                throw new RuntimeException("Vision API Error: " + imageResponse.getError().getMessage());
            }

            return new DetectionResult(
                    imageResponse.getLocalizedObjectAnnotationsList(),
                    imageResponse.getLabelAnnotationsList()
            );

        } catch (Exception e) {
            log.error("Vision API detection failed", e);
            throw new RuntimeException("Vision API 호출 실패", e);
        }
    }

    private Map<String, Object> processDetectionResult(DetectionResult result) {
        Map<String, Object> analysisResult = new HashMap<>();

        // People Detection - boolean
        analysisResult.put("people", analyzePeople(result.getObjects()));

        // Obstacles Detection - boolean
        analysisResult.put("obstacles", analyzeObstacles(result.getObjects()));

        // Counter Detection - boolean
        analysisResult.put("counter", analyzeCounter(result.getLabels(), result.getObjects()));

        // Directions Analysis - object with front/left/right booleans
        analysisResult.put("directions", analyzeDirections(result.getObjects()));

        // Category Analysis - string
        analysisResult.put("category", analyzeCategory(result.getLabels()));

        return analysisResult;
    }

    private boolean analyzePeople(List<LocalizedObjectAnnotation> objects) {
        return objects.stream()
                .anyMatch(obj -> obj.getName().toLowerCase().contains("person") && obj.getScore() > 0.5f);
    }

    private boolean analyzeObstacles(List<LocalizedObjectAnnotation> objects) {
        Set<String> obstacleTypes = Set.of("chair", "table", "cart", "box", "bag", "bicycle", "vehicle");

        return objects.stream()
                .anyMatch(obj -> {
                    String objectName = obj.getName().toLowerCase();
                    return obstacleTypes.stream().anyMatch(objectName::contains) && obj.getScore() > 0.5f;
                });
    }

    private boolean analyzeCounter(List<EntityAnnotation> labels, List<LocalizedObjectAnnotation> objects) {
        // 라벨에서 카운터/계산대 관련 키워드 찾기
        Set<String> counterKeywords = Set.of("counter", "checkout", "register", "cashier", "desk", "reception");

        return labels.stream()
                .anyMatch(label -> {
                    String description = label.getDescription().toLowerCase();
                    return counterKeywords.stream().anyMatch(description::contains) && label.getScore() > 0.5f;
                }) || objects.stream()
                .anyMatch(obj -> {
                    String objectName = obj.getName().toLowerCase();
                    return counterKeywords.stream().anyMatch(objectName::contains) && obj.getScore() > 0.5f;
                });
    }

    private Map<String, Boolean> analyzeDirections(List<LocalizedObjectAnnotation> objects) {
        // 이미지를 3개 구역으로 나누어 방향 분석
        Map<String, Boolean> directions = new HashMap<>();

        // 기본값 설정 (객체가 없는 곳은 이동 가능)
        directions.put("front", true);
        directions.put("left", true);
        directions.put("right", true);

        for (LocalizedObjectAnnotation obj : objects) {
            if (obj.getScore() > 0.6f) {
                BoundingPoly boundingPoly = obj.getBoundingPoly();
                float centerX = getCenterX(boundingPoly);

                // 이미지를 3등분하여 방향 결정
                if (centerX < 0.33f) {
                    directions.put("left", false);
                } else if (centerX > 0.67f) {
                    directions.put("right", false);
                } else {
                    directions.put("front", false);
                }
            }
        }

        return directions;
    }


    private String analyzeCategory(List<EntityAnnotation> labels) {
        Map<String, Double> categoryScores = new HashMap<>();
        categoryScores.put("beverage", 0.0);
        categoryScores.put("snack", 0.0);

        for (EntityAnnotation label : labels) {
            String description = label.getDescription().toLowerCase();
            float score = label.getScore();

            if (BEVERAGE_KEYWORDS.stream().anyMatch(description::contains)) {
                categoryScores.merge("beverage", (double) score, Double::sum);
            }
            if (SNACK_KEYWORDS.stream().anyMatch(description::contains)) {
                categoryScores.merge("snack", (double) score, Double::sum);
            }
        }

        return categoryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(entry -> entry.getValue() > 0.3) // 최소 신뢰도
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }

    private float getCenterX(BoundingPoly boundingPoly) {
        List<Vertex> vertices = boundingPoly.getVerticesList();
        if (vertices.size() < 2) return 0.5f;

        float minX = vertices.stream().map(Vertex::getX).min(Integer::compareTo).orElse(0);
        float maxX = vertices.stream().map(Vertex::getX).max(Integer::compareTo).orElse(0);

        return (minX + maxX) / 2.0f / 1000.0f; // 정규화 (1000px 기준)
    }

    // 내부 클래스
    private static class DetectionResult {
        private final List<LocalizedObjectAnnotation> objects;
        private final List<EntityAnnotation> labels;

        public DetectionResult(List<LocalizedObjectAnnotation> objects, List<EntityAnnotation> labels) {
            this.objects = objects;
            this.labels = labels;
        }

        public List<LocalizedObjectAnnotation> getObjects() {
            return objects;
        }

        public List<EntityAnnotation> getLabels() {
            return labels;
        }
    }
}