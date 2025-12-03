package com.oneonone.pointservice.application.command;

import com.oneonone.pointservice.domain.enums.PointStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UpdatePointStatusCommand {
    private final UUID pointId;
    private final PointStatus status;
}
