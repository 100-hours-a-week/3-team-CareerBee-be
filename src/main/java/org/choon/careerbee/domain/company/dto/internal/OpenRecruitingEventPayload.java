package org.choon.careerbee.domain.company.dto.internal;


import java.util.Map;
import java.util.Set;

public record OpenRecruitingEventPayload(
    Map<String, Set<Long>> notifyMap
) {

}
