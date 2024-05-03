package utilities;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrintEnumsUtil {
    public static <T extends Enum<T>> void printEnums(final List<T> list) {
        for (T enumm : list) System.out.printf("%d - %s \n", list.indexOf(enumm), enumm);
    }
}
