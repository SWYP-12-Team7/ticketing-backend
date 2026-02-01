package com.example.ticketing.collection.controller;

import com.example.ticketing.collection.facade.PopupCollectionFacade;
import com.example.ticketing.collection.facade.PopupCollectionFacade.CollectionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/collection")
@RequiredArgsConstructor
public class PopupCollectionController {

    private final PopupCollectionFacade popupCollectionFacade;

    @PostMapping("/popups")
    public ResponseEntity<CollectionResponse> collectPopups() {
        log.info("팝업 수집 API 호출");

        CollectionResult result = popupCollectionFacade.collectAndSavePopups();

        return ResponseEntity.ok(new CollectionResponse(
                result.collectedCount(),
                result.savedCount(),
                result.skippedCount(),
                result.message()
        ));
    }

    public record CollectionResponse(
            int collectedCount,
            int savedCount,
            int skippedCount,
            String message
    ) {
    }
}