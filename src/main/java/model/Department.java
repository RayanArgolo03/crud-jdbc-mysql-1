package model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor(force = true)

@Entity
@Table(name = "departments")
@DynamicInsert
@DynamicUpdate

@NamedQuery(name = "Department.findAll", query = "SELECT d FROM Department d")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long id;

    @Column(columnDefinition = "varchar(50)", unique = true, nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name = "created_date")
    private final LocalDateTime createdDate;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Job> jobs;

    public Department(String name) {
        this.name = name;
        this.createdDate = null;
        this.lastUpdateDate = null;
    }

    @PreUpdate
    public void onPreUpdate() {
        this.setLastUpdateDate(LocalDateTime.now());
    }

    public Set<Employee> getEmployees() {
        return jobs.stream()
                .map(Job::getEmployee)
                .collect(Collectors.toSet());
    }


}
