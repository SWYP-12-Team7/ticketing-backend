package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.user.application.dto.FolderResponse;
import com.example.ticketing.user.domain.FavoriteFolder;
import com.example.ticketing.user.domain.FavoriteFolderRepository;
import com.example.ticketing.user.domain.UserFavorite;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FolderUseCase {

    private final FavoriteFolderRepository folderRepository;
    private final UserFavoriteRepository favoriteRepository;

    private static final int MAX_FOLDER_COUNT = 10;

    @Transactional(readOnly = true)
    public List<FolderResponse> getFolders(Long userId) {
        return folderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(folder -> {
                    long popupCount = favoriteRepository.countByUserIdAndFolderIdAndCurationType(
                            userId, folder.getId(), CurationType.POPUP);
                    long exhibitionCount = favoriteRepository.countByUserIdAndFolderIdAndCurationType(
                            userId, folder.getId(), CurationType.EXHIBITION);
                    return FolderResponse.from(folder, popupCount, exhibitionCount);
                })
                .toList();

    }

    public FolderResponse createFolder(Long userId, String name) {
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
                .build();

        // 새 폴더는 개수가 0
        return FolderResponse.from(folderRepository.save(folder), 0, 0);
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