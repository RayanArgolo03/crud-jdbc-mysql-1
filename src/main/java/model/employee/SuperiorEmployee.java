package model.employee;

import model.department.Department;
import model.department.Level;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public final class SuperiorEmployee extends Employee {

    private final int workExperience;

    public SuperiorEmployee(Long id, String name, String document, LocalDate birthDate, Integer age, Map<Department, Map<Level, BigDecimal>> departmentsAndLevelsAndSalaries, LocalDateTime createdDate, LocalDateTime lastUpdateDate, int workExperience) {
        super(id, name, document, birthDate, age, departmentsAndLevelsAndSalaries, createdDate, lastUpdateDate);
        this.workExperience = workExperience;
    }

    public int getWorkExperience() {
        return workExperience;
    }


    public static final class SuperiorEmployeeBuilder {
        private Long id;
        private String name;
        private String document;
        private LocalDate birthDate;
        private Integer age;
        private Map<Department, Map<Level, BigDecimal>> departmentsAndLevelsAndSalaries;
        private LocalDateTime createdDate;
        private LocalDateTime lastUpdateDate;
        private int workExperience;

        private SuperiorEmployeeBuilder() {
        }

        public static SuperiorEmployeeBuilder aSuperiorEmployee() {
            return new SuperiorEmployeeBuilder();
        }

        public SuperiorEmployeeBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SuperiorEmployeeBuilder name(String name) {
            this.name = name;
            return this;
        }

        public SuperiorEmployeeBuilder document(String document) {
            this.document = document;
            return this;
        }

        public SuperiorEmployeeBuilder birthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public SuperiorEmployeeBuilder age(Integer age) {
            this.age = age;
            return this;
        }

        public SuperiorEmployeeBuilder departmentsAndLevelsAndSalaries(Map<Department, Map<Level, BigDecimal>> departmentsAndLevelsAndSalaries) {
            this.departmentsAndLevelsAndSalaries = departmentsAndLevelsAndSalaries;
            return this;
        }

        public SuperiorEmployeeBuilder createdDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public SuperiorEmployeeBuilder lastUpdateDate(LocalDateTime lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        public SuperiorEmployeeBuilder workExperience(int workExperience) {
            this.workExperience = workExperience;
            return this;
        }

        public SuperiorEmployee build() {
            return new SuperiorEmployee(id, name, document, birthDate, age, departmentsAndLevelsAndSalaries, createdDate, lastUpdateDate, workExperience);
        }
    }
}
