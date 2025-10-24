package org.service.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private Long id;
    private Long hotelId;
    private String roomNumber;
    private String roomType;
    private BigDecimal price;
    private Integer capacity;
    private String description;
    private Boolean available;
}