package org.service.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private Long userId;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
}