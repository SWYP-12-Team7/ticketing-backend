package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.curation.domain.Curation;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.user.application.dto.FolderResponse;
import com.example.ticketing.user.domain.FavoriteFolder;
import com.example.ticketing.user.domain.FavoriteFolderRepository;
import com.example.ticketing.user.domain.UserFavorite;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FolderUseCase {

    private final FavoriteFolderRepository folderRepository;
    private final UserFavoriteRepository favoriteRepository;
    private final CurationRepository curationRepository;

    private static final int MAX_FOLDER_COUNT = 10;
    private static final int MAX_THUMBNAILS = 3;

    @Transactional(readOnly = true)
    public List<FolderResponse> getFolders(Long userId) {
        return folderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(folder -> {
                    long popupCount = favoriteRepository.countByUserIdAndFolderIdAndCurationType(
                            userId, folder.getId(), CurationType.POPUP);
                    long exhibitionCount = favoriteRepository.countByUserIdAndFolderIdAndCurationType(
                            userId, folder.getId(), CurationType.EXHIBITION);

                    // 최근 찜한 행사 썸네일 최대 3개 조회
                    List<Long> curationIds = favoriteRepository.findCurationIdsByUserIdAndFolderId(
                            userId, folder.getId(), PageRequest.of(0, MAX_THUMBNAILS));
                    List<String> thumbnails = curationIds.isEmpty()
                            ? Collections.emptyList()
                            : curationRepository.findAllById(curationIds).stream()
                                    .map(Curation::getThumbnail)
                                    .toList();

                    return FolderResponse.from(folder, popupCount, exhibitionCount, thumbnails);
                })
                .toList();
    }

    public FolderResponse createFolder(Long userId, String name, String color) {
        // 폴더 개수 제한
        if (folderRepository.countByUserId(userId) >= MAX_FOLDER_COUNT) {
            throw new CustomException(ErrorCode.FOLDER_LIMIT_EXCEEDED);
        }

        // 중복 이름 체크
        if (folderRepository.existsByUserIdAndName(userId, name)) {
            throw new CustomException(ErrorCode.DUPLICATE_FOLDER_NAME);
        }

        FavoriteFolder folder = FavoriteFolder.builder()
                .userId(userId)
                .name(name)
                .color(color)
                .build();

        // 새 폴더는 개수가 0, 썸네일도 없음
        return FolderResponse.from(folderRepository.save(folder), 0, 0, Collections.emptyList());
    }

    public void updateFolder(Long userId, Long folderId, String name) {
        FavoriteFolder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLDER_NOT_FOUND));

        // 중복 이름 체크 (자기 자신 제외)
        if (folderRepository.existsByUserIdAndName(userId, name) && !folder.getName().equals(name)) {
            throw new CustomException(ErrorCode.DUPLICATE_FOLDER_NAME);
        }

        folder.updateName(name);
    }

    public void deleteFolder(Long userId, Long folderId) {
        FavoriteFolder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLDER_NOT_FOUND));

        // 해당 폴더의 찜 항목들을 미분류로 이동
        List<UserFavorite> favorites = favoriteRepository.findByUserIdAndFolderId(userId, folderId);
        favorites.forEach(UserFavorite::removeFromFolder);

        folderRepository.delete(folder);
    }

    public void moveFavoriteToFolder(Long userId, Long favoriteId, Long folderId) {
        UserFavorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new CustomException(ErrorCode.FAVORITE_NOT_FOUND));

        // 본인 소유 확인
        if (!favorite.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FAVORITE_NOT_FOUND);
        }

        // 폴더 존재 확인 (folderId가 null이 아닌 경우)
        if (folderId != null) {
            folderRepository.findByIdAndUserId(folderId, userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.FOLDER_NOT_FOUND));
            favorite.moveToFolder(folderId);
        } else {
            favorite.removeFromFolder();
        }
    }
}