package utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrinterUtils {
    public static <T> void printElements(final List<T> list) {
        for (T t : list) System.out.printf("%d - %s \n", list.indexOf(t), t);
    }
}
