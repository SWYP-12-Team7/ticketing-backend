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
import org.springframework.stereotype.Service;

/**
 * popga.co.kr 사이트에서 팝업 데이터를 크롤링하고 Gemini로 파싱하는 서비스
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
    private static final int REQUEST_DELAY_MS = 1000;
    private static final long GEMINI_DELAY_SECONDS = 5;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

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
                Thread.sleep(REQUEST_DELAY_MS);
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
     * 여러 팝업 페이지를 크롤링하고 Gemini로 파싱
     */
    public List<PopgaPopupData> crawlPopups(List<String> urls, int limit) {
        List<PopgaPopupData> results = new ArrayList<>();
        ChatClient chatClient = chatClientBuilder.build();
        int count = 0;

        for (String url : urls) {
            if (limit > 0 && count >= limit) {
                break;
            }

            try {
                log.info("크롤링 중 ({}/{}): {}", count + 1, Math.min(urls.size(), limit > 0 ? limit : urls.size()), url);

                PopgaPopupData data = crawlAndParseWithGemini(chatClient, url);
                if (data != null && data.getTitle() != null) {
                    results.add(data);
                    count++;
                    log.info("파싱 완료: {}", data.getTitle());
                }

                TimeUnit.SECONDS.sleep(GEMINI_DELAY_SECONDS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("크롤링 실패: {}", url, e);
            }
        }

        log.info("총 {} 개 팝업 크롤링 완료", results.size());
        return results;
    }

    /**
     * 단일 페이지를 크롤링하고 Gemini로 모든 필드 추출
     */
    private PopgaPopupData crawlAndParseWithGemini(ChatClient chatClient, String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            Matcher matcher = POPUP_URL_PATTERN.matcher(url);
            String popgaId = matcher.find() ? matcher.group(1) : null;
            if (popgaId == null) {
                log.warn("팝업 ID 추출 실패: {}", url);
                return null;
            }

            String pageContent = extractPageContent(doc);
            String prompt = buildExtractionPrompt(pageContent, url);
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            PopgaPopupData data = parseGeminiResponse(response, popgaId);

            String ogImage = doc.select("meta[property=og:image]").attr("content");
            if (ogImage != null && !ogImage.isEmpty() && data != null) {
                data = PopgaPopupData.builder()
                        .popgaId(data.getPopgaId())
                        .title(data.getTitle())
                        .subTitle(data.getSubTitle())
                        .description(data.getDescription())
                        .thumbnailImageUrl(ogImage)
                        .startDate(data.getStartDate())
                        .endDate(data.getEndDate())
                        .city(data.getCity())
                        .district(data.getDistrict())
                        .placeName(data.getPlaceName())
                        .address(data.getAddress())
                        .latitude(data.getLatitude())
                        .longitude(data.getLongitude())
                        .operatingHours(data.getOperatingHours())
                        .categories(data.getCategories())
                        .tags(data.getTags())
                        .homepageUrl(data.getHomepageUrl())
                        .snsUrl(data.getSnsUrl())
                        .isFree(data.getIsFree())
                        .reservationType(data.getReservationType())
                        .build();
            }

            return data;

        } catch (IOException e) {
            log.error("페이지 크롤링 실패: {}", url, e);
            return null;
        }
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
                "categories": ["카테고리1", "카테고리2"],
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
