package com.sdt.hotel;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// In-memory PoC store. BookingService synchronizes on this shared store for check + create.
@Component
final class HotelStore {
    final Map<UUID, RoomType> roomTypes = new ConcurrentHashMap<>();
    final Map<UUID, Room> rooms = new ConcurrentHashMap<>();
    final Map<UUID, Guest> guests = new ConcurrentHashMap<>();
    final Map<UUID, Reservation> reservations = new ConcurrentHashMap<>();
}
