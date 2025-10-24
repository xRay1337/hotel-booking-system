package org.service.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.service.booking.entity.Booking;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    private Long userId;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isAutoSelect;
}