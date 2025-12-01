package com.oneonone.pointservice.presentation.response;

import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.enums.PointStatus;
import com.oneonone.pointservice.domain.enums.PointType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class PointResponse {
    private UUID pointId;
    private PointType type;
    private int amount;
    private String description;
    private PointStatus status;
    private Long userId;

    public static PointResponse from(Point point) {
        PointResponse response = new PointResponse();
        response.pointId = point.getId();
        response.type = point.getPointType();
        response.amount = point.getAmount();
        response.description = point.getDescription();
        response.status = point.getStatus();
        response.userId = point.getUserId();
        return response;
    }
}
