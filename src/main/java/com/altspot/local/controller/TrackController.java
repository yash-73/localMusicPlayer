package com.altspot.local.controller;

import com.altspot.local.payload.RescanResult;
import com.altspot.local.service.TrackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @PostMapping("rescan")
    public ResponseEntity<RescanResult> rescan() throws IOException {
        RescanResult result = trackService.rescan();
        return ResponseEntity.ok(result);
    }
}
