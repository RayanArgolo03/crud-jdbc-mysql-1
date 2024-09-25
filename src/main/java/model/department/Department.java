package model.department;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class Department {

    private Long id;
    private String name;
    private final LocalDateTime createdDate;
    private LocalDateTime lastUpdateDate;

    private Department(Long id, String name, LocalDateTime lastUpdateDate) {
        this.id = id;
        this.name = name;
        this.createdDate = LocalDateTime.now();
        this.lastUpdateDate = lastUpdateDate;
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


    public static final class DepartmentBuilder {

        private Long id;
        private String name;
        private LocalDateTime creationDate;
        private LocalDateTime lastUpdateDate;

        private DepartmentBuilder() {
        }

        public static DepartmentBuilder builder() {
            return new DepartmentBuilder();
        }

        public DepartmentBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DepartmentBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DepartmentBuilder creationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public DepartmentBuilder lastUpdateDate(LocalDateTime lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        public Department build() { return new Department(id, name, lastUpdateDate);}
    }
}
