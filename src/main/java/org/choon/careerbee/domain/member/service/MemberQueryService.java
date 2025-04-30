package org.choon.careerbee.domain.member.service;

import java.util.Optional;

public interface MemberQueryService {

  boolean isMemberExistByEmail(String email);
  Optional<Long> getMemberIdByEmail(String email);
}
