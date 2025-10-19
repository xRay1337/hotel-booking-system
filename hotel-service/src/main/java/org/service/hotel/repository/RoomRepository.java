package org.service.hotel.repository;

import org.service.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.timesBooked ASC")
    List<Room> findAvailableRoomsOrderByTimesBooked();

    List<Room> findByLockedUntilBefore(LocalDateTime dateTime);
}