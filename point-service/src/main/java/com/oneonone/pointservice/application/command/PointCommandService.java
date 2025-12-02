package com.oneonone.pointservice.application.command;

import com.oneonone.common.exception.BusinessException;
import com.oneonone.pointservice.domain.PointErrorCode;
import com.oneonone.pointservice.domain.entity.Point;
import com.oneonone.pointservice.domain.repository.PointRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointCommandService {
    private final PointRepository pointRepository;

    @Transactional
    public Point createPoint(CreatePointCommand command) {
        Point point = new Point(
                command.getType(),
                command.getAmount(),
                command.getDescription(),
                command.getUserId()
        );
        return pointRepository.save(point);
    }

    @Transactional
    public Point updatePointStatus(UpdatePointStatusCommand command) {
        Point point = pointRepository.findById(command.getPointId())
                .orElseThrow(() -> new BusinessException(PointErrorCode.POINT_NOT_FOUND));

        try {
            point.changeStatus(command.getStatus());
        } catch (IllegalStateException e) {
            throw new BusinessException(PointErrorCode.STATUS_CANNOT_CHANGE);
        }

        return pointRepository.save(point);
    }
}
