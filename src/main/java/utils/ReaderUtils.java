package utils;

import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public final class ReaderUtils {

    private static final Scanner SCANNER = new Scanner(System.in);

    private ReaderUtils() {
    }

    public static <T extends Enum<T>> T readEnum(final String title, final Class<T> enumClass) {

        final T[] enumConstants = enumClass.getEnumConstants();
        for (T enumm : enumConstants) System.out.printf("%d - %s \n", enumm.ordinal() + 1, enumm);

        int option = readInt(title);

        //Index in enum array, choose is ordinal + 1
        return enumConstants[option - 1];
    }

    public static <T> T readElement(final String title, final List<T> elements) {

        for (T e : elements) System.out.printf("%d - %s \n", elements.indexOf(e) + 1, e);
        int option = readInt(title);

        //Index in list, choose is index of + 1
        return elements.get(option - 1);
    }

    public static int readInt(final String title) {
        System.out.printf("Enter with %s: ", title);
        return SCANNER.nextInt();
    }

    public static String readString(final String title) {
        System.out.printf("Enter with %s: ", title);
        return SCANNER.next();
    }

}
