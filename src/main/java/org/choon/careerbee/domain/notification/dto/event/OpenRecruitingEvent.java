package org.choon.careerbee.domain.notification.dto.event;

import java.util.Map;
import java.util.Set;

public record OpenRecruitingEvent(
    Map<String, Set<Long>> notifyMap
) {}
