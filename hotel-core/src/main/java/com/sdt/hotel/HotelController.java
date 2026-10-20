package com.sdt.hotel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class HotelController {
    private final CatalogService catalog;
    private final BookingService booking;
    private final NotificationObserver notifications;
    private final AuditObserver audit;

    HotelController(CatalogService catalog, BookingService booking,
                    NotificationObserver notifications, AuditObserver audit) {
        this.catalog = catalog;
        this.booking = booking;
        this.notifications = notifications;
        this.audit = audit;
    }

    public record CreateRoomTypeRequest(@NotBlank String name,
                                        @NotNull @DecimalMin("0.01") BigDecimal basePrice,
                                        @Positive int capacity) {}
    public record RoomTypeResponse(UUID id, String name, BigDecimal basePrice, int capacity) {}
    public record CreateRoomRequest(@NotBlank String number, @NotNull UUID roomTypeId) {}
    public record RoomStatusRequest(@NotNull RoomStatus status) {}
    public record RoomResponse(UUID id, String number, UUID roomTypeId, String roomTypeName,
                               int capacity, BigDecimal basePrice, RoomStatus status) {}
    public record CreateGuestRequest(@NotBlank String name, @NotBlank @Email String email, String preferences) {}
    public record GuestResponse(UUID id, String name, String email, String preferences) {}
    public record CreateReservationRequest(@NotNull UUID guestId, @NotNull UUID roomId,
                                           @NotNull LocalDate checkIn, @NotNull LocalDate checkOut,
                                           Set<Extra> extras, Boolean simulatePaymentSuccess) {}
    public record PriceLineResponse(String description, BigDecimal amount) {}
    public record ReservationResponse(UUID id, UUID guestId, UUID roomId, LocalDate checkIn, LocalDate checkOut,
                                      Set<Extra> extras, ReservationStatus status, BigDecimal totalPrice,
                                      String pricingStrategy, List<PriceLineResponse> priceLines, Instant createdAt) {}

    @PostMapping("/room-types")
    ResponseEntity<RoomTypeResponse> createRoomType(@Valid @RequestBody CreateRoomTypeRequest request) {
        RoomType type = catalog.createRoomType(request.name(), request.basePrice(), request.capacity());
        return ResponseEntity.created(URI.create("/api/room-types/" + type.id())).body(toResponse(type));
    }

    @GetMapping("/room-types")
    List<RoomTypeResponse> roomTypes() { return catalog.roomTypes().stream().map(this::toResponse).toList(); }

    @PostMapping("/rooms")
    ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        Room room = catalog.createRoom(request.number(), request.roomTypeId());
        return ResponseEntity.created(URI.create("/api/rooms/" + room.id())).body(toResponse(room));
    }

    @GetMapping("/rooms")
    List<RoomResponse> rooms() { return catalog.rooms().stream().map(this::toResponse).toList(); }

    @PatchMapping("/rooms/{id}/status")
    RoomResponse updateRoomStatus(@PathVariable UUID id, @Valid @RequestBody RoomStatusRequest request) {
        return toResponse(catalog.updateRoomStatus(id, request.status()));
    }

    @GetMapping("/rooms/availability")
    List<RoomResponse> availability(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                                    @RequestParam(defaultValue = "1") int capacity) {
        return booking.availableRooms(checkIn, checkOut, capacity).stream().map(this::toResponse).toList();
    }

    @PostMapping("/guests")
    ResponseEntity<GuestResponse> createGuest(@Valid @RequestBody CreateGuestRequest request) {
        Guest guest = catalog.createGuest(request.name(), request.email(), request.preferences());
        return ResponseEntity.created(URI.create("/api/guests/" + guest.id())).body(toResponse(guest));
    }

    @GetMapping("/guests")
    List<GuestResponse> guests() { return catalog.guests().stream().map(this::toResponse).toList(); }

    @PostMapping("/reservations")
    ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        CreateReservationCommand command = new CreateReservationCommand(request.guestId(), request.roomId(),
                request.checkIn(), request.checkOut(), request.extras() == null ? Set.of() : request.extras(),
                request.simulatePaymentSuccess() == null || request.simulatePaymentSuccess());
        Reservation reservation = booking.create(command);
        return ResponseEntity.created(URI.create("/api/reservations/" + reservation.id()))
                .body(toResponse(reservation));
    }

    @GetMapping("/reservations/{id}")
    ReservationResponse reservation(@PathVariable UUID id) { return toResponse(booking.get(id)); }

    @GetMapping("/reservations")
    List<ReservationResponse> reservations(@RequestParam(required = false) UUID guestId) {
        return booking.list(guestId).stream().map(this::toResponse).toList();
    }

    @PostMapping("/reservations/{id}/cancel")
    ReservationResponse cancel(@PathVariable UUID id) { return toResponse(booking.cancel(id)); }

    @PostMapping("/reservations/{id}/check-in")
    ReservationResponse checkIn(@PathVariable UUID id) { return toResponse(booking.checkIn(id)); }

    @PostMapping("/reservations/{id}/check-out")
    ReservationResponse checkOut(@PathVariable UUID id) { return toResponse(booking.checkOut(id)); }

    @GetMapping("/notifications")
    List<Notification> notifications(@RequestParam(required = false) UUID guestId) {
        return notifications.list(guestId);
    }

    @GetMapping("/audit")
    List<BookingEvent> audit() { return audit.list(); }

    private RoomTypeResponse toResponse(RoomType type) {
        return new RoomTypeResponse(type.id(), type.name(), type.basePrice(), type.capacity());
    }

    private RoomResponse toResponse(Room room) {
        RoomType type = catalog.roomType(room.roomTypeId());
        return new RoomResponse(room.id(), room.number(), type.id(), type.name(), type.capacity(),
                type.basePrice(), room.status());
    }

    private GuestResponse toResponse(Guest guest) {
        return new GuestResponse(guest.id(), guest.name(), guest.email(), guest.preferences());
    }

    private ReservationResponse toResponse(Reservation reservation) {
        Quote quote = reservation.quote();
        return new ReservationResponse(reservation.id(), reservation.guestId(), reservation.roomId(),
                reservation.checkIn(), reservation.checkOut(), reservation.extras(), reservation.status(),
                quote.total(), quote.strategy(), quote.lines().stream()
                .map(line -> new PriceLineResponse(line.description(), line.amount())).toList(), reservation.createdAt());
    }
}
