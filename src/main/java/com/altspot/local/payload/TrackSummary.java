package com.altspot.local.payload;

import com.altspot.local.model.Album;
import com.altspot.local.model.Artist;

import java.util.Set;

public interface TrackSummary {
    Long getId();
    String getName();
    Integer getDurationSeconds();
}
