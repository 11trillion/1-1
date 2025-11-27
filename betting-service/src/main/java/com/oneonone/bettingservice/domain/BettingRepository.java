package com.oneonone.bettingservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BettingRepository extends JpaRepository<Betting, UUID> {

}
