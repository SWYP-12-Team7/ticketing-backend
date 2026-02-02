package com.example.ticketing.collection.controller;

import com.example.ticketing.collection.facade.ExhibitionCollectionFacade;
import com.example.ticketing.collection.facade.ExhibitionCollectionFacade.CollectionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 수동 트리거 컨트롤러
@Slf4j
@RestController
@RequestMapping("/admin/collection")
@RequiredArgsConstructor
public class ExhibitionCollectionController {

    private final ExhibitionCollectionFacade exhibitionCollectionFacade;

    @PostMapping("/exhibitions")
    public ResponseEntity<CollectionResult> collectExhibitions() {
        log.info("전시 수집 API 호출");
        CollectionResult result = exhibitionCollectionFacade.collectAndSaveExhibitions();
        return ResponseEntity.ok(result);
    }
}