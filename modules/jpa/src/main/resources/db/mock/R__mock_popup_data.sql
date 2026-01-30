-- Repeatable migration: 로컬 테스트용 mock 데이터
-- R__ prefix = Flyway repeatable migration (내용 변경 시 자동 재실행)

-- 기존 mock 데이터 삭제
DELETE FROM popup WHERE popup_id LIKE 'mock-%';
DELETE FROM exhibition WHERE id IN (SELECT id FROM curation WHERE title LIKE 'mock-%');
DELETE FROM curation WHERE title LIKE 'mock-%';

-- 팝업 카테고리: 패션 / 뷰티 / F&B / 캐릭터 / 테크 / 라이프스타일 / 가구·인테리어
-- 1) curation 테이블에 부모 레코드 삽입
INSERT INTO curation (type, title, thumbnail, region, place, start_date, end_date, tags)
VALUES
('POPUP', '나이키 에어맥스 팝업', 'https://picsum.photos/seed/nike/400/300', '서울 강남구', '신사동', '2026-01-15', '2026-02-15', '["나이키", "스니커즈"]'),
('POPUP', '무신사 뷰티 페스타', 'https://picsum.photos/seed/musinsa/400/300', '서울 성동구', '성수동', '2026-02-01', '2026-02-14', '["무신사", "뷰티"]'),
('POPUP', '맛있는 녀석들 푸드 팝업', 'https://picsum.photos/seed/food/400/300', '서울 마포구', '홍대', '2026-02-10', '2026-02-20', '["맛집", "푸드"]'),
('POPUP', '스누피 X 삼성 팝업스토어', 'https://picsum.photos/seed/snoopy/400/300', '서울 성동구', '성수동', '2026-01-10', '2026-02-28', '["스누피", "삼성"]'),
('POPUP', '삼성 갤럭시 언팩 체험존', 'https://picsum.photos/seed/galaxy/400/300', '서울 강남구', '삼성동', '2026-01-20', '2026-02-20', '["삼성", "갤럭시"]'),
('POPUP', '카카오프렌즈 플래그십', 'https://picsum.photos/seed/kakao/400/300', '서울 강남구', '강남역', '2026-01-01', '2026-06-30', '["카카오", "라이언"]'),
('POPUP', '이케아 팝업 라운지', 'https://picsum.photos/seed/ikea/400/300', '서울 강서구', '마곡동', '2026-02-01', '2026-03-15', '["이케아", "인테리어"]');

-- 2) popup 테이블에 자식 레코드 삽입 (JOINED 상속)
INSERT INTO popup (id, popup_id, city, district, place_name, category, is_free, reservation_required)
SELECT id, 'mock-001', '서울', '강남구', '신사동', '["패션"]', true, false FROM curation WHERE title = '나이키 에어맥스 팝업' AND type = 'POPUP';
INSERT INTO popup (id, popup_id, city, district, place_name, category, is_free, reservation_required)
SELECT id, 'mock-002', '서울', '성동구', '성수동', '["뷰티"]', false, false FROM curation WHERE title = '무신사 뷰티 페스타' AND type = 'POPUP';
INSERT INTO popup (id, popup_id, city, district, place_name, category, is_free, reservation_required)
SELECT id, 'mock-003', '서울', '마포구', '홍대', '["F&B"]', true, false FROM curation WHERE title = '맛있는 녀석들 푸드 팝업' AND type = 'POPUP';
INSERT INTO popup (id, popup_id, city, district, place_name, category, is_free, reservation_required)
SELECT id, 'mock-004', '서울', '성동구', '성수동', '["캐릭터"]', false, true FROM curation WHERE title = '스누피 X 삼성 팝업스토어' AND type = 'POPUP';
INSERT INTO popup (id, popup_id, city, district, place_name, category, is_free, reservation_required)
SELECT id, 'mock-005', '서울', '강남구', '삼성동', '["테크"]', true, false FROM curation WHERE title = '삼성 갤럭시 언팩 체험존' AND type = 'POPUP';
INSERT INTO popup (id, popup_id, city, district, place_name, category, is_free, reservation_required)
SELECT id, 'mock-006', '서울', '강남구', '강남역', '["라이프스타일"]', true, false FROM curation WHERE title = '카카오프렌즈 플래그십' AND type = 'POPUP';
INSERT INTO popup (id, popup_id, city, district, place_name, category, is_free, reservation_required)
SELECT id, 'mock-007', '서울', '강서구', '마곡동', '["가구·인테리어"]', true, false FROM curation WHERE title = '이케아 팝업 라운지' AND type = 'POPUP';


-- 전시 카테고리: 현대미술 / 사진 / 디자인 / 일러스트 / 회화 / 조각 / 설치미술
INSERT INTO curation (type, title, sub_title, thumbnail, region, category, place, start_date, end_date, tags, url, address, reservation_status)
VALUES
('EXHIBITION', 'mock-데미안 허스트: 체리블라썸', '영국 현대미술 거장의 신작', 'https://picsum.photos/seed/hirst/400/300', '서울', '["현대미술"]', '리움미술관', '2026-01-15', '2026-04-15', '["데미안허스트", "현대미술"]', NULL, '서울 용산구 이태원로55길 60-16', 'PRE_ORDER'),
('EXHIBITION', 'mock-앤디워홀 회고전', '팝아트의 선구자 앤디워홀', 'https://picsum.photos/seed/warhol/400/300', '서울', '["사진"]', '서울시립미술관', '2026-01-20', '2026-03-20', '["앤디워홀", "팝아트"]', NULL, '서울 중구 덕수궁길 61', 'ALL'),
('EXHIBITION', 'mock-디터 람스: Less but Better', '산업 디자인의 거장', 'https://picsum.photos/seed/rams/400/300', '서울', '["디자인"]', '동대문DDP', '2026-02-01', '2026-05-31', '["디터람스", "디자인"]', NULL, '서울 중구 을지로 281', 'PRE_ORDER'),
('EXHIBITION', 'mock-서울 일러스트레이션 페어', '국내외 일러스트 작가 200팀', 'https://picsum.photos/seed/illust/400/300', '서울', '["일러스트"]', 'COEX', '2026-02-01', '2026-02-10', '["일러스트", "아트페어"]', NULL, '서울 강남구 영동대로 513', 'ON_SITE'),
('EXHIBITION', 'mock-모네: 빛의 울림', '인상주의 거장의 대표작 전시', 'https://picsum.photos/seed/monet/400/300', '서울', '["회화"]', '예술의전당', '2026-01-15', '2026-04-15', '["모네", "인상주의"]', NULL, '서울 서초구 남부순환로 2406', 'PRE_ORDER'),
('EXHIBITION', 'mock-로댕과 카미유', '근대 조각의 거장', 'https://picsum.photos/seed/rodin/400/300', '서울', '["조각"]', '국립현대미술관', '2026-03-01', '2026-06-01', '["로댕", "조각"]', NULL, '서울 종로구 삼청로 30', 'PRE_ORDER'),
('EXHIBITION', 'mock-올라퍼 엘리아슨: In Real Life', '빛과 공간의 설치미술', 'https://picsum.photos/seed/olafur/400/300', '서울', '["설치미술"]', '아모레퍼시픽미술관', '2026-02-10', '2026-05-10', '["올라퍼엘리아슨", "설치미술"]', NULL, '서울 용산구 한강대로 100', 'PRE_ORDER');

-- exhibition 테이블에 JOINED 상속 레코드 추가
INSERT INTO exhibition (id) SELECT id FROM curation WHERE title LIKE 'mock-%' AND type = 'EXHIBITION';