package org.service.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private Long id;
    private String number;
    private String type;
    private Double price;
    private Boolean available;
    private Integer timesBooked;
    private Long hotelId;
}