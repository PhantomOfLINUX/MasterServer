package org.codequistify.master.domain.stage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Stage")
@RequestMapping("api/stage")
public class StageController {
    // 스테이지 목록 조회

    // 스테이지 문항 조회 -> 정답은 클라이언트에게 제공 안 됨, 옵션들은 전부 제공되어야 함

    // 문항 체점 요청

    // 스테이지 등록

    // 스테이지 수정

    // 스테이지 문항 수정

    // 스테이지 옵션 수정
}
