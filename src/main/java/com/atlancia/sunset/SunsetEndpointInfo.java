package com.atlancia.sunset;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record SunsetEndpointInfo(
        String endpoint,
        LocalDate sunsetDate,
        LocalDate sinceDate,
        String replacement,
        String reason
) {

    public boolean isPastSunset() {
        return !LocalDate.now().isBefore(sunsetDate);
    }

    public long daysUntilSunset() {
        return ChronoUnit.DAYS.between(LocalDate.now(), sunsetDate);
    }

    public String phase() {
        return isPastSunset() ? "SUNSET" : "DEPRECATED";
    }
}
