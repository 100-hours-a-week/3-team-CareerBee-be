package org.choon.careerbee.domain.competition.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.common.entity.BaseEntity;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Competition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime startDateTime;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime endDateTime;

    @Builder
    private Competition(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public static Competition of(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return Competition.builder()
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .build();
    }

    public boolean canSubmit(LocalDateTime submitAt) {
        if (!submitAt.isBefore(startDateTime) && !submitAt.isAfter(endDateTime)) {
            return true;
        }

        return false;
    }
}
