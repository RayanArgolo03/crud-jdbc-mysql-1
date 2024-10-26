package dtos.response;

import lombok.Builder;
import model.Employee;

import java.util.Objects;
import java.util.Set;

@Builder
public record DepartmentResponse(String name, String createdDate, String lastUpdate, Set<Employee> employees) {

    public DepartmentResponse {
        name = "Department " + name;
    }

    @Override
    public String toString() {
        return String.format("%s - Created at %s - Last updated  %s - Employees: %s",
                name,
                createdDate,
                (lastUpdate == null) ? "no has" : lastUpdate,
                (employees.isEmpty()) ? "no has" : employees);
    }
}


