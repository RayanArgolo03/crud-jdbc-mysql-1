package domain.employee;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class NormalEmployee extends Employee {

    private boolean hasFaculty;
    @Override
    public String toString() {
        String str = (hasFaculty) ? "Has faculty!" : "No has faculty :(";
        return super.toString() + str + "\n";
    }



}
