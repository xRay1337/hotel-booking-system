package org.service.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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