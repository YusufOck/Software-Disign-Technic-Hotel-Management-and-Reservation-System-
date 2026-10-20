package com.sdt.hotel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

record RoomType(UUID id, String name, BigDecimal basePrice, int capacity) {}

enum RoomStatus { ACTIVE, OUT_OF_SERVICE }

record Room(UUID id, String number, UUID roomTypeId, RoomStatus status) {
    Room withStatus(RoomStatus newStatus) {
        return new Room(id, number, roomTypeId, newStatus);
    }
}

record Guest(UUID id, String name, String email, String preferences) {}

enum Extra { BREAKFAST, PARKING }

final class Reservation {
    private final UUID id;
    private final UUID guestId;
    private final UUID roomId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final Set<Extra> extras;
    private final Quote quote;
    private final Instant createdAt;
    private volatile ReservationState state = PendingPaymentState.INSTANCE;

    Reservation(UUID id, UUID guestId, UUID roomId, LocalDate checkIn, LocalDate checkOut,
                Set<Extra> extras, Quote quote, Instant createdAt) {
        this.id = id;
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.extras = Set.copyOf(extras);
        this.quote = quote;
        this.createdAt = createdAt;
    }

    UUID id() { return id; }
    UUID guestId() { return guestId; }
    UUID roomId() { return roomId; }
    LocalDate checkIn() { return checkIn; }
    LocalDate checkOut() { return checkOut; }
    Set<Extra> extras() { return extras; }
    Quote quote() { return quote; }
    Instant createdAt() { return createdAt; }
    ReservationStatus status() { return state.status(); }

    void confirm() { state = state.confirm(); }
    void failPayment() { state = state.failPayment(); }
    void cancel() { state = state.cancel(); }
    void checkInGuest() { state = state.checkIn(); }
    void checkOutGuest() { state = state.checkOut(); }

    boolean blocks(LocalDate from, LocalDate to) {
        boolean active = status() == ReservationStatus.PENDING_PAYMENT
                || status() == ReservationStatus.CONFIRMED
                || status() == ReservationStatus.CHECKED_IN;
        // Stay intervals are [checkIn, checkOut), so same-day turnover is allowed.
        return active && checkIn.isBefore(to) && from.isBefore(checkOut);
    }
}
