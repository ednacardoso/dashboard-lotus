package com.example.backend.room;

import com.example.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRentalRepository extends JpaRepository<RoomRental, Long> {

    List<RoomRental> findByYearMonthAndActiveTrueOrderByCreatedAtDesc(String yearMonth);

    List<RoomRental> findByProfessionalAndYearMonthAndActiveTrue(User professional, String yearMonth);

    List<RoomRental> findByRoomAndYearMonthAndActiveTrue(Room room, String yearMonth);

    Optional<RoomRental> findFirstByProfessionalAndYearMonthAndActiveTrue(User professional, String yearMonth);

    Optional<RoomRental> findFirstByRoomAndYearMonthAndActiveTrue(Room room, String yearMonth);
}
