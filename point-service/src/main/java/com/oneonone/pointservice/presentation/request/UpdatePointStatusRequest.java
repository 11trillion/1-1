package com.oneonone.pointservice.presentation.request;

import com.oneonone.pointservice.domain.enums.PointStatus;
import lombok.Getter;

@Getter
public class UpdatePointStatusRequest {
    private PointStatus status;
}