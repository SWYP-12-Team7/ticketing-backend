# 전시 조회 이력 테스트 가이드

## ✅ 구현 완료 항목

1. **CurationViewHistory 엔티티** - Curation 타입 통합 조회 이력
2. **CurationViewedEvent** - 조회 이벤트 클래스
3. **CurationViewHistoryListener** - @Async 비동기 리스너
4. **AsyncConfig** - 스레드 풀 설정 (@EnableAsync)
5. **ExhibitionService** - 상세 조회 시 이벤트 발행
6. **V22 Migration** - curation_view_history 테이블 생성

## 🧪 자동 테스트 실행

### 1. 통합 테스트 실행
```bash
./gradlew test --tests ExhibitionViewHistoryTest
```

### 테스트 케이스
- ✅ 전시 상세 조회 시 조회수 증가
- ✅ 비동기로 조회 이력 저장
- ✅ 비로그인 사용자도 이력 저장 (userId = null)
- ✅ 동일 전시 여러 번 조회 시 이력 누적
- ✅ 이벤트 직접 발행 테스트

## 🔍 수동 테스트 방법

### 1. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 2. 전시 데이터 생성 (테스트용)
```sql
-- exhibition 테이블에 임시 데이터 삽입
INSERT INTO curation (type, title, thumbnail, region, place, start_date, end_date, url, address, description, view_count, like_count, created_at, updated_at)
VALUES ('EXHIBITION', '테스트 전시회', 'https://example.com/image.jpg', '서울 강남구', '테스트 갤러리',
        '2025-01-01', '2025-12-31', 'https://example.com', '서울시 강남구 테스트로 123',
        '테스트 전시 설명', 0, 0, NOW(), NOW());

SET @curation_id = LAST_INSERT_ID();

INSERT INTO exhibition (id, charge, contact_point)
VALUES (@curation_id, '무료', '02-1234-5678 / 운영시간: 페이지 참고');

-- ID 확인
SELECT id FROM curation WHERE title = '테스트 전시회';
```

### 3. API 호출
```bash
# 로그인 사용자로 조회
curl -X GET "http://localhost:8080/exhibitions/{exhibitionId}" \
  -H "Authorization: Bearer {access_token}"

# 비로그인 사용자로 조회
curl -X GET "http://localhost:8080/exhibitions/{exhibitionId}"
```

### 4. 결과 확인

#### 조회수 증가 확인
```sql
SELECT id, title, view_count
FROM curation
WHERE id = {exhibitionId};
-- view_count가 1 증가했는지 확인
```

#### 조회 이력 확인
```sql
SELECT *
FROM curation_view_history
WHERE curation_id = {exhibitionId}
ORDER BY viewed_at DESC;

-- 확인 포인트:
-- 1. curation_id가 올바른지
-- 2. curation_type이 'EXHIBITION'인지
-- 3. user_id가 올바른지 (비로그인은 NULL)
-- 4. viewed_at에 시간이 기록되었는지
```

#### 비동기 처리 확인
```sql
-- 여러 번 조회 후 이력 개수 확인
SELECT COUNT(*) as total_views
FROM curation_view_history
WHERE curation_id = {exhibitionId};

-- 시간별 조회 통계
SELECT DATE(viewed_at) as view_date, COUNT(*) as views
FROM curation_view_history
WHERE curation_id = {exhibitionId}
GROUP BY DATE(viewed_at)
ORDER BY view_date DESC;
```

## 📊 랭킹 집계 쿼리 예시

### 최근 7일 인기 전시
```sql
SELECT c.id, c.title, COUNT(h.id) as view_count
FROM curation c
LEFT JOIN curation_view_history h ON c.id = h.curation_id
WHERE c.type = 'EXHIBITION'
  AND h.viewed_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY c.id, c.title
ORDER BY view_count DESC
LIMIT 10;
```

### 오늘의 인기 전시
```sql
SELECT c.id, c.title, COUNT(h.id) as today_views
FROM curation c
LEFT JOIN curation_view_history h ON c.id = h.curation_id
WHERE c.type = 'EXHIBITION'
  AND DATE(h.viewed_at) = CURDATE()
GROUP BY c.id, c.title
ORDER BY today_views DESC
LIMIT 10;
```

### 사용자별 조회 이력
```sql
SELECT c.title, h.viewed_at
FROM curation_view_history h
JOIN curation c ON h.curation_id = c.id
WHERE h.user_id = {userId}
  AND c.type = 'EXHIBITION'
ORDER BY h.viewed_at DESC
LIMIT 20;
```

## 🔍 로그 확인

### 애플리케이션 로그
```bash
# 조회 이력 저장 완료 로그
grep "조회 이력 저장 완료" logs/application.log

# 조회 이력 저장 실패 로그 (있으면 안됨)
grep "조회 이력 저장 실패" logs/application.log

# 비동기 실행 로그
grep "async-" logs/application.log
```

## 🐛 트러블슈팅

### 이력이 저장되지 않는 경우
1. **비동기 설정 확인**: AsyncConfig가 @EnableAsync로 활성화되어 있는지
2. **이벤트 리스너 확인**: CurationViewHistoryListener가 @Component로 등록되어 있는지
3. **트랜잭션 확인**: 별도 트랜잭션(REQUIRES_NEW)으로 실행되는지
4. **로그 확인**: 에러 로그가 있는지 확인

### 비동기 작업이 느린 경우
```java
// AsyncConfig의 스레드 풀 설정 조정
executor.setCorePoolSize(10);  // 기본 5 → 10
executor.setMaxPoolSize(20);   // 기본 10 → 20
```

## ✅ 테스트 체크리스트

- [ ] 전시 상세 조회 시 조회수 증가
- [ ] 조회 이력이 curation_view_history 테이블에 저장됨
- [ ] 비동기로 처리되어 API 응답 시간에 영향 없음
- [ ] 로그인/비로그인 사용자 모두 정상 작동
- [ ] 동일 전시 여러 번 조회 시 이력 누적
- [ ] 인덱스를 활용한 랭킹 쿼리 정상 작동