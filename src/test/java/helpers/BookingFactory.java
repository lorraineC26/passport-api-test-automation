package helpers;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds booking request payloads for tests. Centralizing the shape here keeps
 * test methods focused on what they assert rather than on constructing the body.
 */
public final class BookingFactory {

    private static final int DEFAULT_TOTAL_PRICE = 150;
    private static final String DEFAULT_CHECKIN = "2025-01-01";
    private static final String DEFAULT_CHECKOUT = "2025-01-05";

    private BookingFactory() {
    }

    public static Map<String, Object> booking(String firstName, String lastName) {
        return booking(firstName, lastName, DEFAULT_TOTAL_PRICE);
    }

    public static Map<String, Object> booking(String firstName, String lastName, int totalPrice) {
        Map<String, Object> dates = new HashMap<>();
        dates.put("checkin", DEFAULT_CHECKIN);
        dates.put("checkout", DEFAULT_CHECKOUT);

        Map<String, Object> booking = new HashMap<>();
        booking.put("firstname", firstName);
        booking.put("lastname", lastName);
        booking.put("totalprice", totalPrice);
        booking.put("depositpaid", true);
        booking.put("bookingdates", dates);
        booking.put("additionalneeds", "Breakfast");
        return booking;
    }
}
