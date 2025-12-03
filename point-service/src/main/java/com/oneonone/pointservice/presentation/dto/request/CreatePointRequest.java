package com.oneonone.pointservice.presentation.dto.request;

import com.oneonone.pointservice.domain.enums.PointType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePointRequest {
    private PointType pointType;
    private Long amount;
    private String description;
    private Long userId;
}
