package com.oneonone.bettingservice.application.service;

import com.oneonone.bettingservice.domain.service.BettingService;
import com.oneonone.bettingservice.domain.event.GameCompletedEvent;
import com.oneonone.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BettingKafkaListener {

    private final BettingService bettingService;

    @Transactional
    @KafkaListener(groupId = "betting-service", topics = "${kafka.topics.game-completed:gameResult}")
    public void updateGameResult(GameCompletedEvent requestDto){
        try{
            bettingService.updateGameResult(requestDto);
        }catch (BusinessException e){
            log.warn("gameResult 오류 발생");
        }
    }
}
