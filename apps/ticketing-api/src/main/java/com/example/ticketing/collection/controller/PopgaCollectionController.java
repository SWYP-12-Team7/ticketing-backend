package com.example.ticketing.collection.controller;

import com.example.ticketing.collection.facade.PopgaCollectionFacade;
import com.example.ticketing.collection.facade.PopgaCollectionFacade.CollectionResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Popga Collection", description = "popga.co.kr 팝업 데이터 수집 API")
@RestController
@RequestMapping("/admin/collection/popga")
@RequiredArgsConstructor
public class PopgaCollectionController {

    private final PopgaCollectionFacade popgaCollectionFacade;

    @Operation(
            summary = "Popga 팝업 데이터 수집",
            description = "popga.co.kr에서 팝업 데이터를 크롤링하고 Gemini로 모든 필드를 추출하여 저장합니다."
    )
    @PostMapping("/collect")
    public ResponseEntity<CollectionResult> collectPopups(
            @Parameter(description = "수집할 팝업 수 제한 (0 = 전체)")
            @RequestParam(defaultValue = "0") int limit
    ) {
        CollectionResult result = popgaCollectionFacade.collectAndSavePopups(limit);
        return ResponseEntity.ok(result);
    }
}
