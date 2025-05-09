package org.choon.careerbee.domain.auth.repository;

import java.util.Optional;
import org.choon.careerbee.domain.auth.entity.Token;
import org.choon.careerbee.domain.auth.entity.enums.TokenStatus;
import org.choon.careerbee.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByMemberAndStatus(Member member, TokenStatus status);

    @Query("SELECT t FROM Token t WHERE t.tokenValue = :tokenValue AND t.status = :status")
    Optional<Token> findByTokenValueAndStatus(@Param("tokenValue") String tokenValue,
        @Param("status") TokenStatus status);
}
