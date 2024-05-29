package utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrintEnumsUtils {
    public static <T> void printElements(final List<T> list) {
        for (T enumm : list) System.out.printf("%d - %s \n", list.indexOf(enumm), enumm);
    }
}
