package utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class FormatterUtils {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getCurrencyInstance();

    private FormatterUtils() {
    }

    public static String formatName(final String name) {
        return name.substring(0, 1).toUpperCase().concat(name.substring(1).toLowerCase());
    }

    public static String formatTemporalToString(final Temporal temporal) {

        final String pattern = (temporal instanceof LocalDateTime)
                ? "dd/MM/uuuu HH:mm"
                : (temporal instanceof LocalDate)
                ? "dd/MM/uuuu"
                : "HH:mm"; //LocalTime pattern

        return DateTimeFormatter.ofPattern(pattern)
                .withResolverStyle(ResolverStyle.STRICT)
                .format(temporal);
    }

    public static <T extends TemporalAccessor> T formatStringToTemporal(final String value, final String pattern, final TemporalQuery<T> query) {

        return DateTimeFormatter.ofPattern(pattern)
                .withResolverStyle(ResolverStyle.STRICT)
                .parse(value, query);
    }

    public static String formatSalary(final BigDecimal salary) {
        return NUMBER_FORMAT.format(salary);
    }
}
