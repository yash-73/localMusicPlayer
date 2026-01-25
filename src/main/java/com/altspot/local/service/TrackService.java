package com.altspot.local.service;


import com.altspot.local.payload.RescanResult;

import java.io.IOException;
import java.nio.file.Path;

public interface TrackService {

    RescanResult rescan() throws IOException;
}
