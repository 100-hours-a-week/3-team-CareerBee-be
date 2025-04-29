package org.choon.careerbee.domain.member.repository;

import org.choon.careerbee.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
