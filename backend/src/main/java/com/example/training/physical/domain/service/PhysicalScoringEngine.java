package com.example.training.physical.domain.service;

import com.example.training.physical.domain.model.PhysicalScoreStandard;
import com.example.training.physical.domain.model.PhysicalTestItem;
import com.example.training.physical.domain.repository.PhysicalTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhysicalScoringEngine {

    private final PhysicalTestRepository physicalTestRepository;

    public ScoringResult scoreItem(Long itemId, BigDecimal rawValue, String gender, LocalDate birthDate, LocalDate testDate) {
        if (rawValue == null || rawValue.compareTo(BigDecimal.ZERO) < 0) {
            return ScoringResult.empty();
        }

        int ageMonths = calculateAgeMonths(birthDate, testDate);
        String ageGroup = resolveAgeGroup(ageMonths);

        List<PhysicalScoreStandard> standards = physicalTestRepository
                .findStandardsByItemIdAndGender(itemId, gender, ageGroup);

        if (standards == null || standards.isEmpty()) {
            log.warn("No scoring standard found for itemId={}, gender={}, ageGroup={}", itemId, gender, ageGroup);
            return ScoringResult.empty();
        }

        for (PhysicalScoreStandard standard : standards) {
            if (isInRange(rawValue, standard)) {
                return new ScoringResult(
                        standard.getScoreValue() != null ? standard.getScoreValue() : BigDecimal.valueOf(standard.getScore()),
                        standard.getLevel(),
                        standard.getScore()
                );
            }
        }

        return ScoringResult.empty();
    }

    public BigDecimal calculateOverallScore(List<ScoringResult> results) {
        return results.stream()
                .filter(r -> r.scoreValue != null)
                .map(r -> r.scoreValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(1, RoundingMode.HALF_UP);
    }

    public String calculateOverallLevel(BigDecimal totalScore, int itemCount) {
        if (itemCount == 0 || totalScore == null) {
            return "INADEQUATE";
        }
        double maxScore = itemCount * 5.0;
        double ratio = totalScore.doubleValue() / maxScore;

        if (ratio >= 0.9) return "EXCELLENT";
        if (ratio >= 0.8) return "GOOD";
        if (ratio >= 0.6) return "PASS";
        if (ratio >= 0.4) return "INADEQUATE";
        return "TERRIBLE";
    }

    private int calculateAgeMonths(LocalDate birthDate, LocalDate testDate) {
        if (birthDate == null || testDate == null) {
            return 36;
        }
        Period period = Period.between(birthDate, testDate);
        return period.getYears() * 12 + period.getMonths();
    }

    private String resolveAgeGroup(int ageMonths) {
        int ageYears = ageMonths / 12;
        int remainingMonths = ageMonths % 12;

        if (remainingMonths < 6) {
            return String.valueOf(ageYears);
        } else {
            return ageYears + ".5";
        }
    }

    private boolean isInRange(BigDecimal value, PhysicalScoreStandard standard) {
        if (standard.getMinValue() != null && value.compareTo(standard.getMinValue()) < 0) {
            return false;
        }
        if (standard.getMaxValue() != null && value.compareTo(standard.getMaxValue()) > 0) {
            return false;
        }
        return true;
    }

    public static class ScoringResult {
        private BigDecimal scoreValue;
        private String level;
        private int score;

        public ScoringResult(BigDecimal scoreValue, String level, int score) {
            this.scoreValue = scoreValue;
            this.level = level;
            this.score = score;
        }

        public static ScoringResult empty() {
            return new ScoringResult(null, null, 0);
        }

        public BigDecimal getScoreValue() { return scoreValue; }
        public String getLevel() { return level; }
        public int getScore() { return score; }
        public boolean isValid() { return scoreValue != null; }
    }
}
