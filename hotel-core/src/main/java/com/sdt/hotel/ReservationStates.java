package com.sdt.hotel;

enum ReservationStatus {
    PENDING_PAYMENT, CONFIRMED, CHECKED_IN, COMPLETED, CANCELLED, PAYMENT_FAILED
}

interface ReservationState {
    ReservationStatus status();

    default ReservationState confirm() { throw invalid("confirm"); }
    default ReservationState failPayment() { throw invalid("fail payment"); }
    default ReservationState cancel() { throw invalid("cancel"); }
    default ReservationState checkIn() { throw invalid("check in"); }
    default ReservationState checkOut() { throw invalid("check out"); }

    private IllegalTransitionException invalid(String operation) {
        return new IllegalTransitionException("Cannot " + operation + " reservation in " + status() + " state");
    }
}

final class IllegalTransitionException extends RuntimeException {
    IllegalTransitionException(String message) { super(message); }
}

final class PendingPaymentState implements ReservationState {
    static final PendingPaymentState INSTANCE = new PendingPaymentState();
    private PendingPaymentState() {}
    public ReservationStatus status() { return ReservationStatus.PENDING_PAYMENT; }
    public ReservationState confirm() { return ConfirmedState.INSTANCE; }
    public ReservationState failPayment() { return PaymentFailedState.INSTANCE; }
}

final class ConfirmedState implements ReservationState {
    static final ConfirmedState INSTANCE = new ConfirmedState();
    private ConfirmedState() {}
    public ReservationStatus status() { return ReservationStatus.CONFIRMED; }
    public ReservationState cancel() { return CancelledState.INSTANCE; }
    public ReservationState checkIn() { return CheckedInState.INSTANCE; }
}

final class CheckedInState implements ReservationState {
    static final CheckedInState INSTANCE = new CheckedInState();
    private CheckedInState() {}
    public ReservationStatus status() { return ReservationStatus.CHECKED_IN; }
    public ReservationState checkOut() { return CompletedState.INSTANCE; }
}

final class CompletedState implements ReservationState {
    static final CompletedState INSTANCE = new CompletedState();
    private CompletedState() {}
    public ReservationStatus status() { return ReservationStatus.COMPLETED; }
}

final class CancelledState implements ReservationState {
    static final CancelledState INSTANCE = new CancelledState();
    private CancelledState() {}
    public ReservationStatus status() { return ReservationStatus.CANCELLED; }
}

final class PaymentFailedState implements ReservationState {
    static final PaymentFailedState INSTANCE = new PaymentFailedState();
    private PaymentFailedState() {}
    public ReservationStatus status() { return ReservationStatus.PAYMENT_FAILED; }
}
