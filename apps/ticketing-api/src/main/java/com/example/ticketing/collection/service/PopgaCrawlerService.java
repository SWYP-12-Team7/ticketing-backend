package com.example.ticketing.collection.service;

import com.example.ticketing.collection.dto.PopgaPopupData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * popga.co.kr 사이트에서 팝업 데이터를 크롤링하고 Gemini로 파싱하는 서비스
 *
 * 2단계 파이프라인 방식:
 * Phase 1 - HTML 페이지 병렬 수집 (Jsoup, 멀티스레드)
 * Phase 2 - Gemini API 파싱 (RPM/RPD rate limiting 적용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PopgaCrawlerService {

    private final ChatClient.Builder chatClientBuilder;

    private static final String BASE_URL = "https://popga.co.kr";
    private static final String SITEMAP_URL_TEMPLATE = BASE_URL + "/sitemap/%d.xml";
    private static final int[] SITEMAP_INDICES = {2, 3, 4, 5};
    private static final Pattern POPUP_URL_PATTERN = Pattern.compile("/popup/(\\d+)");

    @Value("${crawler.sitemap.delay-ms:500}")
    private int sitemapDelayMs;

    @Value("${crawler.gemini.rpm:5}")
    private int geminiRpm;

    @Value("${crawler.gemini.rpd:20}")
    private int geminiRpd;

    @Value("${crawler.html-fetch.thread-pool-size:5}")
    private int htmlFetchThreadPoolSize;

    @Value("${crawler.html-fetch.timeout-ms:15000}")
    private int htmlFetchTimeoutMs;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    /**
     * HTML 수집 결과를 담는 중간 DTO
     */
    private record HtmlFetchResult(String url, String popgaId, String pageContent, String ogImage) {}

    /**
     * URL에서 popgaId를 추출
     */
    public String extractPopgaId(String url) {
        Matcher matcher = POPUP_URL_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * sitemap에서 팝업 URL 목록을 추출
     */
    public List<String> collectPopupUrls() {
        List<String> popupUrls = new ArrayList<>();

        for (int index : SITEMAP_INDICES) {
            String sitemapUrl = String.format(SITEMAP_URL_TEMPLATE, index);
            try {
                log.info("Sitemap 수집 중: {}", sitemapUrl);
                Document doc = Jsoup.connect(sitemapUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get();

                Elements locs = doc.select("loc");
                for (Element loc : locs) {
                    String url = loc.text();
                    if (POPUP_URL_PATTERN.matcher(url).find()) {
                        popupUrls.add(url);
                    }
                }
                log.info("Sitemap {} 에서 {} 개 팝업 URL 발견", index, locs.size());
                Thread.sleep(sitemapDelayMs);
            } catch (IOException e) {
                log.error("Sitemap 수집 실패: {}", sitemapUrl, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("총 {} 개 팝업 URL 수집 완료", popupUrls.size());
        return popupUrls;
    }

    /**
     * 여러 팝업 페이지를 크롤링하고 Gemini로 파싱 (2단계 파이프라인)
     *
     * Phase 1: HTML 페이지를 멀티스레드로 병렬 수집
     * Phase 2: Gemini API를 RPM 기반 rate limiting으로 순차 호출
     */
    public List<PopgaPopupData> crawlPopups(List<String> urls, int limit) {
        int effectiveLimit = limit > 0 ? Math.min(limit, geminiRpd) : Math.min(urls.size(), geminiRpd);
        List<String> targetUrls = urls.subList(0, Math.min(urls.size(), effectiveLimit));

        log.info("=== Phase 1: HTML 페이지 병렬 수집 시작 ({} 건, 스레드 풀: {}) ===",
                targetUrls.size(), htmlFetchThreadPoolSize);
        long phase1Start = System.currentTimeMillis();
        List<HtmlFetchResult> htmlResults = fetchHtmlPagesParallel(targetUrls);
        long phase1Duration = System.currentTimeMillis() - phase1Start;
        log.info("=== Phase 1 완료: {} 건 성공 (소요시간: {}ms) ===", htmlResults.size(), phase1Duration);

        long minIntervalMs = (60_000L / geminiRpm) + 1_000L;
        log.info("=== Phase 2: Gemini 파싱 시작 (RPM: {}, 호출 간격: {}ms, 최대: {}건) ===",
                geminiRpm, minIntervalMs, effectiveLimit);
        long phase2Start = System.currentTimeMillis();
        List<PopgaPopupData> results = parseWithGeminiRateLimited(htmlResults, effectiveLimit, minIntervalMs);
        long phase2Duration = System.currentTimeMillis() - phase2Start;
        log.info("=== Phase 2 완료: {} 건 파싱 성공 (소요시간: {}ms) ===", results.size(), phase2Duration);

        log.info("총 소요시간: {}ms", phase1Duration + phase2Duration);
        return results;
    }

    /**
     * Phase 1: HTML 페이지를 병렬로 수집
     */
    private List<HtmlFetchResult> fetchHtmlPagesParallel(List<String> urls) {
        ExecutorService executor = Executors.newFixedThreadPool(htmlFetchThreadPoolSize);
        try {
            List<CompletableFuture<HtmlFetchResult>> futures = urls.stream()
                    .map(url -> CompletableFuture.supplyAsync(() -> fetchSinglePage(url), executor))
                    .toList();

            return futures.stream()
                    .map(future -> {
                        try {
                            return future.get(htmlFetchTimeoutMs + 5_000L, TimeUnit.MILLISECONDS);
                        } catch (Exception e) {
                            log.warn("HTML 수집 타임아웃 또는 오류", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 단일 HTML 페이지 수집 (Gemini 호출 없이 HTML만 가져옴)
     */
    private HtmlFetchResult fetchSinglePage(String url) {
        try {
            Matcher matcher = POPUP_URL_PATTERN.matcher(url);
            String popgaId = matcher.find() ? matcher.group(1) : null;
            if (popgaId == null) {
                log.warn("팝업 ID 추출 실패: {}", url);
                return null;
            }

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                    .timeout(htmlFetchTimeoutMs)
                    .get();

            String pageContent = extractPageContent(doc);
            String ogImage = doc.select("meta[property=og:image]").attr("content");

            log.debug("HTML 수집 완료: {} (popgaId={})", url, popgaId);
            return new HtmlFetchResult(url, popgaId, pageContent, ogImage);

        } catch (IOException e) {
            log.error("HTML 페이지 수집 실패: {}", url, e);
            return null;
        }
    }

    /**
     * Phase 2: Gemini API로 파싱 (RPM 기반 rate limiting 적용)
     *
     * API 호출 간 최소 간격을 유지하되, API 응답 시간도 간격에 포함하여
     * 불필요한 대기를 최소화함
     */
    private List<PopgaPopupData> parseWithGeminiRateLimited(
            List<HtmlFetchResult> htmlResults, int limit, long minIntervalMs) {
        List<PopgaPopupData> results = new ArrayList<>();
        ChatClient chatClient = chatClientBuilder.build();
        long lastCallTimeMs = 0;
        int count = 0;

        for (HtmlFetchResult html : htmlResults) {
            if (count >= limit) break;

            try {
                // RPM 기반 rate limiting: 이전 호출로부터 최소 간격 보장
                if (lastCallTimeMs > 0) {
                    long elapsed = System.currentTimeMillis() - lastCallTimeMs;
                    long waitTime = minIntervalMs - elapsed;
                    if (waitTime > 0) {
                        log.debug("Gemini rate limit 대기: {}ms", waitTime);
                        Thread.sleep(waitTime);
                    }
                }

                log.info("Gemini 파싱 중 ({}/{}): {}", count + 1,
                        Math.min(htmlResults.size(), limit), html.url());

                lastCallTimeMs = System.currentTimeMillis();
                String prompt = buildExtractionPrompt(html.pageContent(), html.url());
                String response = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();

                PopgaPopupData data = parseGeminiResponse(response, html.popgaId());
                if (data != null && data.getTitle() != null) {
                    if (html.ogImage() != null && !html.ogImage().isEmpty()) {
                        data = data.toBuilder().thumbnailImageUrl(html.ogImage()).build();
                    }
                    results.add(data);
                    count++;
                    log.info("파싱 완료: {}", data.getTitle());
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Gemini 파싱 실패: {}", html.url(), e);
            }
        }

        return results;
    }

    private String extractPageContent(Document doc) {
        StringBuilder content = new StringBuilder();

        content.append("=== META ===\n");
        content.append("title: ").append(doc.select("meta[property=og:title]").attr("content")).append("\n");
        content.append("description: ").append(doc.select("meta[property=og:description]").attr("content")).append("\n");
        content.append("keywords: ").append(doc.select("meta[name=keywords]").attr("content")).append("\n");

        content.append("\n=== DATA ===\n");
        Elements scripts = doc.select("script");
        for (Element script : scripts) {
            String html = script.html();
            if (html.contains("self.__next_f.push")) {
                content.append(html).append("\n");
            }
        }

        content.append("\n=== BODY ===\n");
        String bodyText = doc.body().text();
        if (bodyText.length() > 3000) {
            bodyText = bodyText.substring(0, 3000);
        }
        content.append(bodyText);

        return content.toString();
    }

    private String buildExtractionPrompt(String pageContent, String url) {
        return """
            다음은 팝업스토어 정보 페이지의 내용입니다. 이 내용에서 팝업스토어 정보를 추출해서 JSON으로 반환해주세요.

            페이지 URL: %s

            페이지 내용:
            %s

            다음 JSON 형식으로만 응답해주세요. 다른 텍스트 없이 순수 JSON만 반환:
            {
                "title": "팝업스토어 제목",
                "subTitle": "부제목 또는 슬로건 (없으면 null)",
                "description": "상세 설명",
                "startDate": "2025-01-01",
                "endDate": "2025-01-31",
                "city": "서울",
                "district": "성동구",
                "placeName": "장소명",
                "address": "전체 주소",
                "latitude": 37.5445,
                "longitude": 127.0567,
                "operatingHours": {
                    "월": "11:00~20:00",
                    "화": "11:00~20:00"
                },
                "categories": ["패션"],
                "tags": ["태그1", "태그2"],
                "homepageUrl": "공식 홈페이지 URL (없으면 null)",
                "snsUrl": "SNS URL (없으면 null)",
                "isFree": true,
                "reservationType": "ALL"
            }

            규칙:
            - 날짜는 yyyy-MM-dd 형식
            - isFree: 무료면 true, 유료면 false (대부분 팝업은 무료)
            - reservationType: PRE_ORDER(사전예약 필수), ON_SITE(현장만), ALL(둘 다 가능)
            - 정보를 찾을 수 없으면 해당 필드는 null
            - operatingHours는 요일별 운영시간, 정보 없으면 null
            - 위도/경도를 찾을 수 없으면 null
            - categories는 반드시 다음 7개 중에서만 선택: 패션, 뷰티, F&B, 캐릭터, 테크, 라이프스타일, 가구&인테리어
            - 팝업 내용을 분석하여 가장 적합한 카테고리 1~2개를 선택
            """.formatted(url, pageContent);
    }

    private PopgaPopupData parseGeminiResponse(String response, String popgaId) {
        try {
            String jsonContent = extractJsonContent(response);
            log.debug("Gemini 응답 JSON (popgaId={}): {}", popgaId, jsonContent.substring(0, Math.min(500, jsonContent.length())));

            JsonNode node = objectMapper.readTree(jsonContent);

            return PopgaPopupData.builder()
                    .popgaId(popgaId)
                    .title(getTextOrNull(node, "title"))
                    .subTitle(getTextOrNull(node, "subTitle"))
                    .description(getTextOrNull(node, "description"))
                    .startDate(getDateOrNull(node, "startDate"))
                    .endDate(getDateOrNull(node, "endDate"))
                    .city(getTextOrNull(node, "city"))
                    .district(getTextOrNull(node, "district"))
                    .placeName(getTextOrNull(node, "placeName"))
                    .address(getTextOrNull(node, "address"))
                    .latitude(getDoubleOrNull(node, "latitude"))
                    .longitude(getDoubleOrNull(node, "longitude"))
                    .operatingHours(getMapOrNull(node, "operatingHours"))
                    .categories(getListOrNull(node, "categories"))
                    .tags(getListOrNull(node, "tags"))
                    .homepageUrl(getTextOrNull(node, "homepageUrl"))
                    .snsUrl(getTextOrNull(node, "snsUrl"))
                    .isFree(getBooleanOrNull(node, "isFree"))
                    .reservationType(getTextOrNull(node, "reservationType"))
                    .build();

        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", response, e);
            return null;
        }
    }

    private String extractJsonContent(String response) {
        if (response == null || response.isBlank()) {
            return "{}";
        }
        String trimmed = response.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) return null;
        return fieldNode.asText();
    }

    private LocalDate getDateOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) return null;
        try {
            return LocalDate.parse(fieldNode.asText());
        } catch (Exception e) {
            return null;
        }
    }

    private Double getDoubleOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) return null;
        return fieldNode.asDouble();
    }

    private Boolean getBooleanOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) return null;
        return fieldNode.asBoolean();
    }

    private Map<String, String> getMapOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull() || !fieldNode.isObject()) return null;
        Map<String, String> map = new HashMap<>();
        fieldNode.fields().forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue().asText()));
        return map;
    }

    private List<String> getListOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull() || !fieldNode.isArray()) return null;
        List<String> list = new ArrayList<>();
        fieldNode.forEach(item -> list.add(item.asText()));
        return list;
    }
}
