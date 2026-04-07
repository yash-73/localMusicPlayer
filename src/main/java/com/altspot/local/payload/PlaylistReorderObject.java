package com.altspot.local.payload;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlaylistReorderObject {
    Long playlistId;
    Long playlistItemId;
    Long previousId;
    Long nextId;
}
