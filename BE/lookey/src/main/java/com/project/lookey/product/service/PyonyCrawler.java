package com.project.lookey.product.service;

import com.project.lookey.product.entity.Product;
import com.project.lookey.product.repository.ProductRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PyonyCrawler {

    private static final String BASE = "https://pyony.com";
    private static final String LIST = BASE + "/brands/seven/";
    private static final Pattern MONEY = Pattern.compile("(\\d[\\d,]*)\\s*원");
    private static final Pattern EVENT = Pattern.compile("\\b([1-4]\\+1)\\b");

    private final ProductRepository repo;

    public PyonyCrawler(ProductRepository repo) {
        this.repo = repo;
    }

    /** 세븐일레븐 - 음료(category=1) 페이지 범위를 크롤링 */
    public void crawlDrinks(int startPage, int endPage) throws Exception {
        for (int page = startPage; page <= endPage; page++) {
            String url = LIST + "?category=1&page=" + page + "&event_type=&item=&sort=&price=&q=";

            Document doc = getWithRetry(url, 3);
            if (doc == null) break;

            // 상세 링크들 수집 (/brands/seven/products/{id}/ 형태)
            Elements links = doc.select("a[href*=/brands/seven/products/]");
            if (links.isEmpty()) break;

            for (Element a : links) {
                String href = BASE + a.attr("href");
                if (!href.matches(".*/brands/seven/products/\\d+/?$")) continue;

                Detail d = fetchDetail(href);
                if (d == null || d.name == null || d.name.isBlank()) continue;

                // (name, brand)로 upsert (현재 스키마 기준)
                final String brand = "7-ELEVEN";
                Optional<Product> found = repo.findByNameAndBrand(d.name, brand);
                Product p = found.orElseGet(Product::new);

                p.setName(d.name);
                p.setBrand(brand);
                p.setPrice(d.stdPrice != null ? d.stdPrice : 0);
                p.setEvent(d.event);

                repo.save(p);

                // 매너 딜레이
                sleepRandom(300, 700);
            }

            sleepRandom(700, 1200);
        }
    }

    /** 상세 페이지에서 이름/가격/행사 추출 */
    private Detail fetchDetail(String href) throws Exception {
        Document doc = getWithRetry(href, 3);
        if (doc == null) return null;

        // 1) 이름 후보: og:title → 개별 h1 → 기타 h 태그
        String name =
                attrOrNull(doc.selectFirst("meta[property=og:title]"), "content");
        if (isBlank(name)) name = ownTextOrNull(doc.selectFirst("h1.product-title"));
        if (isBlank(name)) name = ownTextOrNull(doc.selectFirst(".product-detail h1"));
        if (isBlank(name)) name = ownTextOrNull(doc.selectFirst("h1,h2,h3"));

        // 가격/행사는 본문에서 추출
        String all = doc.text().replaceAll("\\s+", " ");
        Integer std = firstMoney(all);
        Integer promo = secondMoney(all);
        String event = firstEvent(all);

        // 이름에서 가격/행사 꼬리표 제거
        if (!isBlank(name)) {
            name = name
                    .replaceAll("세븐일레븐\\s*", "")
                    .replaceAll("행사상품 가격 보기", "")
                    .replaceAll("(\\d[\\d,]*)\\s*원", "")
                    .replaceAll("\\(\\s*\\d[\\d,]*\\s*원\\s*\\)", "")
                    .replaceAll("\\b[1-4]\\+1\\b", "")
                    .replaceAll("\\s{2,}", " ")
                    .trim();
            
            // 제품명 정제 로직 추가
            name = cleanProductName(name);
        }
        return new Detail(name, std, promo, event);
    }

    private Document getWithRetry(String url, int maxRetry) throws InterruptedException {
        int attempt = 0;
        while (attempt++ < maxRetry) {
            try {
                Connection conn = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (compatible; lookey-crawler)")
                        .timeout(15000)
                        .ignoreHttpErrors(true);
                return conn.get();
            } catch (IOException e) {
                // 재시도
                if (attempt >= maxRetry) return null;
                sleepRandom(500, 1500);
            }
        }
        return null;
    }

    private Integer firstMoney(String s) {
        Matcher m = MONEY.matcher(s);
        return m.find() ? parseInt(m.group(1)) : null;
    }

    private Integer secondMoney(String s) {
        Matcher m = MONEY.matcher(s);
        if (m.find() && m.find()) return parseInt(m.group(1));
        return null;
    }

    private String firstEvent(String s) {
        Matcher m = EVENT.matcher(s);
        return m.find() ? m.group(1) : null;
    }

    private int parseInt(String n) {
        return Integer.parseInt(n.replace(",", ""));
    }

    private String attrOrNull(Element el, String attr) {
        return (el == null) ? null : emptyToNull(el.attr(attr));
    }

    private String ownTextOrNull(Element el) {
        return (el == null) ? null : emptyToNull(el.ownText());
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void sleepRandom(int minMs, int maxMs) throws InterruptedException {
        int ms = ThreadLocalRandom.current().nextInt(minMs, maxMs + 1);
        Thread.sleep(ms);
    }
    
    /**
     * 제품명 정제: 회사명 제거, 용량 단위 대문자 변환, 용량 뒤 불필요한 문자 제거
     */
    private String cleanProductName(String name) {
        if (isBlank(name)) return name;
        
        String cleaned = name;
        
        // 1. 회사명 제거 (농심), 롯데) 등)
        cleaned = cleaned.replaceAll("^[^)]+\\)\\s*", "");
        
        // 2. ml, l을 ML, L로 대문자 변환 (대소문자 구분 없이)
        cleaned = cleaned.replaceAll("(?i)ml", "ML");
        cleaned = cleaned.replaceAll("(?i)l(?![a-zA-Z])", "L");
        
        // 3. 용량 뒤 불필요한 문자 제거 (캔, 병, 페트 등)
        cleaned = cleaned.replaceAll("(\\d+)(ML|L)[가-힣]*.*?$", "$1$2");
        
        // 4. 연속된 공백 정리
        cleaned = cleaned.replaceAll("\\s{2,}", " ").trim();
        
        return cleaned;
    }

    private record Detail(String name, Integer stdPrice, Integer promoPrice, String event) {}
}
