package com.altspot.local.controller;

import com.altspot.local.payload.RescanResult;
import com.altspot.local.service.TrackService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/stream/{id}")
    public ResponseEntity<Resource> streamAudio(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String range
    ) throws IOException {
        return trackService.stream(id, range);
    }
}
