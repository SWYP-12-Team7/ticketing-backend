# 팝업 상세 조회 - 조회 이력 기록 적용 가이드

## 🎯 TL;DR (요약)
**전시 상세 조회 패턴을 그대로 따라하면 됩니다!**
- 조회 이력 인프라는 이미 구축됨 ✅
- `CurationType.EXHIBITION` → `CurationType.POPUP`만 바꾸면 됨
- 3개 파일만 수정하면 완료 (PopupService, PopupFacade, PopupController)

---

## ✅ 이미 구축된 공통 인프라 (손댈 필요 없음)

| 구성요소 | 위치 | 설명 |
|---------|------|------|
| `CurationViewHistory` 엔티티 | `curation/domain/` | 팝업/전시 조회 이력 통합 테이블 |
| `CurationViewedEvent` 이벤트 | `curation/event/` | 조회 이벤트 (팝업/전시 공통) |
| `CurationViewHistoryListener` | `curation/event/` | 비동기 이력 저장 리스너 |
| `AsyncConfig` | `common/config/` | 비동기 처리 설정 |
| DB 마이그레이션 | `V22__create_curation_view_history.sql` | 테이블 생성 완료 |

---

## 📝 체크리스트 (3단계)

### ☐ Step 1: PopupService 수정
- [ ] `ApplicationEventPublisher` 필드 추가
- [ ] `getPopupDetail()` 메서드에 조회수 증가 + 이벤트 발행 코드 추가
- [ ] `@Transactional(readOnly = true)` → `@Transactional`로 변경

### ☐ Step 2: PopupFacade 수정
- [ ] `userId` 파라미터 Service로 전달

### ☐ Step 3: PopupController 수정
- [ ] `@CurrentUser` 어노테이션으로 userId 받아서 Facade로 전달

---

## 🔧 구현 방법 (복사 붙여넣기 가능)

### Step 1: PopupService 수정

**📁 파일 위치**: `apps/ticketing-api/src/main/java/com/example/ticketing/curation/service/PopupService.java`

#### 1-1. ApplicationEventPublisher 필드 추가 (18-19번째 줄 주석 해제)

```java
@Service
@RequiredArgsConstructor
public class PopupService {
    private final PopupRepository popupRepository;
    private final ApplicationEventPublisher eventPublisher;  // ← 이 줄 주석 해제

    // ...
}
```

#### 1-2. import 문 추가

```java
import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.event.CurationViewedEvent;
import org.springframework.context.ApplicationEventPublisher;
```

#### 1-3. 상세 조회 메서드 수정 (33-49번째 줄 주석 해제 및 수정)

**현재 TODO 주석으로 남겨진 코드를 활성화하고 메서드명 변경:**

```java
@Transactional  // ← readOnly = true 제거!
public Popup getPopupDetail(String popupId, Long userId) {  // ← userId 파라미터 추가
    Popup popup = popupRepository.findByPopupId(popupId)
        .orElseThrow(() -> new IllegalArgumentException("Popup not found: " + popupId));

    popup.incrementViewCount();  // 조회수 증가

    // 비동기로 조회 이력 기록 (메인 스레드 블로킹 방지)
    eventPublisher.publishEvent(new CurationViewedEvent(
            popup.getId(),
            CurationType.POPUP,  // ← EXHIBITION이 아니라 POPUP!
            userId
    ));

    return popup;
}
```

---

### Step 2: PopupFacade 수정

**📁 파일 위치**: `apps/ticketing-api/src/main/java/com/example/ticketing/curation/facade/PopupFacade.java`

#### 기존 코드에서 userId 추가:

```java
public PopupDetailResponse getPopupDetail(String popupId, Long userId) {  // ← userId 추가
    Popup popup = popupService.getPopupDetail(popupId, userId);  // ← userId 전달

    // 나머지 로직 (좋아요, 리뷰 등)은 그대로 유지
    // ...

    return PopupDetailResponse.from(popup, ...);
}
```

---

### Step 3: PopupController 수정

**📁 파일 위치**: `apps/ticketing-api/src/main/java/com/example/ticketing/curation/controller/PopupController.java`

#### @CurrentUser로 userId 전달:

```java
import com.example.ticketing.common.security.CurrentUser;
import com.example.ticketing.user.domain.User;

// ...

@GetMapping("/{popupId}")
public ApiResponse<PopupDetailResponse> getPopupDetail(
        @PathVariable String popupId,
        @CurrentUser(required = false) User user  // ← required = false로 비로그인 허용
) {
    Long userId = user != null ? user.getId() : null;
    PopupDetailResponse response = popupFacade.getPopupDetail(popupId, userId);
    return ApiResponse.success(response);
}
```

---

## 📚 참고: 전시 상세 조회 구현 (완전 동일한 패턴)

**ExhibitionService.java:62-79** 참고

```java
@Transactional
public ExhibitionDetailResponse getExhibition(Long exhibitionId, Long userId) {
    Exhibition exhibition = exhibitionRepository.findByIdAndNotDeleted(exhibitionId)
        .orElseThrow(() -> new CustomException(ErrorCode.EXHIBITION_NOT_FOUND));

    exhibition.incrementViewCount();

    eventPublisher.publishEvent(new CurationViewedEvent(
            exhibitionId,
            CurationType.EXHIBITION,  // ← 팝업은 POPUP으로!
            userId
    ));

    boolean isLiked = userId != null &&
        exhibitionLikeRepository.existsByUserIdAndExhibitionId(userId, exhibitionId);

    return ExhibitionDetailResponse.from(exhibition, isLiked);
}
```

---

## ⚠️ 주의사항

- ✅ `@Transactional(readOnly = true)` → `@Transactional`로 변경 필수!
  - 이유: `incrementViewCount()`가 DB를 변경하기 때문
- ✅ `userId`는 `null` 가능 (비로그인 사용자도 조회 이력 기록)
- ✅ 이벤트만 발행하면 나머지는 자동 처리 (비동기 리스너가 알아서 저장)
- ✅ 이력 저장 실패해도 메인 플로우에 영향 없음 (에러 로깅만 됨)

---

## 🧪 동작 확인 방법

### 1. API 호출
```bash
GET /api/popups/{popupId}
```

### 2. 데이터베이스 확인
```sql
-- 최근 팝업 조회 이력 확인
SELECT
    curation_id,
    curation_type,
    user_id,
    viewed_at
FROM curation_view_history
WHERE curation_type = 'POPUP'
ORDER BY viewed_at DESC
LIMIT 10;
```

### 3. 조회수 증가 확인
```sql
-- 특정 팝업의 조회수 확인
SELECT id, title, view_count
FROM curation
WHERE type = 'POPUP' AND id = {popupId};
```

---

## 💡 참고 파일 위치

| 파일 | 경로 | 용도 |
|------|------|------|
| `ExhibitionService.java` | `curation/service/` | 전시 구현 예시 (동일 패턴) |
| `CurationViewedEvent.java` | `curation/event/` | 이벤트 클래스 |
| `CurationViewHistoryListener.java` | `curation/event/` | 비동기 리스너 |
| `ExhibitionViewHistoryTest.java` | `test/.../curation/service/` | 테스트 예시 |

---

## ❓ 질문이 있으면

1. **전시 구현 코드 참고**: `ExhibitionService.java:62-79`
2. **테스트 참고**: `ExhibitionViewHistoryTest.java`
3. **이벤트 처리 확인**: `CurationViewHistoryListener.java:27-46`