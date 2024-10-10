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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
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

    @UpdateTimestamp
    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Job> jobs;

    public Department(String name) {
        this.name = name;
        this.createdDate = null;
        this.lastUpdateDate = null;
    }

    public Set<Employee> getEmployees(){
        return jobs.stream()
                .map(Job::getEmployee)
                .collect(Collectors.toSet());
    }

    //Todo corrigir to string department
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        sb.append(id).append(" - ").append(name).append(" - ").append(" Created at ").append(createdDate.format(dtf))
                .append(" - ")
                .append("Last update date: ").append(Objects.isNull(lastUpdateDate) ? "No updates" : lastUpdateDate.format(dtf))
                .append("\n");

        return sb.toString();
    }
}
