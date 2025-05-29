package org.choon.careerbee.domain.company.entity.techStack;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.choon.careerbee.domain.company.entity.enums.StackType;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLRestriction("deleted_at is NULL")
@Table(name = "tech_stack")
public class TechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", length = 13, nullable = false)
    @Enumerated(EnumType.STRING)
    private StackType stackType;

    @Column(length = 20, nullable = false, unique = true)
    private String name;

    @Column(length = 500, nullable = false)
    private String imgUrl;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime deletedAt;
}
