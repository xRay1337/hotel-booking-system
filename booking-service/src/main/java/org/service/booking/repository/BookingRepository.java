package org.service.booking.repository;

import org.service.booking.entity.Booking;
import org.service.booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    Optional<Booking> findByIdAndUser(Long id, User user);

    List<Booking> findByRoomIdAndStatusIn(Long roomId, List<Booking.BookingStatus> statuses);
}