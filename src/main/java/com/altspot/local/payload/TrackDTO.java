package com.altspot.local.payload;

import jakarta.persistence.Column;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackDTO {
    public Long id;
    public String title;
    public String album;
    public String artist;
    private String genre;
    public Integer durationSeconds;
    public Integer sampleRate;
    private Long fileSize;
}
