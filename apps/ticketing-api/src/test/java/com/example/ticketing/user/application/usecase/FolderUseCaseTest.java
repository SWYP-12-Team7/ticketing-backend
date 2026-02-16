package com.example.ticketing.user.application.usecase;

import com.example.ticketing.common.exception.CustomException;
import com.example.ticketing.common.exception.ErrorCode;
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.repository.CurationRepository;
import com.example.ticketing.user.application.dto.FolderResponse;
import com.example.ticketing.user.domain.FavoriteFolder;
import com.example.ticketing.user.domain.FavoriteFolderRepository;
import com.example.ticketing.user.domain.UserFavorite;
import com.example.ticketing.user.domain.UserFavoriteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FolderUseCaseTest {

    @Mock
    private FavoriteFolderRepository folderRepository;
    @Mock
    private UserFavoriteRepository favoriteRepository;
    @Mock
    private CurationRepository curationRepository;

    @InjectMocks
    private FolderUseCase folderUseCase;

    @Nested
    @DisplayName("getFolders 메서드")
    class GetFolders {

        @Test
        @DisplayName("성공: 폴더 목록과 개수 조회")
        void success() {
            // given
            Long userId = 1L;
            FavoriteFolder folder = FavoriteFolder.builder()
                    .userId(userId)
                    .name("서울 전시")
                    .build();

            given(folderRepository.findByUserIdOrderByCreatedAtDesc(userId))
                    .willReturn(List.of(folder));
            given(favoriteRepository.countByUserIdAndFolderIdAndCurationType(userId, folder.getId(), CurationType.POPUP))
                    .willReturn(3L);
            given(favoriteRepository.countByUserIdAndFolderIdAndCurationType(userId, folder.getId(), CurationType.EXHIBITION))
                    .willReturn(2L);

            // when
            List<FolderResponse> response = folderUseCase.getFolders(userId);

            // then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).name()).isEqualTo("서울 전시");
            assertThat(response.get(0).totalCount()).isEqualTo(5);
            assertThat(response.get(0).popupCount()).isEqualTo(3);
            assertThat(response.get(0).exhibitionCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("createFolder 메서드")
    class CreateFolder {

        @Test
        @DisplayName("성공: 폴더 생성")
        void success() {
            // given
            Long userId = 1L;
            String name = "새 폴더";
            String color = "#F36012";
            FavoriteFolder folder = FavoriteFolder.builder()
                    .userId(userId)
                    .name(name)
                    .color(color)
                    .build();

            given(folderRepository.countByUserId(userId)).willReturn(0);
            given(folderRepository.existsByUserIdAndName(userId, name)).willReturn(false);
            given(folderRepository.save(any(FavoriteFolder.class))).willReturn(folder);

            // when
            FolderResponse response = folderUseCase.createFolder(userId, name, color);

            // then
            assertThat(response.name()).isEqualTo(name);
            assertThat(response.color()).isEqualTo(color);
            assertThat(response.totalCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("실패: 폴더 개수 초과")
        void failWhenFolderLimitExceeded() {
            // given
            Long userId = 1L;
            given(folderRepository.countByUserId(userId)).willReturn(10);

            // when & then
            assertThatThrownBy(() -> folderUseCase.createFolder(userId, "새 폴더", "#F36012"))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FOLDER_LIMIT_EXCEEDED);
        }

        @Test
        @DisplayName("실패: 중복된 폴더 이름")
        void failWhenDuplicateName() {
            // given
            Long userId = 1L;
            String name = "기존 폴더";
            given(folderRepository.countByUserId(userId)).willReturn(1);
            given(folderRepository.existsByUserIdAndName(userId, name)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> folderUseCase.createFolder(userId, name, "#F36012"))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_FOLDER_NAME);
        }
    }

    @Nested
    @DisplayName("moveFavoriteToFolder 메서드")
    class MoveFavoriteToFolder {

        @Test
        @DisplayName("성공: 찜을 폴더로 이동")
        void success() {
            // given
            Long userId = 1L;
            Long favoriteId = 10L;
            Long folderId = 5L;

            UserFavorite favorite = UserFavorite.builder()
                    .userId(userId)
                    .curationId(100L)
                    .curationType(CurationType.POPUP)
                    .build();
            FavoriteFolder folder = FavoriteFolder.builder()
                    .userId(userId)
                    .name("폴더")
                    .build();

            given(favoriteRepository.findById(favoriteId)).willReturn(Optional.of(favorite));
            given(folderRepository.findByIdAndUserId(folderId, userId)).willReturn(Optional.of(folder));

            // when
            folderUseCase.moveFavoriteToFolder(userId, favoriteId, folderId);

            // then
            assertThat(favorite.getFolderId()).isEqualTo(folderId);
        }

        @Test
        @DisplayName("성공: 찜을 미분류로 이동 (folderId = null)")
        void successMoveToUncategorized() {
            // given
            Long userId = 1L;
            Long favoriteId = 10L;

            UserFavorite favorite = UserFavorite.builder()
                    .userId(userId)
                    .curationId(100L)
                    .curationType(CurationType.POPUP)
                    .build();
            favorite.moveToFolder(5L); // 기존 폴더 설정

            given(favoriteRepository.findById(favoriteId)).willReturn(Optional.of(favorite));

            // when
            folderUseCase.moveFavoriteToFolder(userId, favoriteId, null);

            // then
            assertThat(favorite.getFolderId()).isNull();
        }
    }
}