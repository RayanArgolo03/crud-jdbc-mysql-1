package dtos;

public record DepartmentResponse(String departmentName, String createdDate, String lastUpdateDate) {

    public DepartmentResponse {
        departmentName = "Department" + departmentName;
    }

    @Override
    public String toString() {
        return String.format("%s - Created at %s - Last updated in %s", departmentName, createdDate, lastUpdateDate);
    }
}


