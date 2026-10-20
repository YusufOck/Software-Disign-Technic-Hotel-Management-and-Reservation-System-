package com.sdt.hotel;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

enum BookingEventType { CREATED, CONFIRMED, PAYMENT_FAILED, CANCELLED, CHECKED_IN, COMPLETED }

record BookingEvent(UUID reservationId, UUID guestId, BookingEventType type, Instant occurredAt) {}

interface BookingObserver {
    void onBookingEvent(BookingEvent event);
}

// Observer subject. Additional channels can subscribe without changing BookingService.
@Component
final class BookingEventPublisher {
    private final List<BookingObserver> observers;

    BookingEventPublisher(List<BookingObserver> observers) {
        this.observers = List.copyOf(observers);
    }

    void publish(BookingEvent event) {
        observers.forEach(observer -> observer.onBookingEvent(event));
    }
}

@Component
final class AuditObserver implements BookingObserver {
    private final List<BookingEvent> events = new CopyOnWriteArrayList<>();

    public void onBookingEvent(BookingEvent event) { events.add(event); }
    List<BookingEvent> list() { return List.copyOf(events); }
}

record Notification(UUID id, UUID guestId, UUID reservationId, String message, Instant createdAt) {}

@Component
final class NotificationObserver implements BookingObserver {
    private final List<Notification> notifications = new CopyOnWriteArrayList<>();

    public void onBookingEvent(BookingEvent event) {
        String message = switch (event.type()) {
            case CREATED -> null;
            case CONFIRMED -> "Reservation confirmed";
            case PAYMENT_FAILED -> "Reservation payment failed";
            case CANCELLED -> "Reservation cancelled";
            case CHECKED_IN -> "Guest checked in";
            case COMPLETED -> "Stay completed";
        };
        if (message != null) {
            notifications.add(new Notification(UUID.randomUUID(), event.guestId(), event.reservationId(),
                    message, event.occurredAt()));
        }
    }

    List<Notification> list(UUID guestId) {
        return notifications.stream().filter(item -> guestId == null || item.guestId().equals(guestId)).toList();
    }
}
