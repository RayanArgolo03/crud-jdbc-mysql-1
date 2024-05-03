package utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FormatterUtil {
    public static String formatName(final String name) {return name.substring(0, 1).toUpperCase().concat(name.substring(1));}
    public static String formatMoney(final String moneyInString) {
        return moneyInString.replace(",", ".");
    }
}
