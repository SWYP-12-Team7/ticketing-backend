package com.example.ticketing.curation.controller;

import com.example.ticketing.common.security.CurrentUser;
import com.example.ticketing.curation.dto.ToggleFavoriteRequest;
import com.example.ticketing.curation.facade.CurationFacade;
import com.example.ticketing.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/curations")
@RequiredArgsConstructor
public class CurationController {

    private final CurationFacade curationFacade;

    // 팝업

    // 전시

    @PostMapping("/favorites")
    public void toggleFavorite(@CurrentUser User user,
                               @RequestBody ToggleFavoriteRequest request) {
        curationFacade.toggle(
                user.getId(),
                request.curationId(),
                request.curationType()
        );
    }

}
