-- =========================================================
-- 1. Department
-- =========================================================
INSERT INTO department (dept_code, dept_name, college_name, office_location, office_phone)
VALUES ('CS', '컴퓨터공학과', '공과대학', 'E101', '02-111-1111');

INSERT INTO department (dept_code, dept_name, college_name, office_location, office_phone)
VALUES ('AI', '인공지능학과', '공과대학', 'E201', '02-222-2222');

-- =========================================================
-- 2. Professor  (rrn 또는 passport_no 필수)
-- =========================================================
INSERT INTO professor (
  professor_id, name_kr, name_en, rrn, passport_no, dept_code,
  position, office_location, office_phone, email, hire_date
) VALUES (
  'PROF001','김교수','Kim','9001011234567',NULL,'CS',
  '정교수','E101','02-111-1111','kim@uni.ac.kr',DATE '2020-03-01'
);

INSERT INTO professor (
  professor_id, name_kr, name_en, rrn, passport_no, dept_code,
  position, office_location, office_phone, email, hire_date
) VALUES (
  'PROF002','이교수','Lee','8801011234567',NULL,'AI',
  '부교수','E201','02-222-2222','lee@uni.ac.kr',DATE '2019-03-01'
);

-- =========================================================
-- 3. Student (admission_date NOT NULL, rrn/passport_no 필수)
--    - 20250001 ~ 20251200 생성 (부하/동시성 테스트용)
-- =========================================================
BEGIN
  FOR i IN 1..1200 LOOP
    INSERT INTO student (
      student_id, name_kr, name_en,
      rrn, passport_no, nationality,
      dept_code, status, admission_date,
      address, phone, email
    ) VALUES (
      '2025' || LPAD(i,4,'0'),
      '테스트학생' || i,
      'Student' || i,
      NULL, 'P' || LPAD(i,9,'0'), 'KOR',
      CASE WHEN MOD(i,2)=0 THEN 'CS' ELSE 'AI' END,
      'ENROLLED',
      DATE '2025-03-01',
      '서울 어딘가 ' || i,
      '010-' || LPAD(MOD(i,10000),4,'0') || '-' || LPAD(MOD(i,10000),4,'0'),
      's' || i || '@test.ac.kr'
    );
  END LOOP;
END;
/
  
-- 시나리오 전용 TEST000xx (테스트 코드에 직접 등장)
INSERT INTO student (
  student_id, name_kr, name_en, rrn, passport_no, nationality,
  dept_code, status, admission_date, address, phone, email
) VALUES (
  'TEST00001','테스트A','Test A', NULL,'TP000000001','KOR',
  'CS','ENROLLED',DATE '2024-03-01','서울','010-0000-0001','test00001@test.ac.kr'
);

INSERT INTO student (
  student_id, name_kr, name_en, rrn, passport_no, nationality,
  dept_code, status, admission_date, address, phone, email
) VALUES (
  'TEST00003','락A','Lock A', NULL,'TP000000003','KOR',
  'CS','ENROLLED',DATE '2024-03-01','서울','010-0000-0003','test00003@test.ac.kr'
);

INSERT INTO student (
  student_id, name_kr, name_en, rrn, passport_no, nationality,
  dept_code, status, admission_date, address, phone, email
) VALUES (
  'TEST00004','락B','Lock B', NULL,'TP000000004','KOR',
  'CS','ENROLLED',DATE '2024-03-01','서울','010-0000-0004','test00004@test.ac.kr'
);

INSERT INTO student (
  student_id, name_kr, name_en, rrn, passport_no, nationality,
  dept_code, status, admission_date, address, phone, email
) VALUES (
  'TEST00010','팬텀리드','Phantom', NULL,'TP000000010','KOR',
  'AI','ENROLLED',DATE '2024-03-01','서울','010-0000-0010','test00010@test.ac.kr'
);

