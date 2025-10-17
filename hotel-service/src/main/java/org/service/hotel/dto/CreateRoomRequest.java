package org.service.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    private String number;
    private String type;
    private Double price;
    private Boolean available;
    private Long hotelId;
}