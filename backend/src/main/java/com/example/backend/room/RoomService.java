package com.example.backend.room;

import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomRentalRepository roomRentalRepository;
    private final UserRepository userRepository;

    public RoomService(RoomRepository roomRepository,
                       RoomRentalRepository roomRentalRepository,
                       UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.roomRentalRepository = roomRentalRepository;
        this.userRepository = userRepository;
    }

    public List<RoomResponse> listAll() {
        return roomRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<RoomResponse> listActive() {
        return roomRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomResponse create(RoomRequest request) {
        Room room = Room.builder()
                .name(request.name())
                .description(request.description())
                .capacity(request.capacity())
                .monthlyPrice(request.monthlyPrice())
                .active(true)
                .build();

        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        room.setName(request.name());
        room.setDescription(request.description());
        room.setCapacity(request.capacity());
        room.setMonthlyPrice(request.monthlyPrice());

        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse toggleActive(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        room.setActive(!room.isActive());
        return toResponse(roomRepository.save(room));
    }

    public List<RoomRentalResponse> listRentals(String yearMonth) {
        return roomRentalRepository.findByYearMonthAndActiveTrueOrderByCreatedAtDesc(yearMonth).stream()
                .map(this::toRentalResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomRentalResponse rentRoom(RoomRentalRequest request) {
        User professional = userRepository.findById(request.professionalId())
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        if (!room.isActive()) {
            throw new RuntimeException("Não é possível vincular uma sala inativa");
        }

        roomRentalRepository.findFirstByProfessionalAndYearMonthAndActiveTrue(professional, request.yearMonth())
                .ifPresent(existing -> {
                    throw new RuntimeException("Profissional já possui uma sala alocada no mês informado");
                });

        roomRentalRepository.findFirstByRoomAndYearMonthAndActiveTrue(room, request.yearMonth())
                .ifPresent(existing -> {
                    throw new RuntimeException("Sala já está ocupada no mês informado");
                });

        RoomRental rental = RoomRental.builder()
                .professional(professional)
                .room(room)
                .yearMonth(request.yearMonth())
                .active(true)
                .build();

        return toRentalResponse(roomRentalRepository.save(rental));
    }

    @Transactional
    public void removeRental(Long rentalId) {
        RoomRental rental = roomRentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado"));

        rental.setActive(false);
        roomRentalRepository.save(rental);
    }

    public List<RoomResponse> listVacantRooms(String yearMonth) {
        List<Long> occupiedRoomIds = roomRentalRepository.findByYearMonthAndActiveTrueOrderByCreatedAtDesc(yearMonth)
                .stream()
                .map(rental -> rental.getRoom().getId())
                .distinct()
                .collect(Collectors.toList());

        return roomRepository.findByActiveTrueOrderByNameAsc().stream()
                .filter(room -> !occupiedRoomIds.contains(room.getId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<RoomOccupancyResponse> listOccupiedRooms(String yearMonth) {
        return roomRentalRepository.findByYearMonthAndActiveTrueOrderByCreatedAtDesc(yearMonth).stream()
                .map(rental -> new RoomOccupancyResponse(
                        rental.getRoom().getId(),
                        rental.getRoom().getName(),
                        rental.getProfessional().getId(),
                        rental.getProfessional().getName(),
                        rental.getProfessional().getSpecialty(),
                        rental.getYearMonth()
                ))
                .collect(Collectors.toList());
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getCapacity(),
                room.getMonthlyPrice(),
                room.isActive(),
                room.getCreatedAt()
        );
    }

    private RoomRentalResponse toRentalResponse(RoomRental rental) {
        return new RoomRentalResponse(
                rental.getId(),
                rental.getProfessional().getId(),
                rental.getProfessional().getName(),
                rental.getProfessional().getSpecialty(),
                rental.getRoom().getId(),
                rental.getRoom().getName(),
                rental.getYearMonth(),
                rental.isActive()
        );
    }
}
