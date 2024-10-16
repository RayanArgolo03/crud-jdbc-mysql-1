package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@EqualsAndHashCode(callSuper = true)

@Entity
@Table(name = "normal_employes")
public class NormalEmployee extends Employee {

    @Column(name = "has_faculty", nullable = false)
    private boolean hasFaculty;

    public NormalEmployee() {
    }

    public NormalEmployee(Builder builder) {
        super(builder);
        this.hasFaculty = builder.hasFaculty;
    }

    public void hasFaculty() {
        this.hasFaculty = true;
    }

    public static class Builder<T extends Builder<T>> extends Employee.Builder<T> {

        protected boolean hasFaculty;

        @Override
        T self() {
            return super.self();
        }

        public T hasFaculty(boolean hasFaculty) {
            this.hasFaculty = hasFaculty;
            return self();
        }

        @Override
        public Employee build() {
            return new NormalEmployee(this);
        }
    }

}
