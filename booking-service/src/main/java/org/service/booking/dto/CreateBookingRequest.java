package org.service.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    private Long userId;  // ← ДОБАВИЛИ userId
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean autoSelect = false;
    private String correlationId;
}