package com.sdt.hotel;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
final class CatalogService {
    private final HotelStore store;

    CatalogService(HotelStore store) { this.store = store; }

    RoomType createRoomType(String name, BigDecimal basePrice, int capacity) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Room type name is required");
        if (basePrice == null || basePrice.signum() <= 0) throw new IllegalArgumentException("Base price must be positive");
        if (basePrice.scale() > 2) throw new IllegalArgumentException("Base price supports at most two decimal places");
        if (capacity < 1) throw new IllegalArgumentException("Capacity must be at least one");
        synchronized (store) {
            if (store.roomTypes.values().stream().anyMatch(type -> type.name().equalsIgnoreCase(name.trim()))) {
                throw new ConflictException("Room type name already exists");
            }
            RoomType type = new RoomType(UUID.randomUUID(), name.trim(), basePrice.setScale(2), capacity);
            store.roomTypes.put(type.id(), type);
            return type;
        }
    }

    Room createRoom(String number, UUID roomTypeId) {
        if (number == null || number.isBlank()) throw new IllegalArgumentException("Room number is required");
        synchronized (store) {
            roomType(roomTypeId);
            if (store.rooms.values().stream().anyMatch(room -> room.number().equalsIgnoreCase(number.trim()))) {
                throw new ConflictException("Room number already exists");
            }
            Room room = new Room(UUID.randomUUID(), number.trim(), roomTypeId, RoomStatus.ACTIVE);
            store.rooms.put(room.id(), room);
            return room;
        }
    }

    Room updateRoomStatus(UUID id, RoomStatus status) {
        if (status == null) throw new IllegalArgumentException("Room status is required");
        synchronized (store) {
            Room changed = room(id).withStatus(status);
            store.rooms.put(id, changed);
            return changed;
        }
    }

    Guest createGuest(String name, String email, String preferences) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Guest name is required");
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Valid guest email is required");
        }
        synchronized (store) {
            if (store.guests.values().stream().anyMatch(guest -> guest.email().equalsIgnoreCase(email.trim()))) {
                throw new ConflictException("Guest email already exists");
            }
            Guest guest = new Guest(UUID.randomUUID(), name.trim(), email.trim(), preferences == null ? "" : preferences);
            store.guests.put(guest.id(), guest);
            return guest;
        }
    }

    RoomType roomType(UUID id) {
        RoomType type = store.roomTypes.get(id);
        if (type == null) throw new NoSuchElementException("Room type not found: " + id);
        return type;
    }

    Room room(UUID id) {
        Room room = store.rooms.get(id);
        if (room == null) throw new NoSuchElementException("Room not found: " + id);
        return room;
    }

    Guest guest(UUID id) {
        Guest guest = store.guests.get(id);
        if (guest == null) throw new NoSuchElementException("Guest not found: " + id);
        return guest;
    }

    List<RoomType> roomTypes() { return store.roomTypes.values().stream().sorted(Comparator.comparing(RoomType::name)).toList(); }
    List<Room> rooms() { return store.rooms.values().stream().sorted(Comparator.comparing(Room::number)).toList(); }
    List<Guest> guests() { return store.guests.values().stream().sorted(Comparator.comparing(Guest::name)).toList(); }
}

record CreateReservationCommand(UUID guestId, UUID roomId, LocalDate checkIn, LocalDate checkOut,
                                Set<Extra> extras, boolean simulatePaymentSuccess) {}

@Service
final class BookingService {
    private final HotelStore store;
    private final CatalogService catalog;
    private final PricingService pricing;
    private final BookingEventPublisher events;
    private final Clock clock;

    BookingService(HotelStore store, CatalogService catalog, PricingService pricing,
                   BookingEventPublisher events, Clock clock) {
        this.store = store;
        this.catalog = catalog;
        this.pricing = pricing;
        this.events = events;
        this.clock = clock;
    }

