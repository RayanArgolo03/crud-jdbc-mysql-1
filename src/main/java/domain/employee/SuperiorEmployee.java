package domain.employee;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.net.Inet4Address;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@SuperBuilder
@Getter
@Setter
public final class SuperiorEmployee extends Employee {

    private int workExperience;

    @Override
    public String toString() {
        return super.toString() + "Work experience: " + workExperience + " years\n";
    }
}
