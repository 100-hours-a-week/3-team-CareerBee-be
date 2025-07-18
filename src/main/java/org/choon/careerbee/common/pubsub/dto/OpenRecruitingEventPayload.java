package org.choon.careerbee.common.pubsub.dto;


import java.util.Map;
import java.util.Set;

public record OpenRecruitingEventPayload(
    Map<String, Set<Long>> notifyMap
) {

}
