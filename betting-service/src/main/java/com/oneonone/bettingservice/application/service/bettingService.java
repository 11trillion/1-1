package com.oneonone.bettingservice.application.service;

import com.oneonone.bettingservice.domain.BettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class bettingService {
    private final BettingRepository bettingRepository;
}
