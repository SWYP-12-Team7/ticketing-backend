# 메인페이지 API 명세서

> Base URL: `http://localhost:8080/api`

## 목차
- [메인페이지 조회](#메인페이지-조회)
- [인기행사 조회](#인기행사-조회)
- [나의 취향 조회](#나의-취향-조회)

---

## 메인페이지 조회

메인페이지에 필요한 유저 맞춤/오픈예정/무료/오늘오픈 행사 목록을 조회합니다.

### Request

```
GET /main
```

#### Headers

| 헤더 | 필수 | 설명 |
|-----|------|------|
| `Authorization` | N | Bearer {accessToken} - 로그인 시 유저 맞춤 행사 제공 |

#### Example

```bash
# 비로그인
curl "http://localhost:8080/api/main"

# 로그인
curl "http://localhost:8080/api/main" \
  -H "Authorization: Bearer {accessToken}"
```

### Response

```json
{
  "result": "SUCCESS",
  "data": {
    "userCurations": [
      {
        "id": 1,
        "type": "POPUP",
        "title": "봄날엔 팝업",
        "subTitle": null,
        "thumbnail": "https://cdn.popga.co.kr/...",
        "region": "서울 용산구",
        "place": "아이파크몰 용산점",
        "startDate": "2026-02-06",
        "endDate": "2026-02-20",
        "category": ["F&B", "카페/디저트"],
        "dDay": 0
      }
    ],
    "upcomingCurations": [...],
    "freeCurations": [...],
    "todayOpenCurations": [...]
  }
}
```

#### Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `userCurations` | array | 유저 선호 지역+카테고리 기반 맞춤 행사 (비로그인 시 빈 배열) |
| `upcomingCurations` | array | D-7 이내 오픈 예정 행사 |
| `freeCurations` | array | 무료 행사 |
| `todayOpenCurations` | array | 오늘 오픈 행사 |

#### CurationSummary Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | long | 행사 ID |
| `type` | string | 행사 타입 (POPUP, EXHIBITION) |
| `title` | string | 제목 |
| `subTitle` | string | 부제목 |
| `thumbnail` | string | 썸네일 이미지 URL |
| `region` | string | 지역 (예: 서울 용산구) |
| `place` | string | 장소명 |
| `startDate` | string | 시작일 (YYYY-MM-DD) |
| `endDate` | string | 종료일 (YYYY-MM-DD) |
| `category` | array | 카테고리 목록 |
| `dDay` | long | D-Day (오늘 기준 시작일까지 남은 일수) |

---

## 인기행사 조회

24시간/이번주/이번달 기준 조회수가 많은 순으로 팝업/전시 각각 조회합니다.

### Request

```
GET /main/popular
```

#### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `limit` | int | N | 10 | 각 기간별 조회할 개수 |

#### Example

```bash
curl "http://localhost:8080/api/main/popular?limit=10"
```

### Response

```json
{
  "result": "SUCCESS",
  "data": {
    "popup": {
      "daily": [
        {
          "rank": 1,
          "id": 1,
          "title": "봄날엔 팝업",
          "thumbnail": "https://cdn.popga.co.kr/...",
          "address": "서울 용산구",
          "period": "2026-02-06 ~ 2026-02-20"
        },
        {
          "rank": 2,
          "id": 2,
          "title": "스누피 팝업스토어",
          "thumbnail": "https://example.com/...",
          "address": "서울 성동구",
          "period": "2026-02-01 ~ 2026-02-28"
        }
      ],
      "weekly": [...],
      "monthly": [...]
    },
    "exhibition": {
      "daily": [...],
      "weekly": [...],
      "monthly": [...]
    }
  }
}
```

#### Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `popup` | object | 팝업 인기행사 |
| `popup.daily` | array | 24시간 기준 조회수 순 |
| `popup.weekly` | array | 이번주 (월요일~) 기준 조회수 순 |
| `popup.monthly` | array | 이번달 기준 조회수 순 |
| `exhibition` | object | 전시 인기행사 |
| `exhibition.daily` | array | 24시간 기준 조회수 순 |
| `exhibition.weekly` | array | 이번주 기준 조회수 순 |
| `exhibition.monthly` | array | 이번달 기준 조회수 순 |

#### PopularItem Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `rank` | int | 순위 (1부터 시작) |
| `id` | long | 행사 ID |
| `title` | string | 제목 |
| `thumbnail` | string | 썸네일 이미지 URL |
| `address` | string | 주소 (동까지) |
| `period` | string | 기간 (시작일 ~ 마감일) |

---

## 나의 취향 조회

유저가 최근 찜한 행사 5개, 최근 열람 5개, 카테고리 기반 추천 2개를 조회합니다.

### Request

```
GET /users/me/taste
```

#### Headers

| 헤더 | 필수 | 설명 |
|-----|------|------|
| `Authorization` | Y | Bearer {accessToken} |

#### Example

```bash
curl "http://localhost:8080/api/users/me/taste" \
  -H "Authorization: Bearer {accessToken}"
```

### Response

```json
{
  "favorites": [
    {
      "id": 1,
      "type": "POPUP",
      "title": "봄날엔 팝업",
      "thumbnail": "https://...",
      "region": "서울 용산구",
      "startDate": "2026-02-06",
      "endDate": "2026-02-20"
    }
  ],
  "recentViews": [...],
  "recommendations": [...]
}
```

#### Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| `favorites` | array | 최근 찜한 행사 5개 |
| `recentViews` | array | 최근 열람한 행사 5개 |
| `recommendations` | array | 카테고리 기반 추천 행사 2개 |

---

## 변경 이력

| 날짜 | 버전 | 변경 내용 |
|------|------|----------|
| 2026-02-06 | v1.1 | 인기행사 API 추가 |
| 2026-02-05 | v1.0 | 최초 작성 - 메인페이지/나의취향 API |
