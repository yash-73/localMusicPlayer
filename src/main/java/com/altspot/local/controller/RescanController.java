package com.altspot.local.controller;


import com.altspot.local.payload.RescanResult;
import com.altspot.local.service.RescanServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class RescanController {

    private final RescanServiceImpl rescanService;

    public RescanController(RescanServiceImpl rescanService) {
        this.rescanService = rescanService;
    }

    @PostMapping("/rescan")
    public ResponseEntity<RescanResult> rescan() throws Exception {
        return ResponseEntity.ok(rescanService.rescan());
    }

//    @PostMapping("/refill/track-pos-album-year")
//    public ResponseEntity<Boolean> refillTrackPosAlbumYear() throws IOException {
//            Boolean result = rescanService.backfillMetadataInBatches();
//            return ResponseEntity.ok(result);
//    }

}

