package org.service.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String correlationId; // для идемпотентности
}