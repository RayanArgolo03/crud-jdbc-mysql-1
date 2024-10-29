package criteria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.stream.Stream;

public final class DepartmentFilter {

    String departmentName, employeeName;
    Integer employeeAge;
    LocalDate creationDate, employeeHireDate;
    LocalDateTime lastUpdateDate;
    LocalTime lastUpdateTime;

    public DepartmentFilter() {
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Integer getEmployeeAge() {
        return employeeAge;
    }

    public void setEmployeeAge(Integer employeeAge) {
        this.employeeAge = employeeAge;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDate getEmployeeHireDate() {
        return employeeHireDate;
    }

    public void setEmployeeHireDate(LocalDate employeeHireDate) {
        this.employeeHireDate = employeeHireDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public LocalTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean hasFilters() {
        return Stream.of(departmentName, employeeName, creationDate,
                        lastUpdateDate, employeeHireDate, lastUpdateTime,
                        employeeAge)
                .anyMatch(Objects::nonNull);
    }
}

