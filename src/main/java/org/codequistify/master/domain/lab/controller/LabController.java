package org.codequistify.master.domain.lab.controller;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.dto.PtyUrlResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LabController {
    @GetMapping("/lab/pty")
    public ResponseEntity<PtyUrlResponse> getPtyConnectionURL() {
        return null;
    }
}
