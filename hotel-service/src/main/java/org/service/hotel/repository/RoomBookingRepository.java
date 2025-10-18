package org.service.hotel.repository;

import org.service.hotel.entity.RoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomBookingRepository extends JpaRepository<RoomBooking, Long> {

    @Query("SELECT CASE WHEN COUNT(rb) > 0 THEN true ELSE false END " +
            "FROM RoomBooking rb WHERE rb.roomId = :roomId AND " +
            "rb.status = 'CONFIRMED' AND " +
            "((:startDate BETWEEN rb.startDate AND rb.endDate) OR " +
            "(:endDate BETWEEN rb.startDate AND rb.endDate) OR " +
            "(rb.startDate BETWEEN :startDate AND :endDate))")
    boolean existsConfirmedBookingForRoomInDateRange(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<RoomBooking> findByRoomId(Long roomId);

    List<RoomBooking> findByRoomIdAndStatus(Long roomId, RoomBooking.BookingStatus status);
}