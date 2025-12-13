package university.service;

import university.dao.GradeDAO;
import university.model.Grade;

/**
 * 성적 처리 비즈니스 로직 서비스
 */
public class GradeService {

    private GradeDAO gradeDAO;

    public GradeService() {
        this.gradeDAO = new GradeDAO();
    }

    /**
     * 점수를 기반으로 성적 등급과 평점 자동 계산
     */
    public GradeCalculation calculateGrade(Double midtermScore, Double finalScore) {
        if (midtermScore == null || finalScore == null) {
            return new GradeCalculation(null, null, "점수가 입력되지 않았습니다.");
        }

        // 평균 점수 계산
        double average = (midtermScore + finalScore) / 2.0;

        String letterGrade;
        Double gradePoint;

        // 성적 등급 및 평점 계산
        if (average >= 95) {
            letterGrade = "A+";
            gradePoint = 4.5;
        } else if (average >= 90) {
            letterGrade = "A0";
            gradePoint = 4.0;
        } else if (average >= 85) {
            letterGrade = "B+";
            gradePoint = 3.5;
        } else if (average >= 80) {
            letterGrade = "B0";
            gradePoint = 3.0;
        } else if (average >= 75) {
            letterGrade = "C+";
            gradePoint = 2.5;
        } else if (average >= 70) {
            letterGrade = "C0";
            gradePoint = 2.0;
        } else if (average >= 65) {
            letterGrade = "D+";
            gradePoint = 1.5;
        } else if (average >= 60) {
            letterGrade = "D0";
            gradePoint = 1.0;
        } else {
            letterGrade = "F";
            gradePoint = 0.0;
        }

        return new GradeCalculation(letterGrade, gradePoint,
                String.format("평균: %.2f점", average));
    }

    /**
     * 성적 입력/수정
     */
    public GradeResult inputGrade(int enrollmentId, Double midtermScore,
                                  Double finalScore, String professorId) {
        // 1. 성적 계산
        GradeCalculation calc = calculateGrade(midtermScore, finalScore);

        if (calc.getLetterGrade() == null) {
            return new GradeResult(false, calc.getMessage());
        }

        // 2. 기존 성적 확인
        Grade existingGrade = gradeDAO.selectByEnrollmentId(enrollmentId);

        if (existingGrade != null) {
            // 수정
            existingGrade.setMidtermScore(midtermScore);
            existingGrade.setFinalScore(finalScore);
            existingGrade.setFinalGrade(calc.getLetterGrade());
            existingGrade.setGradePoint(calc.getGradePoint());
            existingGrade.setGradeConfirmed("N"); // 수정 시 확정 해제

            boolean success = gradeDAO.update(existingGrade);

            if (success) {
                return new GradeResult(true, "성적이 수정되었습니다. " + calc.getMessage());
            } else {
                return new GradeResult(false, "성적 수정 중 오류가 발생했습니다.");
            }

        } else {
            // 신규 등록
            Grade grade = new Grade();
            grade.setEnrollmentId(enrollmentId);
            grade.setMidtermScore(midtermScore);
            grade.setFinalScore(finalScore);
            grade.setFinalGrade(calc.getLetterGrade());
            grade.setGradePoint(calc.getGradePoint());
            grade.setGradeConfirmed("N");
            grade.setConfirmedBy(professorId);

            boolean success = gradeDAO.insert(grade);

            if (success) {
                return new GradeResult(true, "성적이 입력되었습니다. " + calc.getMessage());
            } else {
                return new GradeResult(false, "성적 입력 중 오류가 발생했습니다.");
            }
        }
    }

    /**
     * 성적 확정
     */
    public GradeResult confirmGrade(int gradeId, String professorId) {
        Grade grade = gradeDAO.selectByEnrollmentId(gradeId);

        if (grade == null) {
            return new GradeResult(false, "존재하지 않는 성적입니다.");
        }

        if (grade.isConfirmed()) {
            return new GradeResult(false, "이미 확정된 성적입니다.");
        }

        if (grade.getMidtermScore() == null || grade.getFinalScore() == null) {
            return new GradeResult(false, "중간고사와 기말고사 점수를 모두 입력해야 확정할 수 있습니다.");
        }

        boolean success = gradeDAO.confirmGrade(gradeId, professorId);

        if (success) {
            return new GradeResult(true, "성적이 확정되었습니다.");
        } else {
            return new GradeResult(false, "성적 확정 중 오류가 발생했습니다.");
        }
    }

    /**
     * 학생 평점 계산
     */
    public double calculateStudentGPA(String studentId) {
        return gradeDAO.calculateGPA(studentId);
    }

    /**
     * 성적 계산 결과를 담는 내부 클래스
     */
    public static class GradeCalculation {
        private String letterGrade;
        private Double gradePoint;
        private String message;

        public GradeCalculation(String letterGrade, Double gradePoint, String message) {
            this.letterGrade = letterGrade;
            this.gradePoint = gradePoint;
            this.message = message;
        }

        public String getLetterGrade() {
            return letterGrade;
        }

        public Double getGradePoint() {
            return gradePoint;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 성적 처리 결과를 담는 내부 클래스
     */
    public static class GradeResult {
        private boolean success;
        private String message;

        public GradeResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}