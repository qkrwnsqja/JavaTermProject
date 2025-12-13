-- 대학 학사관리 시스템 - 테이블 생성 스크립트
-- ============================================
-- 1. 학과 (Department)
-- ============================================
CREATE TABLE department (
    dept_code       VARCHAR2(10)    PRIMARY KEY,
    dept_name       VARCHAR2(100)   NOT NULL,
    college_name    VARCHAR2(100),
    office_location VARCHAR2(50),
    office_phone    VARCHAR2(20)
);

COMMENT ON TABLE department IS '학과 정보';
COMMENT ON COLUMN department.dept_code IS '학과 코드 (PK)';
COMMENT ON COLUMN department.dept_name IS '학과명';
COMMENT ON COLUMN department.college_name IS '단과대학명';

-- ============================================
-- 2. 교수 (Professor)
-- ============================================
CREATE TABLE professor (
    professor_id    VARCHAR2(20)    PRIMARY KEY,
    name_kr         VARCHAR2(100)   NOT NULL,
    name_en         VARCHAR2(100),
    rrn             CHAR(13),
    passport_no     VARCHAR2(30),
    dept_code       VARCHAR2(10)    NOT NULL,
    position        VARCHAR2(50),
    office_location VARCHAR2(50),
    office_phone    VARCHAR2(20),
    email           VARCHAR2(100),
    hire_date       DATE,
    
    CONSTRAINT professor_dept_fk 
        FOREIGN KEY (dept_code) 
        REFERENCES department(dept_code),
    
    CONSTRAINT professor_ident_ck 
        CHECK (rrn IS NOT NULL OR passport_no IS NOT NULL)
);

COMMENT ON TABLE professor IS '교수 정보';
COMMENT ON COLUMN professor.rrn IS '주민등록번호 (내국인)';
COMMENT ON COLUMN professor.passport_no IS '여권번호 (외국인)';

-- ============================================
-- 3. 학생 (Student)
-- ============================================
CREATE TABLE student (
    student_id      VARCHAR2(20)    PRIMARY KEY,
    name_kr         VARCHAR2(100)   NOT NULL,
    name_en         VARCHAR2(100),
    rrn             CHAR(13),
    passport_no     VARCHAR2(30),
    nationality     VARCHAR2(50),
    dept_code       VARCHAR2(10)    NOT NULL,
    status          VARCHAR2(20)    NOT NULL,
    admission_date  DATE            NOT NULL,
    address         VARCHAR2(200),
    phone           VARCHAR2(20),
    email           VARCHAR2(100),
    
    CONSTRAINT student_dept_fk 
        FOREIGN KEY (dept_code) 
        REFERENCES department(dept_code),
    
    CONSTRAINT student_ident_ck 
        CHECK (rrn IS NOT NULL OR passport_no IS NOT NULL)
);

COMMENT ON TABLE student IS '학생 정보';
COMMENT ON COLUMN student.status IS '학적 상태 (ENROLLED/LEAVE/WITHDRAWN/GRADUATED)';

-- ============================================
-- 4. 과목 (Course)
-- ============================================
CREATE TABLE course (
    course_code         VARCHAR2(20)    PRIMARY KEY,
    course_name_kr      VARCHAR2(200)   NOT NULL,
    course_name_en      VARCHAR2(200),
    credit              NUMBER(3,1)     NOT NULL,
    course_type         VARCHAR2(50),
    recommended_year    NUMBER(10),
    is_deleted          CHAR(1)         DEFAULT 'N' NOT NULL,
    
    CONSTRAINT course_deleted_ck CHECK (is_deleted IN ('Y', 'N'))
);

COMMENT ON TABLE course IS '과목 마스터 정보';
COMMENT ON COLUMN course.course_type IS '과목 구분 (전공필수/전공선택/교양)';
COMMENT ON COLUMN course.is_deleted IS '폐지 여부 (Y/N)';

-- ============================================
-- 5. 선수/동시수강 관계 (Prerequisite)
-- ============================================
CREATE TABLE prerequisite (
    course_code         VARCHAR2(20)    NOT NULL,
    prereq_course_code  VARCHAR2(20)    NOT NULL,
    coreq_flag          CHAR(1)         DEFAULT 'N' NOT NULL,
    
    CONSTRAINT prerequisite_pk PRIMARY KEY (course_code, prereq_course_code),
    
    CONSTRAINT prerequisite_course_fk 
        FOREIGN KEY (course_code) 
        REFERENCES course(course_code),
    
    CONSTRAINT prerequisite_prereq_fk 
        FOREIGN KEY (prereq_course_code) 
        REFERENCES course(course_code),
    
    CONSTRAINT prerequisite_flag_ck CHECK (coreq_flag IN ('Y', 'N'))
);

COMMENT ON TABLE prerequisite IS '선수과목/동시수강 관계';
COMMENT ON COLUMN prerequisite.coreq_flag IS 'N=선이수필수, Y=동시수강허용';

-- ============================================
-- 6. 개설강좌 (OpenCourse)
-- ============================================
CREATE SEQUENCE seq_open_course START WITH 1 INCREMENT BY 1;

