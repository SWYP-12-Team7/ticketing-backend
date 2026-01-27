# Gemini 데이터 수집 가이드

## 개요

이 문서는 Gemini API를 통해 팝업스토어 데이터를 수집할 때 발생할 수 있는 문제와 해결 방법을 설명합니다.

## 문제: 가상 데이터 생성

### 증상

Gemini API를 통해 팝업 데이터를 수집했을 때, 실제 존재하지 않는 가상의 데이터가 반환됨:

```json
{
  "title": "미정: 식물 관련 팝업",
  "placeName": "성수동 식물원",
  ...
}
```

### 원인

Spring AI에서 Gemini의 **Google Search grounding 기능이 기본적으로 비활성화**되어 있음.

- grounding이 비활성화된 경우: 모델이 학습 데이터만 사용하여 응답 생성 → 가상 데이터
- grounding이 활성화된 경우: 실시간 Google 검색을 통해 최신 정보 반영 → 실제 데이터

### 해결 방법

#### 1. application.yml 설정 추가

```yaml
spring:
  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY}
        chat:
          options:
            model: gemini-2.0-flash
            temperature: 1.0              # grounding 사용 시 권장값
            google-search-retrieval: true # Google Search grounding 활성화
```

#### 2. 프롬프트 개선

실제 정보를 검색하도록 명시적으로 지시:

```
서울 성수동에서 2026년 1월에 진행 중이거나 예정된 실제 팝업스토어 정보를 검색해서 알려줘.

중요: 가상의 데이터가 아닌 실제로 존재하는 팝업스토어 정보만 알려줘.
브랜드명, 장소명, 날짜 등 정확한 정보를 포함해줘.
```

## 데이터 품질 관리

### 신뢰도(confidence) 필터링

Gemini 응답에는 각 데이터의 신뢰도(0.0 ~ 1.0)가 포함됨. 품질 보장을 위해 **신뢰도 0.8 이상**인 데이터만 DB에 적재:

```java
// PopupDataProcessor.java
private static final double MINIMUM_CONFIDENCE = 0.8;

if (data.confidence() != null && data.confidence() < MINIMUM_CONFIDENCE) {
    return ProcessResult.skipped("신뢰도 미달: " + data.title());
}
```

### 신뢰도 기준 및 검토 상태

| 신뢰도 | 의미 | 검토 상태 | 처리 |
|--------|------|-----------|------|
| 0.8 ~ 1.0 | 높은 신뢰도 | `APPROVED` | 바로 서비스 노출 |
| 0.5 ~ 0.8 | 중간 신뢰도 | `PENDING_REVIEW` | 관리자 검토 후 노출 |
| 0.0 ~ 0.5 | 낮은 신뢰도 | - | 스킵 (저장 안함) |

## 검토 시스템

### 검토 상태 (ReviewStatus)

- `APPROVED`: 승인됨 - 서비스에 노출
- `PENDING_REVIEW`: 검토 대기 - 관리자 승인 필요
- `REJECTED`: 거절됨 - 서비스에 노출 안함

### 관리자 검토 API

#### 검토 대기 목록 조회
```
GET /api/admin/review/pending
```

응답 예시:
```json
{
  "result": "SUCCESS",
  "data": [
    {
      "popupId": "abc-123",
      "title": "발로란트 팝업",
      "placeName": "성수동 일대",
      "startDate": "2026-01-15",
      "endDate": "2026-01-31",
      "reviewStatus": "PENDING_REVIEW"
    }
  ]
}
```

#### 승인
```
POST /api/admin/review/{popupId}/approve
```

#### 거절
```
POST /api/admin/review/{popupId}/reject
```

## 참고 자료

- [Spring AI Google GenAI Chat](https://docs.spring.io/spring-ai/reference/api/chat/google-genai-chat.html)
- [Gemini API - Grounding with Google Search](https://ai.google.dev/gemini-api/docs/google-search)
- [Spring AI GitHub Issue #2185](https://github.com/spring-projects/spring-ai/issues/2185) - Gemini 2.0 grounding 관련 이슈