INSERT INTO student (
  student_id, name_kr, name_en, rrn, passport_no, nationality,
  dept_code, status, admission_date, address, phone, email
) VALUES (
  'TEST00020','학점경계','CreditEdge', NULL,'TP000000020','KOR',
  'CS','ENROLLED',DATE '2023-03-01','서울','010-0000-0020','test00020@test.ac.kr'
);

INSERT INTO student (student_id,name_kr,name_en,rrn,passport_no,nationality,dept_code,status,admission_date,address,phone,email)
VALUES ('TEST00021','취소1','Cancel1',NULL,'TP000000021','KOR','CS','ENROLLED',DATE '2024-03-01','서울','010-0000-0021','test00021@test.ac.kr');
INSERT INTO student (student_id,name_kr,name_en,rrn,passport_no,nationality,dept_code,status,admission_date,address,phone,email)
VALUES ('TEST00022','취소2','Cancel2',NULL,'TP000000022','KOR','CS','ENROLLED',DATE '2024-03-01','서울','010-0000-0022','test00022@test.ac.kr');
INSERT INTO student (student_id,name_kr,name_en,rrn,passport_no,nationality,dept_code,status,admission_date,address,phone,email)
VALUES ('TEST00023','신규','New',NULL,'TP000000023','KOR','CS','ENROLLED',DATE '2024-03-01','서울','010-0000-0023','test00023@test.ac.kr');

INSERT INTO student (student_id,name_kr,name_en,rrn,passport_no,nationality,dept_code,status,admission_date,address,phone,email)
VALUES ('TEST00030','트리거1','Trig1',NULL,'TP000000030','KOR','CS','ENROLLED',DATE '2024-03-01','서울','010-0000-0030','test00030@test.ac.kr');
INSERT INTO student (student_id,name_kr,name_en,rrn,passport_no,nationality,dept_code,status,admission_date,address,phone,email)
VALUES ('TEST00031','트리거2','Trig2',NULL,'TP000000031','KOR','CS','ENROLLED',DATE '2024-03-01','서울','010-0000-0031','test00031@test.ac.kr');
INSERT INTO student (student_id,name_kr,name_en,rrn,passport_no,nationality,dept_code,status,admission_date,address,phone,email)
VALUES ('TEST00032','트리거3','Trig3',NULL,'TP000000032','KOR','CS','ENROLLED',DATE '2024-03-01','서울','010-0000-0032','test00032@test.ac.kr');
INSERT INTO student (student_id,name_kr,name_en,rrn,passport_no,nationality,dept_code,status,admission_date,address,phone,email)
VALUES ('TEST00033','트리거4','Trig4',NULL,'TP000000033','KOR','CS','ENROLLED',DATE '2024-03-01','서울','010-0000-0033','test00033@test.ac.kr');
INSERT INTO student (student_id,name_kr,name_en,rrn,passport_no,nationality,dept_code,status,admission_date,address,phone,email)
VALUES ('TEST00034','트리거5','Trig5',NULL,'TP000000034','KOR','CS','ENROLLED',DATE '2024-03-01','서울','010-0000-0034','test00034@test.ac.kr');
INSERT INTO student (student_id,name_kr,name_en,rrn,passport_no,nationality,dept_code,status,admission_date,address,phone,email)
VALUES ('TEST00035','트리거6','Trig6',NULL,'TP000000035','KOR','CS','ENROLLED',DATE '2024-03-01','서울','010-0000-0035','test00035@test.ac.kr');

-- =========================================================
-- 4. Course
-- =========================================================
INSERT INTO course (course_code, course_name_kr, course_name_en, credit, course_type, recommended_year, is_deleted)
VALUES ('CS101','자료구조','Data Structures',3.0,'전공필수',1,'N');
INSERT INTO course (course_code, course_name_kr, course_name_en, credit, course_type, recommended_year, is_deleted)
VALUES ('CS102','운영체제','OS',3.0,'전공필수',2,'N');
INSERT INTO course (course_code, course_name_kr, course_name_en, credit, course_type, recommended_year, is_deleted)
VALUES ('CS103','네트워크','Network',3.0,'전공필수',2,'N');

