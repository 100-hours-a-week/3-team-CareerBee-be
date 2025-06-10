package org.choon.careerbee.domain.competition.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
    name = "competition",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"start_date_time", "end_date_time"})
    })
public class Competition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false, columnDefinition = "DATETIME")
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
}
