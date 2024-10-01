package dtos.response;

import model.department.Department;
import model.department.Level;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Map;

public record EmployeeResponse(
        String name,
        String birthDate,
        Integer age,
        String document,
        Map<Department, Map<Level, BigDecimal>> departamentsAndLevelsAndSalaries,
        String createdDate,
        String lastUpdateDate,
        Integer workExperience,
        Boolean hasFaculty
) {

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();
        sb.append(name).append(" who was born ").append(birthDate).append("\n")
                .append(age).append(" years").append("\n")
                .append("With document ").append(document).append("\n");

        sb.append("Work informations: ").append("\n");
        for (Department d : departamentsAndLevelsAndSalaries.keySet()) {
            sb.append("Departament ").append(d);
            Level l = departamentsAndLevelsAndSalaries.get(d).keySet().stream().findFirst().get();
            sb.append("Seniority ").append(l.name());
            BigDecimal salary = departamentsAndLevelsAndSalaries.get(d).get(l);
            sb.append(" receiving ").append(NumberFormat.getCurrencyInstance().format(salary));
            sb.append("\n");
        }

        sb.append("Created date: ").append(createdDate).append("\n");
        sb.append("Last update date: ").append(lastUpdateDate).append("\n");

        if (workExperience != null) sb.append("Work experience: ").append(workExperience).append("\n");

        if (hasFaculty != null) {
            sb.append("Has faculty: ")
                    .append((hasFaculty == Boolean.TRUE) ? "Yes" : "No")
                    .append("\n");
        }

        return sb.toString();
    }


    public static final class EmployeeResponseBuilder {

        private String name;
        private String birthDate;
        private Integer age;
        private String document;
        private Map<Department, Map<Level, BigDecimal>> departamentsAndLevelsAndSalaries;
        private String createdDate;
        private String lastUpdateDate;
        private Integer workExperience;
        private Boolean hasFaculty;

        private EmployeeResponseBuilder() {
        }

        public static EmployeeResponseBuilder builder() {
            return new EmployeeResponseBuilder();
        }

        public EmployeeResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EmployeeResponseBuilder birthDate(String birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public EmployeeResponseBuilder age(Integer age) {
            this.age = age;
            return this;
        }

        public EmployeeResponseBuilder document(String document) {
            this.document = document;
            return this;
        }

        public EmployeeResponseBuilder departamentsAndLevelsAndSalaries(Map<Department, Map<Level, BigDecimal>> departamentsAndLevelsAndSalaries) {
            this.departamentsAndLevelsAndSalaries = departamentsAndLevelsAndSalaries;
            return this;
        }

        public EmployeeResponseBuilder createdDate(String createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public EmployeeResponseBuilder lastUpdateDate(String lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        public EmployeeResponseBuilder workExperience(Integer workExperience) {
            this.workExperience = workExperience;
            return this;
        }

        public EmployeeResponseBuilder hasFaculty(Boolean hasFaculty) {
            this.hasFaculty = hasFaculty;
            return this;
        }

        public EmployeeResponse build() {
            return new EmployeeResponse(name, birthDate, age, document, departamentsAndLevelsAndSalaries, createdDate, lastUpdateDate, workExperience, hasFaculty);
        }
    }
}
