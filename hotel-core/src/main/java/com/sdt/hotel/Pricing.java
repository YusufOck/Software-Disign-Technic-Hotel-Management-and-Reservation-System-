package com.sdt.hotel;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

record PriceLine(String description, BigDecimal amount) {}

record Quote(BigDecimal total, String strategy, List<PriceLine> lines) {
    Quote {
        lines = List.copyOf(lines);
    }
}

// Strategy: the room type supplies the rate; the stay period chooses the algorithm.
interface PricingStrategy {
    BigDecimal calculate(RoomType roomType, LocalDate checkIn, LocalDate checkOut);
    String name();
}

final class StandardPricingStrategy implements PricingStrategy {
    public BigDecimal calculate(RoomType type, LocalDate in, LocalDate out) {
        return type.basePrice().multiply(BigDecimal.valueOf(ChronoUnit.DAYS.between(in, out)));
    }
    public String name() { return "STANDARD"; }
}

final class SeasonalPricingStrategy implements PricingStrategy {
    private static final BigDecimal WEEKEND_MULTIPLIER = new BigDecimal("1.25");
    public BigDecimal calculate(RoomType type, LocalDate in, LocalDate out) {
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate day = in; day.isBefore(out); day = day.plusDays(1)) {
            boolean weekend = day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;
            total = total.add(weekend ? type.basePrice().multiply(WEEKEND_MULTIPLIER) : type.basePrice());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    public String name() { return "SEASONAL_WEEKEND"; }
}

// Decorator: extras wrap the same price interface and add their own line items.
interface PriceComponent {
    BigDecimal amount();
    List<PriceLine> lines();
}

final class BaseRoomPrice implements PriceComponent {
    private final BigDecimal amount;
    BaseRoomPrice(BigDecimal amount) { this.amount = amount; }
    public BigDecimal amount() { return amount; }
    public List<PriceLine> lines() { return List.of(new PriceLine("Room stay", amount)); }
}

abstract class PriceDecorator implements PriceComponent {
    private final PriceComponent delegate;
    private final BigDecimal surcharge;
    private final String description;

    PriceDecorator(PriceComponent delegate, String description, BigDecimal perNight, long nights) {
        this.delegate = delegate;
        this.description = description;
        this.surcharge = perNight.multiply(BigDecimal.valueOf(nights));
    }

    public BigDecimal amount() { return delegate.amount().add(surcharge); }
    public List<PriceLine> lines() {
        List<PriceLine> result = new ArrayList<>(delegate.lines());
        result.add(new PriceLine(description, surcharge));
        return List.copyOf(result);
    }
}

final class BreakfastDecorator extends PriceDecorator {
    BreakfastDecorator(PriceComponent delegate, long nights) {
        super(delegate, "Breakfast", new BigDecimal("15.00"), nights);
    }
}

final class ParkingDecorator extends PriceDecorator {
    ParkingDecorator(PriceComponent delegate, long nights) {
        super(delegate, "Parking", new BigDecimal("10.00"), nights);
    }
}

@Service
final class PricingService {
    private final PricingStrategy standard = new StandardPricingStrategy();
    private final PricingStrategy seasonal = new SeasonalPricingStrategy();

    Quote quote(RoomType type, LocalDate in, LocalDate out, Set<Extra> extras) {
        boolean hasWeekend = in.datesUntil(out).anyMatch(day -> day.getDayOfWeek() == DayOfWeek.SATURDAY
                || day.getDayOfWeek() == DayOfWeek.SUNDAY);
        PricingStrategy strategy = hasWeekend ? seasonal : standard;
        long nights = ChronoUnit.DAYS.between(in, out);
        PriceComponent price = new BaseRoomPrice(strategy.calculate(type, in, out));
        if (extras.contains(Extra.BREAKFAST)) price = new BreakfastDecorator(price, nights);
        if (extras.contains(Extra.PARKING)) price = new ParkingDecorator(price, nights);
        return new Quote(price.amount(), strategy.name(), price.lines());
    }
}
