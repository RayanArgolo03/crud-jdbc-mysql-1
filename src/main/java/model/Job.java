package model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import utils.FormatterUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;

@NoArgsConstructor(force = true)
@Getter
@Setter

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private final Department department;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id", nullable = false)
    private final Employee employee;

    @Enumerated(value = EnumType.STRING)
    private Level level;

    @Column(columnDefinition = "DECIMAL(15,2) NOT NULL")
    private BigDecimal salary;

    public Job(Department department, Employee employee, Level level, BigDecimal salary) {
        this.department = department;
        this.employee = employee;
        this.level = level;
        this.salary = salary;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Departament ").append(getDepartment());
        sb.append(" with seniority ").append(getLevel());
        sb.append(" receiving ").append(FormatterUtils.formatSalary(salary));
        return super.toString();
    }
}
