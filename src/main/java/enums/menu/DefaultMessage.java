package enums.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DefaultMessage {
    ENTER_WITH("Enter with"),
    INVALID("Invalid! ");
    private final String value;

}
