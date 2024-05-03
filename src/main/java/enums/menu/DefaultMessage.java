package enums.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DefaultMessage {
    INVALID("Invalid!"),
    ENTER_WITH("Enter with");

    private final String value;

}
