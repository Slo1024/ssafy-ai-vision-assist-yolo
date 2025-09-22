package com.project.lookey.common.util;

/**
 * 간단한 문자열 유사도 계산 유틸리티
 * 시각장애인 사용자의 타이핑 오류를 보정하기 위한 기본적인 유사도 검색 기능 제공
 */
public class SimilarityUtil {

    private static final double SIMILARITY_THRESHOLD = 0.5;

    /**
     * 두 문자열의 유사도를 계산합니다
     * @param query 검색어
     * @param target 대상 문자열
     * @return 0.0 ~ 1.0 사이의 유사도 (1.0이 완전 일치)
     */
    public static double calculateSimilarity(String query, String target) {
        if (query == null || target == null) return 0.0;

        query = query.toLowerCase().trim();
        target = target.toLowerCase().trim();

        if (query.equals(target)) return 1.0;
        if (target.contains(query)) return 0.8;
        if (query.contains(target)) return 0.7;

        // 한글이 포함된 경우 한글 자음/모음 분해 유사도 사용
        if (containsKorean(query) || containsKorean(target)) {
            return calculateKoreanSimilarity(query, target);
        }

        // 일반 레벤슈타인 거리 기반 유사도
        int maxLen = Math.max(query.length(), target.length());
        if (maxLen == 0) return 1.0;

        int distance = levenshteinDistance(query, target);
        return 1.0 - (double) distance / maxLen;
    }

    /**
     * 유사도가 임계값 이상인지 확인합니다
     */
    public static boolean isSimilar(String query, String target) {
        return calculateSimilarity(query, target) >= SIMILARITY_THRESHOLD;
    }

    /**
     * 레벤슈타인 거리를 계산합니다
     */
    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
                }
            }
        }

        return dp[a.length()][b.length()];
    }

    /**
     * 한글이 포함되어 있는지 확인합니다
     */
    private static boolean containsKorean(String text) {
        return text.chars().anyMatch(c -> c >= 0xAC00 && c <= 0xD7AF);
    }

    /**
     * 한글 문자열의 유사도를 자음/모음 분해를 통해 계산합니다
     */
    private static double calculateKoreanSimilarity(String query, String target) {
        String queryDecomposed = decomposeKorean(query);
        String targetDecomposed = decomposeKorean(target);

        // 분해된 문자열이 같으면 높은 유사도
        if (queryDecomposed.equals(targetDecomposed)) return 0.95;

        // 분해된 문자열로 레벤슈타인 거리 계산
        int maxLen = Math.max(queryDecomposed.length(), targetDecomposed.length());
        if (maxLen == 0) return 1.0;

        int distance = levenshteinDistance(queryDecomposed, targetDecomposed);
        double similarity = 1.0 - (double) distance / maxLen;

        // 한글 분해 유사도에 가중치 적용 (자음이 더 중요)
        return Math.max(similarity * 0.8, calculateComponentSimilarity(query, target));
    }

    /**
     * 한글을 자음/모음으로 분해합니다
     */
    private static String decomposeKorean(String text) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (c >= 0xAC00 && c <= 0xD7AF) {
                // 한글 완성형 분해
                int code = c - 0xAC00;
                int cho = code / (21 * 28);      // 초성
                int jung = (code % (21 * 28)) / 28; // 중성
                int jong = code % 28;              // 종성

                result.append((char)(0x1100 + cho));  // 초성 추가
                result.append((char)(0x1161 + jung)); // 중성 추가
                if (jong > 0) {
                    result.append((char)(0x11A7 + jong)); // 종성 추가 (있는 경우만)
                }
            } else {
                result.append(c); // 한글이 아닌 문자는 그대로
            }
        }

        return result.toString();
    }

    /**
     * 한글 구성 요소별 유사도를 계산합니다 (자음 가중치 높음)
     */
    private static double calculateComponentSimilarity(String query, String target) {
        if (query.length() != target.length()) return 0.0;

        double totalScore = 0.0;
        int totalComponents = 0;

        for (int i = 0; i < Math.min(query.length(), target.length()); i++) {
            char qChar = query.charAt(i);
            char tChar = target.charAt(i);

            if (qChar >= 0xAC00 && qChar <= 0xD7AF && tChar >= 0xAC00 && tChar <= 0xD7AF) {
                // 한글 문자인 경우 구성요소별 비교
                int qCode = qChar - 0xAC00;
                int tCode = tChar - 0xAC00;

                int qCho = qCode / (21 * 28);
                int qJung = (qCode % (21 * 28)) / 28;
                int qJong = qCode % 28;

                int tCho = tCode / (21 * 28);
                int tJung = (tCode % (21 * 28)) / 28;
                int tJong = tCode % 28;

                // 초성 비교 (가중치 0.5)
                if (qCho == tCho) totalScore += 0.5;

                // 중성 비교 (가중치 0.3)
                if (qJung == tJung) totalScore += 0.3;
                else if (isSimilarVowel(qJung, tJung)) totalScore += 0.15;

                // 종성 비교 (가중치 0.2)
                if (qJong == tJong) totalScore += 0.2;

                totalComponents++;
            } else if (qChar == tChar) {
                totalScore += 1.0;
                totalComponents++;
            } else {
                totalComponents++;
            }
        }

        return totalComponents > 0 ? totalScore / totalComponents : 0.0;
    }

    /**
     * 유사한 모음인지 확인합니다 (키보드 배치상 가까운 모음들)
     */
    private static boolean isSimilarVowel(int vowel1, int vowel2) {
        // ㅡ(18)와 ㅜ(13), ㅏ(0)와 ㅓ(4) 등 키보드상 가까운 모음들
        int[][] similarPairs = {
            {18, 13}, // ㅡ, ㅜ
            {0, 4},   // ㅏ, ㅓ
            {8, 13},  // ㅗ, ㅜ
            {1, 5}    // ㅐ, ㅔ
        };

        for (int[] pair : similarPairs) {
            if ((vowel1 == pair[0] && vowel2 == pair[1]) ||
                (vowel1 == pair[1] && vowel2 == pair[0])) {
                return true;
            }
        }
        return false;
    }
}