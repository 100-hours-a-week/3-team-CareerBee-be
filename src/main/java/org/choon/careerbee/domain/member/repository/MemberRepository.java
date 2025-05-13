package org.choon.careerbee.domain.member.repository;

import java.util.Optional;
import org.choon.careerbee.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    Optional<Long> findIdByEmail(String email);

    Optional<Member> findByEmail(String email);
}