-- 학점 경계 테스트용
INSERT INTO course (course_code, course_name_kr, course_name_en, credit, course_type, recommended_year, is_deleted)
VALUES ('CR_HALF','반학점','Half Credit',0.5,'교양',NULL,'N');
INSERT INTO course (course_code, course_name_kr, course_name_en, credit, course_type, recommended_year, is_deleted)
VALUES ('CR_ONE','한학점','One Credit',1.0,'교양',NULL,'N');

-- 17.5학점 맞추기용(2.5학점 과목 추가)
INSERT INTO course (course_code, course_name_kr, course_name_en, credit, course_type, recommended_year, is_deleted)
VALUES ('CR_25','2.5학점','2.5 Credit',2.5,'교양',NULL,'N');

-- =========================================================
-- 5. OpenCourse (테스트 코드에 하드코딩된 open_course_id 맞춤)
--   필수: year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled
-- =========================================================
-- 99999: 동시성/중복신청/부하 테스트 공용(기본은 "정상 개설"로 둔다)
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (99999, 2025, '2학기', 'CS101', 'A', 'PROF001', 'R-999', 30, 0, 'N');

-- DirtyRead/PhantomRead에서 많이 쓰던 849
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (849, 2025, '2학기', 'CS102', 'A', 'PROF001', 'R-849', 100, 0, 'N');

-- LockTimeoutTest에서 쓰는 844 (정원 1)
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (844, 2025, '2학기', 'CS103', 'A', 'PROF001', 'R-844', 1, 0, 'N');

-- DeadlockDetectionTest에서 쓰는 842/843
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (842, 2025, '2학기', 'CS101', 'B', 'PROF001', 'R-842', 100, 0, 'N');
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (843, 2025, '2학기', 'CS102', 'B', 'PROF001', 'R-843', 100, 0, 'N');

-- TriggerConsistencyTest에서 COURSE_ID=2 (정원 5)
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (2, 2025, '2학기', 'CS101', 'T', 'PROF001', 'R-2', 5, 0, 'N');

-- RetakePreventionTest에서 COURSE_PASSED=882, COURSE_FAILED=883
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (882, 2024, '2학기', 'CS101', 'R1', 'PROF001', 'R-882', 50, 0, 'N');
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (883, 2024, '2학기', 'CS102', 'R1', 'PROF001', 'R-883', 50, 0, 'N');

-- CreditBoundaryTest에서 COURSE_HALF=878, COURSE_ONE=879 (둘 다 2025-2학기여야 TotalCredits 계산에 들어감)
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (878, 2025, '2학기', 'CR_HALF', 'C1', 'PROF002', 'R-878', 10, 0, 'N');
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (879, 2025, '2학기', 'CR_ONE', 'C1', 'PROF002', 'R-879', 10, 0, 'N');

-- 17.5학점 맞추기 위한 2025-2학기 추가 개설강좌(3학점*4 + 2.5학점 = 14.5? -> 총 17.5 만들기)
-- 3학점 5개(15) + 2.5(17.5) 구성이 필요하니 CS101/CS102/CS103을 section만 바꿔 여러 개설 생성
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (851, 2025, '2학기', 'CS101', 'C', 'PROF001', 'R-851', 60, 0, 'N');
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (852, 2025, '2학기', 'CS102', 'C', 'PROF001', 'R-852', 60, 0, 'N');
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (853, 2025, '2학기', 'CS103', 'C', 'PROF001', 'R-853', 60, 0, 'N');
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (854, 2025, '2학기', 'CS101', 'D', 'PROF001', 'R-854', 60, 0, 'N');
INSERT INTO open_course (open_course_id, year, term, course_code, section, professor_id, room, capacity, enrolled_count, is_canceled)
VALUES (855, 2025, '2학기', 'CR_25', 'E', 'PROF002', 'R-855', 60, 0, 'N');

