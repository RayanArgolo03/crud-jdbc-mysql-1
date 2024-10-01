package utils;

import java.util.Scanner;

public final class ReaderUtils {

    private static final Scanner SCANNER = new Scanner(System.in);

    private ReaderUtils() {}

    public static <T extends Enum<T>> T readEnum(final String title, final Class<T> enumClass) {

        final T[] enumConstants = enumClass.getEnumConstants();

        System.out.printf("Enter with %s:\n", title);

        for (T enumm : enumConstants) System.out.printf("%d - %s \n", enumm.ordinal() + 1, enumm);

        int option = readInt();

        //Index in enum array, choose is ordinal + 1
        return enumConstants[option - 1];
    }

    public static int readInt(final String title) {
        System.out.printf("Enter with %s:\n", title);
        return SCANNER.nextInt();
    }

    public static String readString(final String title) {
        System.out.printf("Enter with %s:\n", title);
        return SCANNER.next();
    }

}
