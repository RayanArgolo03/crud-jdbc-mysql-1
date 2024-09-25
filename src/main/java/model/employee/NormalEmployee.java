package model.employee;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import model.department.Department;
import model.department.Level;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class NormalEmployee extends Employee {

    private final boolean hasFaculty;

    private NormalEmployee(Long id, String name, String document, LocalDate birthDate, Integer age, Map<Department, Map<Level, BigDecimal>> departmentsAndLevelsAndSalaries, LocalDateTime createdDate, LocalDateTime lastUpdateDate, boolean hasFaculty) {
        super(id, name, document, birthDate, age, departmentsAndLevelsAndSalaries, createdDate, lastUpdateDate);
        this.hasFaculty = hasFaculty;
    }

    public boolean isHasFaculty() {
        return hasFaculty;
    }

    public static final class NormalEmployeeBuilder {
        private Long id;
        private String name;
        private String document;
        private LocalDate birthDate;
        private Integer age;
        private Map<Department, Map<Level, BigDecimal>> departmentsAndLevelsAndSalaries;
        private LocalDateTime createdDate;
        private LocalDateTime lastUpdateDate;
        private boolean hasFaculty;

        private NormalEmployeeBuilder() {
        }

        public static NormalEmployeeBuilder aNormalEmployee() {
            return new NormalEmployeeBuilder();
        }

        public NormalEmployeeBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public NormalEmployeeBuilder name(String name) {
            this.name = name;
            return this;
        }

        public NormalEmployeeBuilder document(String document) {
            this.document = document;
            return this;
        }

        public NormalEmployeeBuilder birthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public NormalEmployeeBuilder age(Integer age) {
            this.age = age;
            return this;
        }

        public NormalEmployeeBuilder departmentsAndLevelsAndSalaries(Map<Department, Map<Level, BigDecimal>> departmentsAndLevelsAndSalaries) {
            this.departmentsAndLevelsAndSalaries = departmentsAndLevelsAndSalaries;
            return this;
        }

        public NormalEmployeeBuilder createdDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public NormalEmployeeBuilder lastUpdateDate(LocalDateTime lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        public NormalEmployeeBuilder hasFaculty(boolean hasFaculty) {
            this.hasFaculty = hasFaculty;
            return this;
        }

        public NormalEmployee build() {
            return new NormalEmployee(id, name, document, birthDate, age, departmentsAndLevelsAndSalaries, createdDate, lastUpdateDate, hasFaculty);
        }
    }
}
