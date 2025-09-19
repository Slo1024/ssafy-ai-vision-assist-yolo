package com.project.lookey.vision.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VisionApiService { // Updated response format

    private ImageAnnotatorClient visionClient;

    @Value("${google.cloud.project.id}")
    private String projectId;

    @Value("${google.cloud.vision.credentials}")
    private String credentialsPath;

    // 실제 Vision API 결과 기반 향상된 키워드 맵
    private static final Set<String> ENHANCED_BEVERAGE_KEYWORDS = Set.of(
            // 실제 감지되는 음료 관련 라벨
            "drink can", "soft drink", "energy drink", "diet drink",
            "carbonated soft drinks", "bottle", "plastic bottle",
            "aluminum can", "steel and tin cans", "beverage can",
            "juice", "water", "soda", "coffee", "tea", "milk"
    );

    private static final Set<String> ENHANCED_SNACK_KEYWORDS = Set.of(
            // 실제 감지되는 식품 관련 라벨
            "food", "convenience food", "frozen food", "food storage",
            "packaged goods", "processed food",
            "candy", "chocolate", "chip", "cookie", "cracker",
            "biscuit", "gum", "cereal", "bar", "nut"
    );


    @PostConstruct
    public void initializeVisionClient() {
        try {
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

            // 3분할 병렬 분석 (모든 기능 포함)
            ParallelAnalysisResult parallelResult = analyzeImageWithParallelRegions(imageBytes);

            // 결과 분석 및 변환
            Map<String, Object> analysisResult = new HashMap<>();

            // 3분할 결과 사용
            Map<String, Boolean> finalPeopleResult = parallelResult.getPeopleByRegion();
            log.info("=== 최종 JSON 응답 생성 ===");
            log.info("peopleByRegion 맵 내용: {}", finalPeopleResult);

            analysisResult.put("people", finalPeopleResult);
            analysisResult.put("directions", parallelResult.getDirections());
            analysisResult.put("category", parallelResult.getCenterCategory());
            analysisResult.put("obstacles", parallelResult.getObstaclesByRegion());
            analysisResult.put("counter", parallelResult.getCounterDetection());

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

            // 디버그 로그 추가 - 감지된 객체들 출력
            log.info("=== Vision API 감지 결과 ===");
            log.info("감지된 객체 수: {}", imageResponse.getLocalizedObjectAnnotationsList().size());
            for (LocalizedObjectAnnotation obj : imageResponse.getLocalizedObjectAnnotationsList()) {
                log.info("객체: {} (신뢰도: {:.3f})", obj.getName(), obj.getScore());
            }

            log.info("감지된 라벨 수: {}", imageResponse.getLabelAnnotationsList().size());
            for (EntityAnnotation label : imageResponse.getLabelAnnotationsList()) {
                log.info("라벨: {} (신뢰도: {:.3f})", label.getDescription(), label.getScore());
            }
            log.info("========================");

            return new DetectionResult(
                    imageResponse.getLocalizedObjectAnnotationsList(),
                    imageResponse.getLabelAnnotationsList()
            );

        } catch (Exception e) {
            log.error("Vision API detection failed", e);
            throw new RuntimeException("Vision API 호출 실패", e);
        }
    }




    /**
     * 3분할 병렬 분석 (방향, 사람, 카테고리)
     */
    private ParallelAnalysisResult analyzeImageWithParallelRegions(byte[] imageBytes) {
        log.info("=== 3분할 병렬 분석 시작 ===");

        try {
            // 이미지를 BufferedImage로 변환
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            // 3분할 영역 생성
            int regionWidth = width / 3;
            BufferedImage leftRegion = originalImage.getSubimage(0, 0, regionWidth, height);
            BufferedImage centerRegion = originalImage.getSubimage(regionWidth, 0, regionWidth, height);
            BufferedImage rightRegion = originalImage.getSubimage(regionWidth * 2, 0, width - regionWidth * 2, height);

            log.info("이미지 3분할 완료 - 원본: {}x{}, 영역: {}x{}", width, height, regionWidth, height);

            // 3개 영역 병렬 분석 (Object + Label Detection)
            CompletableFuture<RegionResult> leftAnalysis = analyzeRegionWithObjectsAsync(leftRegion, "LEFT");
            CompletableFuture<RegionResult> centerAnalysis = analyzeRegionWithObjectsAsync(centerRegion, "CENTER");
            CompletableFuture<RegionResult> rightAnalysis = analyzeRegionWithObjectsAsync(rightRegion, "RIGHT");

            // 모든 분석 완료 대기
            CompletableFuture.allOf(leftAnalysis, centerAnalysis, rightAnalysis).join();

            RegionResult leftResult = leftAnalysis.join();
            RegionResult centerResult = centerAnalysis.join();
            RegionResult rightResult = rightAnalysis.join();

            // 방향 분석
            Map<String, Boolean> directions = new HashMap<>();
            directions.put("left", canMoveInRegion(leftResult.getLabels(), "LEFT"));
            directions.put("front", canMoveInRegion(centerResult.getLabels(), "CENTER"));
            directions.put("right", canMoveInRegion(rightResult.getLabels(), "RIGHT"));

            // 사람 위치 분석
            Map<String, Boolean> peopleByRegion = new HashMap<>();
            boolean leftPeople = detectPeopleInRegion(leftResult, "LEFT");
            boolean centerPeople = detectPeopleInRegion(centerResult, "CENTER");
            boolean rightPeople = detectPeopleInRegion(rightResult, "RIGHT");

            peopleByRegion.put("left", leftPeople);
            peopleByRegion.put("front", centerPeople);
            peopleByRegion.put("right", rightPeople);

            log.info("=== peopleByRegion 맵 생성 결과 ===");
            log.info("LEFT: {} → left: {}", leftPeople, peopleByRegion.get("left"));
            log.info("CENTER: {} → front: {}", centerPeople, peopleByRegion.get("front"));
            log.info("RIGHT: {} → right: {}", rightPeople, peopleByRegion.get("right"));

            // 장애물 위치 분석
            Map<String, Boolean> obstaclesByRegion = new HashMap<>();
            obstaclesByRegion.put("left", detectObstaclesInRegion(leftResult, "LEFT"));
            obstaclesByRegion.put("front", detectObstaclesInRegion(centerResult, "CENTER"));
            obstaclesByRegion.put("right", detectObstaclesInRegion(rightResult, "RIGHT"));

            // 카테고리 분석 (CENTER만)
            String centerCategory = analyzeCategory(centerResult.getLabels());

            // 카운터 감지 (전체 영역 통합 분석)
            boolean counterDetection = detectCounterInRegions(leftResult, centerResult, rightResult);

            log.info("3분할 분석 결과:");
            log.info("  방향: left={}, front={}, right={}",
                    directions.get("left"), directions.get("front"), directions.get("right"));
            log.info("  사람: left={}, front={}, right={}",
                    peopleByRegion.get("left"), peopleByRegion.get("front"), peopleByRegion.get("right"));
            log.info("  장애물: left={}, front={}, right={}",
                    obstaclesByRegion.get("left"), obstaclesByRegion.get("front"), obstaclesByRegion.get("right"));
            log.info("  카테고리(중앙): {}", centerCategory);
            log.info("  카운터: {}", counterDetection);

            return new ParallelAnalysisResult(directions, peopleByRegion, obstaclesByRegion, centerCategory, counterDetection);

        } catch (Exception e) {
            log.error("3분할 분석 실패, 기본값 반환", e);
            // 안전한 기본값 반환
            Map<String, Boolean> safeDirections = new HashMap<>();
            safeDirections.put("front", false);
            safeDirections.put("left", false);
            safeDirections.put("right", false);

            Map<String, Boolean> safePeople = new HashMap<>();
            safePeople.put("front", false);
            safePeople.put("left", false);
            safePeople.put("right", false);

            Map<String, Boolean> safeObstacles = new HashMap<>();
            safeObstacles.put("front", false);
            safeObstacles.put("left", false);
            safeObstacles.put("right", false);

            return new ParallelAnalysisResult(safeDirections, safePeople, safeObstacles, "unknown", false);
        }
    }

    /**
     * 영역 비동기 분석 (Object + Label Detection)
     */
    private CompletableFuture<RegionResult> analyzeRegionWithObjectsAsync(BufferedImage regionImage, String regionName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("{} 영역 분석 시작", regionName);

                // BufferedImage를 byte[]로 변환
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(regionImage, "jpeg", baos);
                byte[] regionBytes = baos.toByteArray();

                // Vision API 호출 (Object + Label Detection)
                ByteString imgBytes = ByteString.copyFrom(regionBytes);
                Image img = Image.newBuilder().setContent(imgBytes).build();

                // Object Detection Feature
                Feature objectFeature = Feature.newBuilder()
                        .setType(Feature.Type.OBJECT_LOCALIZATION)
                        .setMaxResults(10)
                        .build();

                // Label Detection Feature
                Feature labelFeature = Feature.newBuilder()
                        .setType(Feature.Type.LABEL_DETECTION)
                        .setMaxResults(10)
                        .build();

                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                        .addFeatures(objectFeature)
                        .addFeatures(labelFeature)
                        .setImage(img)
                        .build();

                BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(
                        List.of(request));

                AnnotateImageResponse imageResponse = response.getResponsesList().get(0);
                List<LocalizedObjectAnnotation> objects = imageResponse.getLocalizedObjectAnnotationsList();
                List<EntityAnnotation> labels = imageResponse.getLabelAnnotationsList();

                log.info("{} 영역 분석 완료 - 객체 {}개, 라벨 {}개", regionName, objects.size(), labels.size());
                for (LocalizedObjectAnnotation obj : objects) {
                    log.info("  {} 영역 객체: {} (신뢰도: {:.3f})", regionName, obj.getName(), obj.getScore());
                }
                for (EntityAnnotation label : labels) {
                    log.info("  {} 영역 라벨: {} (신뢰도: {:.3f})", regionName, label.getDescription(), label.getScore());
                }

                return new RegionResult(objects, labels);

            } catch (Exception e) {
                log.error("{} 영역 분석 실패", regionName, e);
                return new RegionResult(Collections.emptyList(), Collections.emptyList());
            }
        });
    }

    /**
     * 영역별 이동 가능 여부 판단
     */
    private boolean canMoveInRegion(List<EntityAnnotation> labels, String regionName) {
        boolean hasAisleLabels = labels.stream()
                .anyMatch(label -> label.getDescription().toLowerCase().contains("aisle"));

        log.info("{} 영역: {} - {}", regionName,
                hasAisleLabels ? "통로 감지" : "통로 없음",
                hasAisleLabels ? "이동 가능" : "이동 불가");

        return hasAisleLabels;
    }

    /**
     * 영역별 사람 감지
     */
    private boolean detectPeopleInRegion(RegionResult regionResult, String regionName) {
        System.out.println("=== detectPeopleInRegion 메서드 호출됨: " + regionName + " ===");
        log.info("=== detectPeopleInRegion 메서드 호출됨: {} ===", regionName);

        // Object Detection 우선
        boolean objectDetection = regionResult.getObjects().stream()
                .anyMatch(obj -> obj.getName().toLowerCase().contains("person") && obj.getScore() > 0.5f);

        // Object Detection 로깅 추가
        if (!regionResult.getObjects().isEmpty()) {
            log.info("{} 영역 감지된 객체들:", regionName);
            regionResult.getObjects().forEach(obj ->
                log.info("  {} 영역 객체: {} (신뢰도: {:.3f})", regionName, obj.getName(), obj.getScore()));
        }

        // Label Detection 보완 - 정확한 단어 매칭으로 수정
        Set<String> peopleLabels = Set.of("person", "people", "human", "man", "woman");
        boolean labelDetection = regionResult.getLabels().stream().anyMatch(label -> {
            String desc = label.getDescription().toLowerCase();

            // contains() 대신 정확한 단어 매칭 사용 (personal care 같은 false positive 방지)
            boolean hasPeopleLabel = peopleLabels.stream().anyMatch(keyword -> {
                // 단어 경계를 고려한 정확한 매칭
                return desc.equals(keyword) ||
                       desc.startsWith(keyword + " ") ||
                       desc.endsWith(" " + keyword) ||
                       desc.contains(" " + keyword + " ");
            });

            boolean highConfidence = label.getScore() > 0.6f;

            // 상세 디버깅 로그 추가
            if (hasPeopleLabel) {
                log.info("{} 영역 사람 라벨 매칭: '{}' (신뢰도: {:.3f}) - 매칭됨!",
                        regionName, desc, label.getScore());
            }

            return hasPeopleLabel && highConfidence;
        });

        boolean hasPeople = objectDetection || labelDetection;

        // 상세 디버깅 로그 추가
        log.info("{} 영역 상세 분석 결과:", regionName);
        log.info("  objectDetection: {}", objectDetection);
        log.info("  labelDetection: {}", labelDetection);
        log.info("  최종 hasPeople: {}", hasPeople);
        log.info("{} 영역 사람 감지: {}", regionName, hasPeople ? "있음" : "없음");

        return hasPeople;
    }

    /**
     * 영역별 장애물 감지
     */
    private boolean detectObstaclesInRegion(RegionResult regionResult, String regionName) {
        Set<String> obstacleTypes = Set.of("chair", "table", "cart", "box", "bag", "bicycle", "vehicle");

        boolean hasObstacles = regionResult.getObjects().stream()
                .anyMatch(obj -> {
                    String objectName = obj.getName().toLowerCase();
                    return obstacleTypes.stream().anyMatch(objectName::contains) && obj.getScore() > 0.5f;
                });

        log.info("{} 영역 장애물 감지: {}", regionName, hasObstacles ? "있음" : "없음");

        return hasObstacles;
    }

    /**
     * 영역 독립적 카운터 감지 (다중 증거 시스템)
     */
    private boolean detectCounterInRegions(RegionResult leftResult, RegionResult centerResult, RegionResult rightResult) {
        // 모든 영역의 라벨과 객체를 통합하여 분석
        List<EntityAnnotation> allLabels = new ArrayList<>();
        allLabels.addAll(leftResult.getLabels());
        allLabels.addAll(centerResult.getLabels());
        allLabels.addAll(rightResult.getLabels());

        List<LocalizedObjectAnnotation> allObjects = new ArrayList<>();
        allObjects.addAll(leftResult.getObjects());
        allObjects.addAll(centerResult.getObjects());
        allObjects.addAll(rightResult.getObjects());

        // 탐지된 모든 라벨 로그 출력
        log.info("=== 탐지된 라벨들 (총 {}개) ===", allLabels.size());
        for (EntityAnnotation label : allLabels) {
            log.info("라벨: '{}' (신뢰도: {:.3f})", label.getDescription(), label.getScore());
        }

        // 탐지된 모든 객체 로그 출력
        log.info("=== 탐지된 객체들 (총 {}개) ===", allObjects.size());
        for (LocalizedObjectAnnotation obj : allObjects) {
            log.info("객체: '{}' (신뢰도: {:.3f})", obj.getName(), obj.getScore());
        }

        // 라벨 세트 생성 (소문자, 빠른 검색용)
        Set<String> labelSet = allLabels.stream()
                .filter(label -> label.getScore() > 0.6f)  // 신뢰도 0.6 이상만
                .map(label -> label.getDescription().toLowerCase())
                .collect(Collectors.toSet());

        log.info("=== 분석 대상 라벨들 (신뢰도 0.6+) ===");
        labelSet.forEach(label -> log.info("  분석 라벨: '{}'", label));

        // 1단계: 직접 카운터 키워드 (100% 확실)
        Set<String> directCounterKeywords = Set.of(
            "cash register", "checkout", "cashier", "pos", "terminal",
            "checkout counter", "service desk", "reception desk"
        );

        boolean hasDirectCounterKeyword = directCounterKeywords.stream()
                .anyMatch(labelSet::contains);

        if (hasDirectCounterKeyword) {
            log.info("=== 카운터 탐지 성공 (1단계: 직접 키워드) ===");
            return true;
        }

        // 2단계: Electronic Device + Machine 조합 (핵심 증거)
        boolean hasElectronicDevice = labelSet.contains("electronic device");
        boolean hasMachine = labelSet.contains("machine");

        log.info("=== 2단계 핵심 증거 분석 ===");
        log.info("  Electronic device: {}", hasElectronicDevice);
        log.info("  Machine: {}", hasMachine);

        if (hasElectronicDevice && hasMachine) {
            // 3단계: 자판기 환경 제외 로직
            boolean isVendingMachine = isVendingMachineEnvironment(labelSet);
            log.info("  자판기 환경 여부: {}", isVendingMachine);

            if (!isVendingMachine) {
                // 4단계: 환경 컨텍스트 확인
                boolean isConvenienceStore = isConvenienceStoreEnvironment(labelSet);
                boolean isArchitecturalSpace = isArchitecturalEnvironment(labelSet);

                log.info("  편의점 환경: {}", isConvenienceStore);
                log.info("  건물 내부 환경: {}", isArchitecturalSpace);

                if (isConvenienceStore || isArchitecturalSpace) {
                    log.info("=== 카운터 탐지 성공 (2단계: Electronic+Machine+환경) ===");
                    return true;
                }
            }
        }

        log.info("=== 카운터 탐지 실패 ===");
        log.info("  Electronic device + Machine 조합: {}", hasElectronicDevice && hasMachine);
        log.info("  직접 키워드: {}", hasDirectCounterKeyword);
        return false;
    }

    /**
     * 자판기 환경 감지
     */
    private boolean isVendingMachineEnvironment(Set<String> labelSet) {
        int beverageCount = 0;
        String[] beverageLabels = {"beverage", "drink", "soft drink", "bottle"};

        for (String label : beverageLabels) {
            if (labelSet.contains(label)) {
                beverageCount++;
            }
        }

        // Machine + 음료 관련 라벨 3개 이상 = 자판기
        boolean isVending = labelSet.contains("machine") && beverageCount >= 3;
        log.info("    자판기 판단: Machine({}) + 음료라벨{}개 = {}",
                labelSet.contains("machine"), beverageCount, isVending);
        return isVending;
    }

    /**
     * 편의점 환경 감지
     */
    private boolean isConvenienceStoreEnvironment(Set<String> labelSet) {
        return labelSet.contains("convenience store") ||
               labelSet.contains("retail") ||
               labelSet.contains("supermarket");
    }

    /**
     * 건물 내부 환경 감지 (nopeople_counter.jpeg 케이스)
     */
    private boolean isArchitecturalEnvironment(Set<String> labelSet) {
        return labelSet.contains("building") ||
               labelSet.contains("interior design") ||
               labelSet.contains("architecture");
    }



    private String analyzeCategory(List<EntityAnnotation> labels) {
        // 계산대가 감지되어도 snack/beverage 분류는 유지 (계산대 근처에 상품이 있을 수 있음)

        Map<String, Double> categoryScores = new HashMap<>();
        categoryScores.put("beverage", 0.0);
        categoryScores.put("snack", 0.0);

        for (EntityAnnotation label : labels) {
            String description = label.getDescription().toLowerCase();
            float score = label.getScore();

            if (ENHANCED_BEVERAGE_KEYWORDS.stream().anyMatch(description::contains)) {
                categoryScores.merge("beverage", (double) score, Double::sum);
                log.debug("음료 키워드 매칭: '{}' (점수: {:.2f})", description, score);
            }
            if (ENHANCED_SNACK_KEYWORDS.stream().anyMatch(description::contains)) {
                categoryScores.merge("snack", (double) score, Double::sum);
                log.debug("스낵 키워드 매칭: '{}' (점수: {:.2f})", description, score);
            }
        }

        return categoryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(entry -> entry.getValue() > 0.5) // 신뢰도 높임 (0.3 → 0.5)
                .map(Map.Entry::getKey)
                .orElse("unknown");
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

    /**
     * 3분할 병렬 분석 결과
     */
    private static class ParallelAnalysisResult {
        private final Map<String, Boolean> directions;
        private final Map<String, Boolean> peopleByRegion;
        private final Map<String, Boolean> obstaclesByRegion;
        private final String centerCategory;
        private final boolean counterDetection;

        public ParallelAnalysisResult(Map<String, Boolean> directions,
                                    Map<String, Boolean> peopleByRegion,
                                    Map<String, Boolean> obstaclesByRegion,
                                    String centerCategory,
                                    boolean counterDetection) {
            this.directions = directions;
            this.peopleByRegion = peopleByRegion;
            this.obstaclesByRegion = obstaclesByRegion;
            this.centerCategory = centerCategory;
            this.counterDetection = counterDetection;
        }

        public Map<String, Boolean> getDirections() { return directions; }
        public Map<String, Boolean> getPeopleByRegion() { return peopleByRegion; }
        public Map<String, Boolean> getObstaclesByRegion() { return obstaclesByRegion; }
        public String getCenterCategory() { return centerCategory; }
        public boolean getCounterDetection() { return counterDetection; }
    }

    /**
     * 영역별 분석 결과
     */
    private static class RegionResult {
        private final List<LocalizedObjectAnnotation> objects;
        private final List<EntityAnnotation> labels;

        public RegionResult(List<LocalizedObjectAnnotation> objects, List<EntityAnnotation> labels) {
            this.objects = objects;
            this.labels = labels;
        }

        public List<LocalizedObjectAnnotation> getObjects() { return objects; }
        public List<EntityAnnotation> getLabels() { return labels; }
    }
}