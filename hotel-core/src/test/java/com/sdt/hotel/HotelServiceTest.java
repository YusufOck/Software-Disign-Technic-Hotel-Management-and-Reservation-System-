package com.sdt.hotel;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotelServiceTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);

    @Test
    void strategyAndDecoratorsCreateAuditablePriceBreakdown() {
        Fixture f = new Fixture();
        Reservation reservation = f.book(LocalDate.of(2026, 10, 23), LocalDate.of(2026, 10, 26),
                Set.of(Extra.BREAKFAST, Extra.PARKING), true);

        assertEquals(ReservationStatus.CONFIRMED, reservation.status());
        assertEquals("SEASONAL_WEEKEND", reservation.quote().strategy());
        assertEquals(new BigDecimal("425.00"), reservation.quote().total());
        assertEquals(3, reservation.quote().lines().size());
        assertEquals(2, f.audit.list().size());
        assertEquals(1, f.notifications.list(f.guest.id()).size());
    }

    @Test
    void overlappingStayIsRejectedButAdjacentStayIsAllowed() {
        Fixture f = new Fixture();
        LocalDate in = TODAY.plusDays(2);
        LocalDate out = TODAY.plusDays(4);
        f.book(in, out, Set.of(), true);

        assertFalse(f.booking.availableRooms(in.plusDays(1), out.plusDays(1), 1)
                .stream().anyMatch(room -> room.id().equals(f.room.id())));
        assertThrows(ConflictException.class, () -> f.book(in.plusDays(1), out, Set.of(), true));
        assertEquals(ReservationStatus.CONFIRMED, f.book(out, out.plusDays(1), Set.of(), true).status());
    }

    @Test
    void paymentFailureAndCancellationReleaseInventory() {
        Fixture f = new Fixture();
        LocalDate out = TODAY.plusDays(1);
        Reservation failed = f.book(TODAY, out, Set.of(), false);
        assertEquals(ReservationStatus.PAYMENT_FAILED, failed.status());
        Reservation confirmed = f.book(TODAY, out, Set.of(), true);
        assertThrows(IllegalTransitionException.class, () -> f.booking.cancel(failed.id()));
        f.booking.cancel(confirmed.id());
        assertEquals(ReservationStatus.CONFIRMED, f.book(TODAY, out, Set.of(), true).status());
    }

    @Test
    void stateTransitionsAllowCheckInAndCheckOutOnlyOnce() {
        Fixture f = new Fixture();
        Reservation reservation = f.book(TODAY, TODAY.plusDays(1), Set.of(), true);
        assertThrows(IllegalTransitionException.class, () -> f.booking.checkOut(reservation.id()));
        assertEquals(ReservationStatus.CHECKED_IN, f.booking.checkIn(reservation.id()).status());
        assertThrows(IllegalTransitionException.class, () -> f.booking.cancel(reservation.id()));
        assertEquals(ReservationStatus.COMPLETED, f.booking.checkOut(reservation.id()).status());
        assertThrows(IllegalTransitionException.class, () -> f.booking.checkOut(reservation.id()));
        assertEquals(4, f.audit.list().size());
        assertEquals(3, f.notifications.list(f.guest.id()).size());
    }

    @Test
    void concurrentRequestsHaveExactlyOneWinner() throws Exception {
        Fixture f = new Fixture();
        int candidates = 16;
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(candidates)) {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < candidates; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    try {
                        f.book(TODAY.plusDays(2), TODAY.plusDays(4), Set.of(), true);
                        return true;
                    } catch (ConflictException expected) {
                        return false;
                    }
                }));
            }
            start.countDown();
            int successes = 0;
            for (Future<Boolean> future : futures) if (future.get()) successes++;
            assertEquals(1, successes);
            assertEquals(1, f.booking.list(f.guest.id()).size());
        }
    }

    @Test
    void capacityAndOutOfServiceRoomAreNotReturned() {
        Fixture f = new Fixture();
        assertTrue(f.booking.availableRooms(TODAY, TODAY.plusDays(1), 1)
                .stream().anyMatch(room -> room.id().equals(f.room.id())));
        assertFalse(f.booking.availableRooms(TODAY, TODAY.plusDays(1), 3)
                .stream().anyMatch(room -> room.id().equals(f.room.id())));
        f.catalog.updateRoomStatus(f.room.id(), RoomStatus.OUT_OF_SERVICE);
        assertTrue(f.booking.availableRooms(TODAY, TODAY.plusDays(1), 1).isEmpty());
    }

    private static final class Fixture {
        final HotelStore store = new HotelStore();
        final CatalogService catalog = new CatalogService(store);
        final AuditObserver audit = new AuditObserver();
        final NotificationObserver notifications = new NotificationObserver();
        final BookingEventPublisher publisher = new BookingEventPublisher(List.of(audit, notifications));
        final Clock clock = Clock.fixed(Instant.parse("2026-10-20T09:00:00Z"), ZoneOffset.UTC);
        final BookingService booking = new BookingService(store, catalog, new PricingService(), publisher, clock);
        final RoomType type = catalog.createRoomType("Double", new BigDecimal("100.00"), 2);
        final Room room = catalog.createRoom("101", type.id());
        final Guest guest = catalog.createGuest("Ada Guest", "ada@example.com", "Quiet room");

        Reservation book(LocalDate in, LocalDate out, Set<Extra> extras, boolean paymentSuccess) {
            return booking.create(new CreateReservationCommand(guest.id(), room.id(), in, out, extras, paymentSuccess));
        }
    }
}
