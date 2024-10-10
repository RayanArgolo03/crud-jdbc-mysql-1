package criteria;

import enums.menu.YesOrNo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.stream.Stream;

@Getter
@Setter
public final class EmployeeFilter {

    String departmentName, employeeName, document;
    Integer employeeAge, workExperience;
    LocalDate birthDate, hireDate;
    LocalTime hireTime;
    Boolean hasFaculty;

    public EmployeeFilter() {
    }

    public Boolean hasFaculty() {
        return hasFaculty == Boolean.TRUE;
    }

    public void setHasFaculty(YesOrNo yesOrNo) {
        this.hasFaculty = (yesOrNo == YesOrNo.YES)
                ? Boolean.TRUE
                : Boolean.FALSE;
    }

    public boolean hasFilters() {
        return Stream.of(departmentName, employeeName, document,
                        employeeAge, workExperience, birthDate,
                        hireDate, hireTime, hasFaculty)
                .anyMatch(Objects::nonNull);
    }
}

