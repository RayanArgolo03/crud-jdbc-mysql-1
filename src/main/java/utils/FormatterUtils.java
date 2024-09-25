package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.Temporal;
import java.util.function.Function;

public final class FormatterUtils {


    private FormatterUtils() {
    }

    public static String formatName(final String name) {
        return name.substring(0, 1).toUpperCase().concat(name.substring(1).toLowerCase());
    }

    public static String formatMoney(final String moneyInString) {
        return moneyInString.replace(",", ".");
    }

    public static String formatDate(final Temporal temporal) {

        final String pattern = (temporal instanceof LocalDateTime)
                ? "dd/MM/uuuu HH:mm"
                : "dd/MM/uuuu";

        return DateTimeFormatter.ofPattern(pattern)
                .withResolverStyle(ResolverStyle.STRICT)
                .format(temporal);
    }
}
