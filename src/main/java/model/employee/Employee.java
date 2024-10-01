package model.employee;

import model.department.Department;
import model.department.Level;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public abstract class Employee {

    private Long id;
    private String name;
    private String document;
    private final LocalDate birthDate;
    private final Integer age;
    private final Map<Department, Map<Level, BigDecimal>> departmentsAndLevelsAndSalaries;
    private final LocalDateTime hireDate;
    private LocalDateTime lastUpdateDate;

    protected Employee(String name, String document, LocalDate birthDate, Integer age, Map<Department, Map<Level, BigDecimal>> departmentsAndLevelsAndSalaries) {
        this.name = name;
        this.document = document;
        this.birthDate = birthDate;
        this.age = age;
        this.departmentsAndLevelsAndSalaries = departmentsAndLevelsAndSalaries;
        this.hireDate = LocalDateTime.now();
        this.lastUpdateDate = LocalDateTime.now();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public void setLastUpdateDate(LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDocument() {
        return document;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Integer getAge() {
        return age;
    }

    public Map<Department, Map<Level, BigDecimal>> getDepartmentsAndLevelsAndSalaries() {
        return departmentsAndLevelsAndSalaries;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }
}
