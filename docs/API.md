# Popup API 명세서

> Base URL: `http://localhost:8080/api`

## 목차
- [팝업 리스트 조회](#팝업-리스트-조회)
- [공통 응답 형식](#공통-응답-형식)
- [Enum 정의](#enum-정의)

---

## 팝업 리스트 조회

팝업 목록을 페이지네이션과 함께 조회합니다.

### Request

```
GET /popups
```

#### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `keyword` | string | N | - | 제목 검색 키워드 |
| `city` | string | N | - | 도시 필터 (예: 서울) |
| `status` | string | N | - | 상태 필터 (UPCOMING, ONGOING, ENDED) |
| `page` | int | N | 0 | 페이지 번호 (0부터 시작) |
| `size` | int | N | 10 | 페이지 크기 |
| `userId` | long | N | - | 로그인한 사용자 ID (좋아요 여부 확인용) |

#### Example

```bash
# 비로그인 사용자
curl "http://localhost:8080/api/popups?city=서울&page=0&size=10"

# 로그인 사용자
curl "http://localhost:8080/api/popups?city=서울&page=0&size=10&userId=1"
```

### Response

```json
{
  "result": "SUCCESS",
  "data": {
    "popups": [
      {
        "popupId": "550e8400-e29b-41d4-a716-446655440001",
        "title": "스누피 X 삼성 팝업스토어",
        "thumbnailImageUrl": "https://example.com/snoopy.jpg",
        "startDate": "2026-01-10",
        "endDate": "2026-01-25",
        "status": "ONGOING",
        "location": {
          "city": "서울",
          "district": "성동구",
          "placeName": "성수동"
        },
        "category": ["캐릭터", "브랜드"],
        "isFree": false,
        "reservationRequired": true,
        "tags": ["스누피", "삼성", "콜라보"],
        "likeCount": 128,
        "viewCount": 1542,
        "isLiked": true
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 1,
      "totalPages": 1
    }
  }
}
```

#### Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `popups` | array | 팝업 목록 |
| `popups[].popupId` | string | 팝업 고유 ID (UUID) |
| `popups[].title` | string | 팝업 제목 |
| `popups[].thumbnailImageUrl` | string | 썸네일 이미지 URL |
| `popups[].startDate` | string | 시작일 (YYYY-MM-DD) |
| `popups[].endDate` | string | 종료일 (YYYY-MM-DD) |
| `popups[].status` | string | 팝업 상태 (자동 계산) |
| `popups[].location` | object | 위치 정보 |
| `popups[].location.city` | string | 도시 |
| `popups[].location.district` | string | 구/군 |
| `popups[].location.placeName` | string | 장소명 |
| `popups[].category` | array | 카테고리 목록 |
| `popups[].isFree` | boolean | 무료 여부 |
| `popups[].reservationRequired` | boolean | 예약 필요 여부 |
| `popups[].tags` | array | 태그 목록 |
| `popups[].likeCount` | int | 좋아요 수 |
| `popups[].viewCount` | int | 조회수 |
| `popups[].isLiked` | boolean | 현재 사용자의 좋아요 여부 (userId 미전달 시 항상 false) |
| `pagination` | object | 페이지 정보 |
| `pagination.page` | int | 현재 페이지 번호 |
| `pagination.size` | int | 페이지 크기 |
| `pagination.totalElements` | long | 전체 항목 수 |
| `pagination.totalPages` | int | 전체 페이지 수 |

---

## 공통 응답 형식

모든 API는 아래 형식으로 응답합니다.

### 성공 응답

```json
{
  "result": "SUCCESS",
  "data": { ... }
}
```

### 에러 응답

```json
{
  "result": "ERROR",
  "data": {
    "code": "ERROR_CODE",
    "message": "에러 메시지"
  }
}
```

---

## Enum 정의

### PopupStatus

팝업 상태는 `startDate`와 `endDate`를 기준으로 서버에서 자동 계산됩니다.

| 값 | 설명 | 조건 |
|----|------|------|
| `UPCOMING` | 예정 | 오늘 < startDate |
| `ONGOING` | 진행중 | startDate <= 오늘 <= endDate |
| `ENDED` | 종료 | 오늘 > endDate |

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|----------|
| 2026-01-19 | v1.1 | 좋아요/조회수 기능 추가 - likeCount, viewCount, isLiked 필드 추가, userId 파라미터 추가 |
| 2026-01-15 | v1.0 | 최초 작성 - 리스트 API |