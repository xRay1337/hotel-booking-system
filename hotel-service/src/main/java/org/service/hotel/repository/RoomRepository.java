package org.service.hotel.repository;

import org.service.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotelId(Long hotelId);
    List<Room> findByAvailableTrue();

    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.timesBooked ASC")
    List<Room> findAvailableRoomsOrderByTimesBooked();

    List<Room> findByIdIn(List<Long> ids);

    List<Room> findByHotelIdAndAvailableTrue(Long hotelId);

    List<Room> findByHotelIdAndAvailableFalse(Long hotelId);

    List<Room> findByTypeAndAvailableTrue(String type);

    List<Room> findByPriceLessThanEqualAndAvailableTrue(Double maxPrice);

    List<Room> findByTypeAndPriceLessThanEqualAndAvailableTrue(String type, Double maxPrice);

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.available = true AND r.type = :type AND r.price <= :maxPrice")
    List<Room> findAvailableRoomsByHotelTypeAndPrice(@Param("hotelId") Long hotelId,
                                                     @Param("type") String type,
                                                     @Param("maxPrice") Double maxPrice);
}