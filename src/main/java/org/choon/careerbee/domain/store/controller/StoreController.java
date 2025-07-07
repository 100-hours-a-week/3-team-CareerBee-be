package org.choon.careerbee.domain.store.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.choon.careerbee.domain.store.service.command.StoreCommandService;
import org.choon.careerbee.domain.store.service.query.StoreQueryService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StoreController {

    private final StoreCommandService commandService;
    private final StoreQueryService queryService;
}
