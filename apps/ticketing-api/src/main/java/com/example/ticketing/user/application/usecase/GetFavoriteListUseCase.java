package com.example.ticketing.user.application.usecase;

import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.user.application.dto.FavoriteListResponse;
import com.example.ticketing.user.application.dto.FavoriteListResponse.FavoriteItem;
import com.example.ticketing.user.domain.UserFavorite;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFavoriteListUseCase {

    private final UserFavoriteRepository userFavoriteRepository;
    private final CurationRepository curationRepository;

    public FavoriteListResponse execute(Long userId, String region, Long folderId, Pageable pageable) {
        // 1. 사용자의 찜 목록 조회
        Page<UserFavorite> favoritePage;
        if (folderId != null) {
            favoritePage = userFavoriteRepository.findByUserIdAndFolderId(userId, folderId, pageable);
        } else {
            favoritePage = userFavoriteRepository.findByUserId(userId, pageable);
        }

        List<Long> curationIds = favoritePage.getContent().stream()
                .map(UserFavorite::getCurationId)
                .toList();

        if (curationIds.isEmpty()) {
            return FavoriteListResponse.empty();
        }

        // 2. Curation 정보 조회 (지역 필터링 포함)
        List<Curation> curations;
        if (region != null && !region.isBlank()) {
            curations = curationRepository.findByIdInAndRegion(curationIds, region);
        } else {
            curations = curationRepository.findAllById(curationIds);
        }

        Map<Long, Curation> curationMap = curations.stream()
                .collect(Collectors.toMap(Curation::getId, c -> c));

        // 3. FavoriteItem 생성 (찜 순서 유지)
        Map<Long, UserFavorite> favoriteMap = favoritePage.getContent().stream()
                .collect(Collectors.toMap(UserFavorite::getCurationId, f -> f));

        List<FavoriteItem> items = curations.stream()
                .map(curation -> {
                    UserFavorite favorite = favoriteMap.get(curation.getId());
                    return FavoriteItem.from(curation, favorite.getFolderId());
                })
                .toList();

        return new FavoriteListResponse(
                items,
                favoritePage.getNumber(),
                favoritePage.getTotalPages(),
                favoritePage.getTotalElements()
        );
    }
}