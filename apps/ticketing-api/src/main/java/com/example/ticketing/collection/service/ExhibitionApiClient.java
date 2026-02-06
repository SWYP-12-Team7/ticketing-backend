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
    private static final int MAX_PAGES = 10;

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

        while (true) {
            try {
                final int currentPage = pageNo;

                ExhibitionApiResponse response = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("numOfRows", PAGE_SIZE)  // 정확한 파라미터명
                                .queryParam("pageNo", currentPage)    // 정확한 파라미터명
                                .queryParam("dtype", "전시")          // 필수: 분류명
                                .queryParam("title", "")              // 필수: 제목 (빈 값)
                                .build())
                        .retrieve()
                        .body(ExhibitionApiResponse.class);

                // API 오류 체크
                if (response == null || response.header() == null
                        || !"0000".equals(response.header().resultCode())) {
                    log.error("API 오류 - resultCode: {}, resultMsg: {}",
                            response != null && response.header() != null ? response.header().resultCode() : "null",
                            response != null && response.header() != null ? response.header().resultMsg() : "null");
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

                // 빈 페이지가 오면 종료 (페이징 정보가 없으므로)
                if (items.size() < PAGE_SIZE) {
                    log.info("마지막 페이지 도달 - {}건 미만", PAGE_SIZE);
                    break;
                }

                // 최대 페이지 제한
                if (pageNo >= MAX_PAGES) {
                    log.info("최대 페이지 도달 - {}페이지 제한", MAX_PAGES);
                    break;
                }

                pageNo++;
            } catch (Exception e) {
                log.error("공공 API 호출 실패 - pageNo: {}", pageNo, e);
                break;
            }
        }

        log.info("전시 데이터 수집 완료 - 총 {}건", allItems.size());
        return Collections.unmodifiableList(allItems);
    }
}