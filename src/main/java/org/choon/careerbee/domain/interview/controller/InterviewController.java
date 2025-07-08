package org.choon.careerbee.domain.interview.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.interview.service.command.InterviewCommandService;
import org.choon.careerbee.domain.interview.service.query.InterviewQueryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/")
public class InterviewController {

    private final InterviewQueryService queryService;
    private final InterviewCommandService commandService;


}