-- =========================================================
-- 6. Enrollment + Grade
--  (A) RetakePreventionTest용: TEST00001이 882(C) / 883(F) 이수 기록 만들기
-- =========================================================
INSERT INTO enrollment (
  enrollment_id, student_id, open_course_id, requested_at, status, is_retake, created_by
) VALUES (
  seq_enrollment.NEXTVAL, 'TEST00001', 882, SYSTIMESTAMP, 'APPROVED', 'N', 'TEST00001'
);

INSERT INTO enrollment (
  enrollment_id, student_id, open_course_id, requested_at, status, is_retake, created_by
) VALUES (
  seq_enrollment.NEXTVAL, 'TEST00001', 883, SYSTIMESTAMP, 'APPROVED', 'N', 'TEST00001'
);

-- 위 enrollment_id를 grade에 연결 (서브쿼리로 1건 보장)
INSERT INTO grade (
  grade_id, enrollment_id, midterm_score, final_score, final_grade, grade_point,
  grade_confirmed, confirmed_at, confirmed_by
) VALUES (
  seq_grade.NEXTVAL,
  (SELECT enrollment_id FROM enrollment WHERE student_id='TEST00001' AND open_course_id=882),
  70, 75, 'C', 2.00,
  'Y', SYSTIMESTAMP, 'PROF001'
);

INSERT INTO grade (
  grade_id, enrollment_id, midterm_score, final_score, final_grade, grade_point,
  grade_confirmed, confirmed_at, confirmed_by
) VALUES (
  seq_grade.NEXTVAL,
  (SELECT enrollment_id FROM enrollment WHERE student_id='TEST00001' AND open_course_id=883),
  40, 30, 'F', 0.00,
  'Y', SYSTIMESTAMP, 'PROF001'
);

--  (B) CreditBoundaryTest용: TEST00020이 2025-2학기 총 17.5학점 상태 만들기
--    851(3) + 852(3) + 853(3) + 854(3) + 849(3) = 15
--    + 855(2.5) = 17.5
INSERT INTO enrollment (enrollment_id, student_id, open_course_id, requested_at, status, is_retake, created_by)
VALUES (seq_enrollment.NEXTVAL,'TEST00020',851,SYSTIMESTAMP,'APPROVED','N','TEST00020');
INSERT INTO enrollment (enrollment_id, student_id, open_course_id, requested_at, status, is_retake, created_by)
VALUES (seq_enrollment.NEXTVAL,'TEST00020',852,SYSTIMESTAMP,'APPROVED','N','TEST00020');
INSERT INTO enrollment (enrollment_id, student_id, open_course_id, requested_at, status, is_retake, created_by)
VALUES (seq_enrollment.NEXTVAL,'TEST00020',853,SYSTIMESTAMP,'APPROVED','N','TEST00020');
INSERT INTO enrollment (enrollment_id, student_id, open_course_id, requested_at, status, is_retake, created_by)
VALUES (seq_enrollment.NEXTVAL,'TEST00020',854,SYSTIMESTAMP,'APPROVED','N','TEST00020');
INSERT INTO enrollment (enrollment_id, student_id, open_course_id, requested_at, status, is_retake, created_by)
VALUES (seq_enrollment.NEXTVAL,'TEST00020',849,SYSTIMESTAMP,'APPROVED','N','TEST00020');
INSERT INTO enrollment (enrollment_id, student_id, open_course_id, requested_at, status, is_retake, created_by)
VALUES (seq_enrollment.NEXTVAL,'TEST00020',855,SYSTIMESTAMP,'APPROVED','N','TEST00020');

COMMIT;

-- open_course에 version 컬럼 있어야 함
DESC open_course;

ALTER TABLE open_course ADD version NUMBER DEFAULT 1;
UPDATE open_course SET version = 1;
COMMIT;