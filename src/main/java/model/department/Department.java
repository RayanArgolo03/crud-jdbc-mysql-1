package model.department;

import jakarta.persistence.*;
import model.employee.Employee;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;

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

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "departmentsAndLevelsAndSalaries")
    private Set<Employee> employees;

    public Department(String name) {
        this.name = name;
        this.createdDate = null;
        this.lastUpdateDate = null;
    }

    public Department() {
        createdDate = null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
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

    public Set<Employee> getEmployees() {
        return employees;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

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
