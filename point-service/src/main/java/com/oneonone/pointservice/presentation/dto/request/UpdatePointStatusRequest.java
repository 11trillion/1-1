package com.oneonone.pointservice.presentation.dto.request;

import com.oneonone.pointservice.domain.enums.PointStatus;
import lombok.Getter;

@Getter
public class UpdatePointStatusRequest {
    private PointStatus status;
}