CREATE TABLE open_course (
    open_course_id  NUMBER          PRIMARY KEY,
    year            NUMBER(4)       NOT NULL,
    term            VARCHAR2(10)    NOT NULL,
    course_code     VARCHAR2(20)    NOT NULL,
    section         VARCHAR2(10)    NOT NULL,
    professor_id    VARCHAR2(20),
    room            VARCHAR2(50),
    capacity        NUMBER(10)      NOT NULL,
    enrolled_count  NUMBER(10)      DEFAULT 0 NOT NULL,
    is_canceled     CHAR(1)         DEFAULT 'N' NOT NULL,
    
    CONSTRAINT open_course_course_fk 
        FOREIGN KEY (course_code) 
        REFERENCES course(course_code),
    
    CONSTRAINT open_course_professor_fk 
        FOREIGN KEY (professor_id) 
        REFERENCES professor(professor_id),
    
    CONSTRAINT open_course_uk 
        UNIQUE (year, term, course_code, section),
    
    CONSTRAINT open_course_canceled_ck CHECK (is_canceled IN ('Y', 'N'))
);

COMMENT ON TABLE open_course IS '학기별 개설강좌';
COMMENT ON COLUMN open_course.professor_id IS '담당교수 (미배정 가능)';
COMMENT ON COLUMN open_course.enrolled_count IS '현재 신청 인원 (트리거로 자동 관리)';

-- ============================================
-- 7. 강의시간 (LectureSchedule)
-- ============================================
CREATE SEQUENCE seq_lecture_schedule START WITH 1 INCREMENT BY 1;

CREATE TABLE lecture_schedule (
    schedule_id     NUMBER          PRIMARY KEY,
    open_course_id  NUMBER          NOT NULL,
    day_of_week     NUMBER(1)       NOT NULL,
    start_period    NUMBER(10)      NOT NULL,
    end_period      NUMBER(10)      NOT NULL,
    is_consecutive  CHAR(1)         DEFAULT 'N' NOT NULL,
    
    CONSTRAINT lecture_schedule_course_fk 
        FOREIGN KEY (open_course_id) 
        REFERENCES open_course(open_course_id),
    
    CONSTRAINT lecture_slot_ck 
        CHECK (start_period <= end_period),
    
    CONSTRAINT lecture_consecutive_ck CHECK (is_consecutive IN ('Y', 'N'))
);

COMMENT ON TABLE lecture_schedule IS '강의 시간표';
COMMENT ON COLUMN lecture_schedule.day_of_week IS '요일 (1=Mon, 7=Sun)';
COMMENT ON COLUMN lecture_schedule.is_consecutive IS '연강 여부 (Y/N)';

-- ============================================
-- 8. 수강신청 (Enrollment)
-- ============================================
CREATE SEQUENCE seq_enrollment START WITH 1 INCREMENT BY 1;

CREATE TABLE enrollment (
    enrollment_id   NUMBER          PRIMARY KEY,
    student_id      VARCHAR2(20)    NOT NULL,
    open_course_id  NUMBER          NOT NULL,
    requested_at    TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    status          VARCHAR2(20)    NOT NULL,
    is_retake       CHAR(1)         DEFAULT 'N' NOT NULL,
    created_by      VARCHAR2(50),
    created_at      TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    
    CONSTRAINT enrollment_student_fk 
        FOREIGN KEY (student_id) 
        REFERENCES student(student_id),
    
    CONSTRAINT enrollment_course_fk 
        FOREIGN KEY (open_course_id) 
        REFERENCES open_course(open_course_id),
    
    CONSTRAINT enrollment_uk 
        UNIQUE (student_id, open_course_id),
    
    CONSTRAINT enrollment_retake_ck CHECK (is_retake IN ('Y', 'N'))
);

COMMENT ON TABLE enrollment IS '수강신청 기록';
COMMENT ON COLUMN enrollment.status IS '신청 상태 (APPLIED/APPROVED/WAITING/CANCELLED)';
COMMENT ON COLUMN enrollment.is_retake IS '재수강 여부 (Y/N)';

-- ============================================
-- 9. 성적 (Grade)
-- ============================================
CREATE SEQUENCE seq_grade START WITH 1 INCREMENT BY 1;

CREATE TABLE grade (
    grade_id        NUMBER          PRIMARY KEY,
    enrollment_id   NUMBER          NOT NULL UNIQUE,
    midterm_score   NUMBER(5,2),
    final_score     NUMBER(5,2),
    final_grade     VARCHAR2(3),
    grade_point     NUMBER(3,2),
    grade_confirmed CHAR(1)         DEFAULT 'N' NOT NULL,
    confirmed_at    TIMESTAMP,
    confirmed_by    VARCHAR2(20),
    
    CONSTRAINT grade_enrollment_fk 
        FOREIGN KEY (enrollment_id) 
        REFERENCES enrollment(enrollment_id) 
        ON DELETE CASCADE,
    
    CONSTRAINT grade_professor_fk 
        FOREIGN KEY (confirmed_by) 
        REFERENCES professor(professor_id),
    
    CONSTRAINT grade_confirmed_ck CHECK (grade_confirmed IN ('Y', 'N'))
);

