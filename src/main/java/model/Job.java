package model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

}
