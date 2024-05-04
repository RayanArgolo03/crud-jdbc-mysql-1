package utilities;

import enums.menu.DefaultMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Scanner;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReaderUtil {
    private static final Scanner sc = new Scanner(System.in);

    public static <T extends Enum<T>> T readEnum(final List<T> availableEnums) {

        System.out.printf("%s %s: \n", DefaultMessage.ENTER_WITH.getValue(), "your choice");
        PrintEnumsUtil.printEnums(availableEnums);
        int choice;

        //Modifyning choice input, comparision and assignment in one line
        while (!validChoice(choice = readInt(), availableEnums.size())) {
            System.out.println(DefaultMessage.INVALID.getValue());
            System.out.printf("%s %s: \n", DefaultMessage.ENTER_WITH.getValue(), "your choice");
            PrintEnumsUtil.printEnums(availableEnums);
        }

        return availableEnums.get(choice);
    }

    public static int readInt() {
        return sc.nextInt();
    }

    public static Long readLong() {
        return sc.nextLong();
    }

    public static String readString(final String title) {
        System.out.printf("%s %s: \n", DefaultMessage.ENTER_WITH.getValue(), title);
        return sc.next();
    }

    public static boolean validChoice(final int choice, final int total) {
        return choice > -1 && choice <= total;
    }

}
