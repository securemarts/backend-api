package com.shopper.common.util;

/**
 * Haversine distance between two points on Earth (in km).
 * Used for zone checks and delivery fee / nearest-rider logic.
 */
public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371;

    private GeoUtils() {}

    /**
     * Distance in km between (lat1, lon1) and (lat2, lon2).
     */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Null-safe version using BigDecimal coordinates (e.g. from entities).
     */
    public static double distanceKm(java.math.BigDecimal lat1, java.math.BigDecimal lon1,
                                   java.math.BigDecimal lat2, java.math.BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            throw new IllegalArgumentException("All coordinates must be non-null");
        }
        return distanceKm(lat1.doubleValue(), lon1.doubleValue(), lat2.doubleValue(), lon2.doubleValue());
    }
}
