package com.oneonone.pointservice.application.command;

import com.oneonone.pointservice.domain.enums.PointType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreatePointCommand {
    private final PointType type;
    private final int amount;
    private final String description;
    private final Long userId;
}