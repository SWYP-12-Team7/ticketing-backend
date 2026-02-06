package com.example.ticketing.collection.service;

import com.example.ticketing.collection.dto.ExhibitionApiResponse;
import com.example.ticketing.collection.dto.ExhibitionApiResponse.Item;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

// 공공 API 호출 (페이징 처리 포함)
@Slf4j
@Service
public class ExhibitionApiClient {

    private final RestClient restClient;
    private final String serviceKey;

    private static final int PAGE_SIZE = 10;
    private static final int MAX_PAGES = 2;
    private static final int MAX_RETRIES = 3;        // 최대 재시도 횟수
    private static final int RETRY_DELAY_MS = 2000;  // 재시도 대기 시간 (2초)
    private static final int PAGE_DELAY_MS = 1000;   // 페이지 간 대기 시간 (1초)

    public ExhibitionApiClient(
            @Value("${exhibition.api.base-url}") String baseUrl,
            @Value("${exhibition.api.service-key}") String serviceKey
    ) {
        XmlMapper xmlMapper = new XmlMapper();
        MappingJackson2XmlHttpMessageConverter xmlConverter = new MappingJackson2XmlHttpMessageConverter(xmlMapper);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .messageConverters(converters -> {
                    converters.add(0, xmlConverter);
                })
                .build();
        this.serviceKey = serviceKey;
    }

    public List<Item> fetchExhibitions() {
        log.info("공공 API 전시 데이터 수집 시작");

        List<Item> allItems = new ArrayList<>();
        int pageNo = 1;

        while (pageNo <= MAX_PAGES) {
            ExhibitionApiResponse response = fetchPageWithRetry(pageNo);

            // 응답 실패 시 중단
            if (response == null) {
                log.error("페이지 {} 수집 실패 - 재시도 초과", pageNo);
                break;
            }

            // API 오류 체크
            if (response.header() == null || !"0000".equals(response.header().resultCode())) {
                log.error("API 오류 - resultCode: {}, resultMsg: {}",
                        response.header() != null ? response.header().resultCode() : "null",
                        response.header() != null ? response.header().resultMsg() : "null");
                break;
            }

            if (response.body() == null
                    || response.body().items() == null
                    || response.body().items().item() == null
                    || response.body().items().item().isEmpty()) {
                log.info("더 이상 데이터가 없습니다. pageNo: {}", pageNo);
                break;
            }

            List<Item> items = response.body().items().item();
            allItems.addAll(items);
            log.info("페이지 {} 수집 완료 - {}건 (누적: {}건)", pageNo, items.size(), allItems.size());

            // 마지막 페이지 체크
            if (items.size() < PAGE_SIZE) {
                log.info("마지막 페이지 도달 - {}건 미만", PAGE_SIZE);
                break;
            }

            pageNo++;

            // 다음 페이지 호출 전 딜레이 (API 부하 방지)
            sleep(PAGE_DELAY_MS);
        }

        log.info("전시 데이터 수집 완료 - 총 {}건", allItems.size());
        return Collections.unmodifiableList(allItems);
    }

    private ExhibitionApiResponse fetchPageWithRetry(int pageNo) {
        for (int retry = 1; retry <= MAX_RETRIES; retry++) {
            try {
                ExhibitionApiResponse response = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("numOfRows", PAGE_SIZE)
                                .queryParam("pageNo", pageNo)
                                .queryParam("dtype", "전시")
                                .queryParam("title", "")
                                .build())
                        .retrieve()
                        .body(ExhibitionApiResponse.class);

                return response;
            } catch (Exception e) {
                log.warn("페이지 {} 호출 실패 (시도 {}/{}) - {}", pageNo, retry, MAX_RETRIES, e.getMessage());
                if (retry < MAX_RETRIES) {
                    sleep(RETRY_DELAY_MS);
                }
            }
        }
        return null;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}