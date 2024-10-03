package model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter

@Embeddable
public class Job {

    @ManyToOne
    @JoinColumn(name = "department_id")
    Department department;

    @Enumerated(value = EnumType.STRING)
    Level level;

    @Column(columnDefinition = "DECIMAL(15,2) NOT NULL")
    BigDecimal salary;

    public Job(Department department, Level level, BigDecimal salary) {
        this.department = department;
        this.level = level;
        this.salary = salary;
    }
}
