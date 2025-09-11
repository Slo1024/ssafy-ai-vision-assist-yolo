package com.project.lookey.product.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.lookey.product.dto.FoodSafetyResponse;
import com.project.lookey.product.entity.FoodSafetyClient;
import com.project.lookey.product.entity.Ingredient;
import com.project.lookey.product.entity.Product;
import com.project.lookey.product.repository.IngredientRepository;
import com.project.lookey.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientService {

    private final FoodSafetyClient client;
    private final ProductRepository productRepo;
    private final IngredientRepository ingRepo;
    private final ObjectMapper om = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // 접두 코드 제거 (예: CJ), G), HK) … 최대 4글자 + ')'
    private static final Pattern PREFIX_CODE = Pattern.compile("^\\s*[A-Za-z가-힣]{1,4}\\)\\s*");
    // 용량 제거 (250ml, 1.5L, 500mL, 250㎖, 1ℓ 등)
    private static final Pattern VOLUME = Pattern.compile("\\b\\d+(?:\\.\\d+)?\\s*(?:ml|mL|ML|㎖|L|ℓ)\\b");
    // 괄호 블록 전체 제거 (표시용 후보 생성 시에만 사용)
    private static final Pattern BRACKETS = Pattern.compile("[\\[\\(（【].*?[\\]\\)）】]");
    // 다중 공백
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s{2,}");

    // 불용/통칭 토큰 제거용(필요에 따라 보강)
    private static final List<Pattern> NOISE_PATTERNS = List.of(
            Pattern.compile("^기타\\s*.*$"),
            Pattern.compile("^식품첨가물.*$"),
            Pattern.compile("^혼합제제.*$"),
            Pattern.compile("^기호식품$"),
            Pattern.compile("^과.?채가공품$"),
            Pattern.compile("^기타\\s*농산가공품$"),
            Pattern.compile("^정제수$"),
            Pattern.compile("^향료$") // 너무 포괄적인 통칭
    );

    @Transactional
    public int importForSevenDrinks() throws Exception {
        List<Product> products = productRepo.findAll();
        int touched = 0;

        for (Product p : products) {
            if (p.getName() == null || p.getName().isBlank()) continue;

            List<String> candidates = buildCandidates(p.getName());
            log.info("== product #{} | name=[{}]", p.getId(), p.getName());
            log.debug("   candidates: {}", candidates);

            // 기존 원재료 제거
            ingRepo.deleteByProductId(p.getId());

            FoodSafetyResponse.Row picked = null;

            for (String q : candidates) {
                String json = client.c002(1, 300, q).block();   // 1~300까지 넉넉히
                if (json == null || json.isBlank()) {
                    log.debug("   (no json) q={}", q);
                    continue;
                }

                FoodSafetyResponse res = om.readValue(json, FoodSafetyResponse.class);
                if (res == null || res.C002() == null) {
                    log.debug("   (no C002) q={}", q);
                    continue;
                }

                var result = res.C002().RESULT();
                if (result != null) {
                    log.debug("   RESULT: {} {}", result.CODE(), result.MSG());
                    // INFO-000(정상) 외에는 다음 후보로
                    if (result.CODE() != null && !result.CODE().equals("INFO-000")) continue;
                }

                var rows = res.C002().row();
                if (rows == null || rows.isEmpty()) {
                    log.debug("   rows: 0");
                    continue;
                }
                log.debug("   rows: {}", rows.size());

                picked = pickBestRow(p.getName(), rows);
                log.debug("   picked: {}", picked == null ? "NONE" : picked.PRDLST_NM());
                if (picked != null) break;

                Thread.sleep(80); // 호출 텀
            }

            if (picked != null) {
                int saved = saveIngredients(p.getId(), picked.RAWMTRL_NM());
                touched += (saved > 0 ? 1 : 0);
            }

            Thread.sleep(120);
        }
        return touched;
    }

    /** 디버그용: 특정 productId 하나만 수행 */
    @Transactional
    public int importForOne(Long productId) throws Exception {
        Product p = productRepo.findById(productId).orElseThrow();
        List<String> candidates = buildCandidates(p.getName());
        log.info("== (ONE) product #{} | name=[{}]", p.getId(), p.getName());
        log.info("   candidates: {}", candidates);

        ingRepo.deleteByProductId(p.getId());

        FoodSafetyResponse.Row picked = null;

        for (String q : candidates) {
            String json = client.c002(1, 300, q).block();
            if (json == null || json.isBlank()) { log.info("   (no json) q={}", q); continue; }

            FoodSafetyResponse res = om.readValue(json, FoodSafetyResponse.class);
            if (res == null || res.C002() == null) { log.info("   (no C002) q={}", q); continue; }

            var result = res.C002().RESULT();
            log.info("   RESULT: {}", result == null ? "null" : result.CODE() + " " + result.MSG());
            if (result != null && result.CODE() != null && !result.CODE().equals("INFO-000")) continue;

            var rows = res.C002().row();
            log.info("   rows: {}", rows == null ? 0 : rows.size());
            if (rows == null || rows.isEmpty()) continue;

            picked = pickBestRow(p.getName(), rows);
            log.info("   picked: {}", picked == null ? "NONE" : picked.PRDLST_NM());
            if (picked != null) break;
        }

        if (picked != null) {
            return saveIngredients(p.getId(), picked.RAWMTRL_NM());
        }
        return 0;
    }

    /** RAWMTRL_NM 문자열을 파싱해 ingredient 테이블에 저장 */
    private int saveIngredients(Long productId, String raw) {
        if (raw == null || raw.isBlank()) return 0;

        String[] toks = splitIngredients(raw);
        // 정리 + 노이즈 필터 + 중복 제거
        LinkedHashSet<String> uniq = Arrays.stream(toks)
                .map(this::normalizeIngredientToken)
                .filter(s -> !s.isBlank() && !isNoise(s))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (uniq.isEmpty()) return 0;

        List<Ingredient> batch = new ArrayList<>(uniq.size());
        for (String name : uniq) {
            Ingredient ing = new Ingredient();
            ing.setProduct_id(productId);
            ing.setName(name);
            batch.add(ing);
        }
        ingRepo.saveAll(batch);
        log.info("   saved ingredients: {}", uniq.size());
        return batch.size();
    }

    /** 개별 원재료 토큰 정리 */
    private String normalizeIngredientToken(String s) {
        if (s == null) return "";
        s = s.trim()
                .replaceAll("^[•·\\-*]+\\s*", "")  // 불릿 제거
                .replaceAll("\\s{2,}", " ");      // 다중 공백
        // 필요시 더 제거: “함유/등/외/원재료명:” 같은 꼬리표
        s = s.replaceAll("^원재료명\\s*[:：]\\s*", "")
                .replaceAll("\\b함유\\b", "")
                .replaceAll("\\b등\\b", "")
                .replaceAll("\\b외\\b", "")
                .replaceAll("\\s{2,}", " ")
                .trim();
        if (s.length() > 120) s = s.substring(0, 120).trim();
        return s;
    }

    private boolean isNoise(String token) {
        String t = token.trim();
        if (t.length() <= 1) return true;
        for (Pattern p : NOISE_PATTERNS) {
            if (p.matcher(t).find()) return true;
        }
        return false;
    }


    /** DB 이름에서 다양한 검색 후보 생성 */
    private List<String> buildCandidates(String original) {
        if (original == null || original.isBlank()) return List.of();
        // 원본 이름 그대로만 후보로 사용
        return List.of(original.trim());
    }

    /** 이름 전처리 (접두코드/괄호/다중공백 등) */
    private String cleanName(String s) {
        if (s == null) return "";
        s = PREFIX_CODE.matcher(s).replaceFirst(""); // CJ) 제거
        s = s.replace('㎖', 'm');                    // 비표준 문자 정규화(선택)
        s = MULTI_SPACE.matcher(s).replaceAll(" ");
        return s.trim();
    }

    /** rows에서 최적 후보 선택 */
    private FoodSafetyResponse.Row pickBestRow(String dbName, List<FoodSafetyResponse.Row> rows) {
        if (rows == null || rows.isEmpty()) return null;

        String nDb = normalizeForCompare(cleanName(dbName));

        // 1) 강한 contains(양방향)
        for (FoodSafetyResponse.Row r : rows) {
            String nRow = normalizeForCompare(cleanName(r.PRDLST_NM()));
            if (nRow.contains(nDb) || nDb.contains(nRow)) return r;
        }

        // 2) 앞쪽 두 단어 기준 비교
        String head2 = Arrays.stream(cleanName(dbName).split("\\s+"))
                .limit(2).collect(Collectors.joining(" "));
        String nHead2 = normalizeForCompare(head2);
        if (!nHead2.isBlank()) {
            for (FoodSafetyResponse.Row r : rows) {
                String nRow = normalizeForCompare(cleanName(r.PRDLST_NM()));
                if (nRow.contains(nHead2)) return r;
            }
        }

        // 3) 유사도 최고값 (임계 0.76로 완화)
        double bestScore = 0.0;
        FoodSafetyResponse.Row best = null;
        for (FoodSafetyResponse.Row r : rows) {
            String nRow = normalizeForCompare(cleanName(r.PRDLST_NM()));
            double s = jaroWinkler(nDb, nRow);
            if (s > bestScore) { bestScore = s; best = r; }
        }
        if (bestScore >= 0.76) return best;

        // 4) 마지막 안전망(테스트 중에만 권장)
        rows.sort(Comparator.comparingInt(r -> cleanName(r.PRDLST_NM()).length()));
        return rows.get(0);
    }

    private String normalizeForCompare(String s) {
        return s == null ? "" :
                s.replaceAll("\\s+", "")
                        .replaceAll("[^0-9A-Za-z가-힣]", "")
                        .toLowerCase();
    }

    /* =========================================================
       원재료 분할(괄호 인지)
       ========================================================= */

    /** 괄호 깊이를 인지해 최상위 구분자(, ; / · • |)에서만 자른다 */
    private String[] splitIngredients(String raw) {
        if (raw == null) return new String[0];
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int round=0, square=0, curly=0;

        for (int i=0; i<raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '(' -> round++;
                case ')' -> round = Math.max(0, round-1);
                case '[' -> square++;
                case ']' -> square = Math.max(0, square-1);
                case '{' -> curly++;
                case '}' -> curly = Math.max(0, curly-1);
            }
            if (round==0 && square==0 && curly==0 && isTopLevelDelimiter(c)) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length()>0) out.add(cur.toString());

        return out.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }

    private boolean isTopLevelDelimiter(char c) {
        return c==',' || c==';' || c=='/' || c=='·' || c=='•' || c=='|';
    }

    /* =========================================================
       유사도(Jaro-Winkler)
       ========================================================= */

    private double jaroWinkler(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        int[] mtp = matches(s1, s2);
        float m = mtp[0];
        if (m == 0) return 0.0f;
        float j = ((m / s1.length()) + (m / s2.length()) + ((m - mtp[1]) / m)) / 3;
        return j < 0.7 ? j : j + Math.min(0.1, 1.0 / mtp[3]) * mtp[2] * (1 - j);
    }

    private int[] matches(String s1, String s2) {
        String max = s1.length() > s2.length() ? s1 : s2;
        String min = s1.length() > s2.length() ? s2 : s1;
        int range = Math.max(max.length() / 2 - 1, 0);

        boolean[] matchFlags = new boolean[max.length()];
        int[] matchIndexes = new int[min.length()];
        Arrays.fill(matchIndexes, -1);

        int matches = 0;
        for (int mi = 0; mi < min.length(); mi++) {
            char c1 = min.charAt(mi);
            for (int xi = Math.max(mi - range, 0),
                 xn = Math.min(mi + range + 1, max.length()); xi < xn; xi++) {
                if (!matchFlags[xi] && c1 == max.charAt(xi)) {
                    matchIndexes[mi] = xi;
                    matchFlags[xi] = true;
                    matches++;
                    break;
                }
            }
        }

        char[] ms1 = new char[matches];
        char[] ms2 = new char[matches];
        for (int i = 0, si = 0; i < min.length(); i++)
            if (matchIndexes[i] != -1) ms1[si++] = min.charAt(i);
        for (int i = 0, si = 0; i < max.length(); i++)
            if (matchFlags[i]) ms2[si++] = max.charAt(i);

        int transpositions = 0;
        for (int i = 0; i < ms1.length; i++)
            if (ms1[i] != ms2[i]) transpositions++;
        int prefix = 0;
        for (int i = 0; i < Math.min(4, min.length()); i++)
            if (s1.charAt(i) == s2.charAt(i)) prefix++;
            else break;
        return new int[]{matches, transpositions / 2, prefix, max.length()};
    }
}