COMMENT ON TABLE grade IS '성적 정보 (Enrollment와 1:1 관계)';
COMMENT ON COLUMN grade.enrollment_id IS '수강신청 ID (UNIQUE, 1:1 관계 보장)';
COMMENT ON COLUMN grade.grade_confirmed IS '성적 확정 여부 (Y/N)';

-- ============================================
-- 10. 감사로그 (AuditLog)
-- ============================================
CREATE SEQUENCE seq_audit_log START WITH 1 INCREMENT BY 1;

CREATE TABLE audit_log (
    log_id      NUMBER          PRIMARY KEY,
    table_name  VARCHAR2(50)    NOT NULL,
    record_id   VARCHAR2(100)   NOT NULL,
    operation   VARCHAR2(10)    NOT NULL,
    changed_by  VARCHAR2(50),
    changed_at  TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    remarks     CLOB
);

COMMENT ON TABLE audit_log IS '감사 로그 (모든 주요 데이터 변경 이력)';
COMMENT ON COLUMN audit_log.operation IS 'INSERT/UPDATE/DELETE';

-- ============================================
-- 11. 시스템 파라미터 (SystemParameter)
-- ============================================
CREATE TABLE system_parameter (
    param_key   VARCHAR2(100)   PRIMARY KEY,
    param_value VARCHAR2(100)   NOT NULL,
    description CLOB
);

COMMENT ON TABLE system_parameter IS '시스템 설정값 (학점 제한, 재수강 기준 등)';

-- 기본 설정값 삽입
INSERT INTO system_parameter (param_key, param_value, description) VALUES
('max_credits_per_semester', '18', '학기당 최대 신청 학점');
INSERT INTO system_parameter (param_key, param_value, description) VALUES
('max_credits_excellence', '21', '우수자 최대 신청 학점 (평점 4.0+)');
INSERT INTO system_parameter (param_key, param_value, description) VALUES
('retake_grade_threshold', '2.5', '재수강 허용 기준 (C+ 이하)');
INSERT INTO system_parameter (param_key, param_value, description) VALUES
('min_graduation_gpa', '2.0', '최소 졸업 평점');

COMMIT;

-- ============================================
-- 인덱스 생성 
-- ============================================

-- 자주 조회되는 FK 컬럼
CREATE INDEX idx_professor_dept ON professor(dept_code);
CREATE INDEX idx_student_dept ON student(dept_code);
CREATE INDEX idx_open_course_course ON open_course(course_code);
CREATE INDEX idx_open_course_professor ON open_course(professor_id);
CREATE INDEX idx_enrollment_student ON enrollment(student_id);
CREATE INDEX idx_enrollment_course ON enrollment(open_course_id);

-- 수강신청 검증용 복합 인덱스
CREATE INDEX idx_open_course_year_term ON open_course(year, term);
CREATE INDEX idx_lecture_schedule_course ON lecture_schedule(open_course_id, day_of_week);

-- ============================================
-- 트리거: 정원 자동 관리
-- ============================================

-- 정원 증감 트리거
CREATE OR REPLACE TRIGGER trg_enrollment_count
AFTER INSERT OR UPDATE OR DELETE ON enrollment
FOR EACH ROW
DECLARE
    v_open_course_id NUMBER;
BEGIN
    -- INSERT 시 APPROVED 상태면 정원 +1
    IF INSERTING THEN
        IF (:NEW.status = 'APPROVED') THEN
            UPDATE open_course
            SET enrolled_count = enrolled_count + 1
            WHERE open_course_id = :NEW.open_course_id;
        END IF;
    
    -- UPDATE 시 상태 변경 확인
    ELSIF UPDATING THEN
        -- 미승인 → 승인: +1
        IF (:OLD.status != 'APPROVED' AND :NEW.status = 'APPROVED') THEN
            UPDATE open_course
            SET enrolled_count = enrolled_count + 1
            WHERE open_course_id = :NEW.open_course_id;
        -- 승인 → 미승인: -1
        ELSIF (:OLD.status = 'APPROVED' AND :NEW.status != 'APPROVED') THEN
            UPDATE open_course
            SET enrolled_count = GREATEST(enrolled_count - 1, 0)
            WHERE open_course_id = :NEW.open_course_id;
        END IF;
    
    -- DELETE 시 APPROVED였으면 -1
    ELSIF DELETING THEN
        IF (:OLD.status = 'APPROVED') THEN
            UPDATE open_course
            SET enrolled_count = GREATEST(enrolled_count - 1, 0)
            WHERE open_course_id = :OLD.open_course_id;
        END IF;
    END IF;
END;
/

-- 테이블 목록 확인
SELECT table_name FROM user_tables ORDER BY table_name;

-- 시퀀스 확인
SELECT sequence_name FROM user_sequences;

-- 트리거 확인
SELECT trigger_name, status FROM user_triggers;

-- 인덱스 확인
SELECT index_name, table_name FROM user_indexes 
WHERE table_name IN ('DEPARTMENT', 'PROFESSOR', 'STUDENT', 'COURSE', 
                     'OPEN_COURSE', 'ENROLLMENT', 'GRADE');