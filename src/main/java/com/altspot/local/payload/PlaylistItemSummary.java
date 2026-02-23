package com.altspot.local.payload;


public interface PlaylistItemSummary {
    Long getPlaylistItemId();
    Long getPosition();
    Long getTrackId();
    String getTrackName();
    Integer getDurationSeconds();
    Long getAlbumId();
    String getAlbumName();



}
