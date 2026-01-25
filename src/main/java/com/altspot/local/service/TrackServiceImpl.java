package com.altspot.local.service;

import com.altspot.local.repository.TrackRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TrackServiceImpl implements TrackService {

    @Value("${music.directory.path}")
    private String musicDirectoryPath;

    private final TrackRepository trackRepository;

    public TrackServiceImpl(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }




}
