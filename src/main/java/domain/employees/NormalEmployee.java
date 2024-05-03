package domain.employees;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class NormalEmployee extends Employee {
    private boolean hasFaculty;
    public void setHasFaculty() {
        this.hasFaculty = true;
    }
    @Override
    public String toString() {
        String str = (hasFaculty) ? "Has faculty!" : "No has faculty :(";
        return super.toString() + str + "\n";
    }

}
