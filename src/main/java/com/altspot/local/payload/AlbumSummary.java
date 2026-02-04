package com.altspot.local.payload;

import com.altspot.local.model.Artist;

public interface AlbumSummary {
    Long getId();
    String getName();
    Long getPrimaryArtistId();
    String getPrimaryArtistName();
}
