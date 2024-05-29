package utils;

import enums.menu.DefaultMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReaderUtils {
    private static final Scanner sc = new Scanner(System.in);

    public static <T> T readElement(final String title, final List<T> list) {

        PrintEnumsUtils.printElements(list);
        int choose = readInt(title);

        return list.stream()
                .filter(e -> list.indexOf(e) == choose)
                .findFirst()
                .orElseThrow(InputMismatchException::new);
    }

    public static int readInt(final String title) {
        System.out.printf("%s %s: \n", DefaultMessage.ENTER_WITH.getValue(), title);
        return sc.nextInt();
    }

    public static Long readLong(final String title) {
        System.out.printf("%s %s: \n", DefaultMessage.ENTER_WITH.getValue(), title);
        return sc.nextLong();
    }

    public static String readString(final String title) {
        System.out.printf("%s %s: \n", DefaultMessage.ENTER_WITH.getValue(), title);
        return sc.next();
    }

}
