package com.altspot.local.payload;

import com.altspot.local.model.Artist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDTO {

    private Long id;
    private String name;
    private Long primaryArtistId;
    private String primaryArtistName;

}
