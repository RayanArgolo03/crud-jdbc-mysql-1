package dtos.response;

import model.Employee;

import java.util.Set;

public record DepartmentResponse(String name, String createdDate, String lastUpdate, Set<Employee> employees) {

    public DepartmentResponse {
        name = "Department" + name;
    }

    @Override
    public String toString() {
        return String.format("%s - Created at %s - Last updated in %s - Employees: %s",
                name,
                createdDate,
                lastUpdate,
                (!employees.isEmpty()) ? employees : "No has");
    }
}


