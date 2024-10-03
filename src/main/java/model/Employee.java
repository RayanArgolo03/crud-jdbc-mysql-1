package model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(force = true)

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "employees")
public abstract class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "cpf", columnDefinition = "VARCHAR(11) NOT NULL UNIQUE")
    private String document;

    @Column(name = "birth_date", nullable = false)
    private final LocalDate birthDate;

    @Column(nullable = false)
    private final Integer age;

    //Todo mapear conjunto incorporado - Mapear employees a departamentos
    private Set<JobInfo> jobsInfo;

    @CreationTimestamp
    private final LocalDateTime hireDate;

    @UpdateTimestamp
    private LocalDateTime lastUpdateDate;

    protected Employee(Builder builder) {

        this.name = builder.name;
        this.document = builder.document;
        this.birthDate = builder.birthDate;
        this.age = builder.age;
        this.departmentsAndLevelsAndSalaries = builder.departmentsAndLevelsAndSalaries;

        this.hireDate = null;
        this.lastUpdateDate = null;

    }

    public abstract static class Builder<T extends Builder> {

        protected String name;
        protected String document;
        protected LocalDate birthDate;
        protected Integer age;
        protected Map<Department, Map<Level, BigDecimal>> departmentsAndLevelsAndSalaries;

        T builder() {
            return (T) this;
        }


        public T name(String name) {
            this.name = name;
            return builder();
        }

        public T document(String document) {
            this.document = document;
            return builder();
        }

        public T birthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
            return builder();
        }

        public T age(Integer age) {
            this.age = age;
            return builder();
        }

        public T departmentsAndLevelsAndSalaries(Map<Department, Map<Level, BigDecimal>> departmentsAndLevelsAndSalaries) {
            this.departmentsAndLevelsAndSalaries = departmentsAndLevelsAndSalaries;
            return builder();
        }

        public abstract Employee build();
    }
}
