package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)

@Entity
@DynamicInsert
@DynamicUpdate
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "employees")

@NamedQuery(name = "Employee.findByName", query = "SELECT e FROM Employee e WHERE name = :name")
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

    @OneToMany(mappedBy = "employee", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Job> jobs;

    @CreationTimestamp
    @Column(name = "hire_date")
    private final LocalDateTime hireDate;

    @UpdateTimestamp
    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    protected Employee(Builder builder) {

        this.name = builder.name;
        this.document = builder.document;
        this.birthDate = builder.birthDate;
        this.age = builder.age;

        this.hireDate = null;
        this.lastUpdateDate = null;

    }

    public abstract static class Builder<T extends Builder> {

        protected String name;
        protected String document;
        protected LocalDate birthDate;
        protected Integer age;

        T self() {
            return (T) this;
        }


        public T name(String name) {
            this.name = name;
            return self();
        }

        public T document(String document) {
            this.document = document;
            return self();
        }

        public T birthDate(LocalDate birthDate) {
            this.birthDate = birthDate;
            return self();
        }

        public T age(Integer age) {
            this.age = age;
            return self();
        }

        public abstract Employee build();
    }
}
