package org.choon.careerbee.domain.competition.controller;

import lombok.RequiredArgsConstructor;
import org.choon.careerbee.domain.competition.service.CompetitionCommandService;
import org.choon.careerbee.domain.competition.service.CompetitionQueryService;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class CompetitionController {

    private final CompetitionQueryService queryService;
    private final CompetitionCommandService commandService;
}
