package com.oneonone.pointservice.presentation.request;

import com.oneonone.pointservice.domain.enums.PointType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePointRequest {
    private PointType pointType;
    private int amount;
    private String description;
    private Long userId;
}