    List<Room> availableRooms(LocalDate in, LocalDate out, int capacity) {
        validateDates(in, out);
        if (capacity < 1) throw new IllegalArgumentException("Capacity must be at least one");
        synchronized (store) {
            return store.rooms.values().stream()
                    .filter(room -> room.status() == RoomStatus.ACTIVE)
                    .filter(room -> catalog.roomType(room.roomTypeId()).capacity() >= capacity)
                    .filter(room -> isAvailable(room.id(), in, out))
                    .sorted(Comparator.comparing(Room::number)).toList();
        }
    }

    Reservation create(CreateReservationCommand command) {
        if (command == null) throw new IllegalArgumentException("Reservation request is required");
        validateDates(command.checkIn(), command.checkOut());
        if (command.checkIn().isBefore(LocalDate.now(clock))) {
            throw new IllegalArgumentException("Check-in cannot be in the past");
        }
        if (command.extras() == null) throw new IllegalArgumentException("Extras are required");
        synchronized (store) {
            catalog.guest(command.guestId());
            Room room = catalog.room(command.roomId());
            if (room.status() != RoomStatus.ACTIVE) throw new ConflictException("Room is out of service");
            if (!isAvailable(room.id(), command.checkIn(), command.checkOut())) {
                throw new ConflictException("Room is unavailable for these dates");
            }
            RoomType type = catalog.roomType(room.roomTypeId());
            Quote quote = pricing.quote(type, command.checkIn(), command.checkOut(), command.extras());
            Reservation reservation = new Reservation(UUID.randomUUID(), command.guestId(), room.id(),
                    command.checkIn(), command.checkOut(), command.extras(), quote, Instant.now(clock));
            store.reservations.put(reservation.id(), reservation);
            publish(reservation, BookingEventType.CREATED);
            if (command.simulatePaymentSuccess()) {
                reservation.confirm();
                publish(reservation, BookingEventType.CONFIRMED);
            } else {
                reservation.failPayment();
                publish(reservation, BookingEventType.PAYMENT_FAILED);
            }
            return reservation;
        }
    }

    Reservation cancel(UUID id) {
        synchronized (store) {
            Reservation reservation = get(id);
            reservation.cancel();
            publish(reservation, BookingEventType.CANCELLED);
            return reservation;
        }
    }

    Reservation checkIn(UUID id) {
        synchronized (store) {
            Reservation reservation = get(id);
            LocalDate today = LocalDate.now(clock);
            if (today.isBefore(reservation.checkIn()) || !today.isBefore(reservation.checkOut())) {
                throw new ConflictException("Check-in must occur during the reserved stay");
            }
            reservation.checkInGuest();
            publish(reservation, BookingEventType.CHECKED_IN);
            return reservation;
        }
    }

    Reservation checkOut(UUID id) {
        synchronized (store) {
            Reservation reservation = get(id);
            reservation.checkOutGuest();
            publish(reservation, BookingEventType.COMPLETED);
            return reservation;
        }
    }

    Reservation get(UUID id) {
        Reservation reservation = store.reservations.get(id);
        if (reservation == null) throw new NoSuchElementException("Reservation not found: " + id);
        return reservation;
    }

    List<Reservation> list(UUID guestId) {
        synchronized (store) {
            if (guestId != null) catalog.guest(guestId);
            return store.reservations.values().stream()
                    .filter(item -> guestId == null || item.guestId().equals(guestId))
                    .sorted(Comparator.comparing(Reservation::createdAt)).toList();
        }
    }

    private boolean isAvailable(UUID roomId, LocalDate in, LocalDate out) {
        return store.reservations.values().stream()
                .noneMatch(reservation -> reservation.roomId().equals(roomId) && reservation.blocks(in, out));
    }

    private void publish(Reservation reservation, BookingEventType type) {
        events.publish(new BookingEvent(reservation.id(), reservation.guestId(), type, Instant.now(clock)));
    }

    private void validateDates(LocalDate in, LocalDate out) {
        if (in == null || out == null || !out.isAfter(in)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
    }
}

final class ConflictException extends RuntimeException {
    ConflictException(String message) { super(message); }
}
