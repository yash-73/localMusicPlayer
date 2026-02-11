package com.altspot.local.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public interface TrackArtistFlatRow {
    Long getTrackId();
    Long getArtistId();
    String getArtistName();
}
