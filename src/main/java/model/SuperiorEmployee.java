package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter

@Entity
@Table(name = "superior_employees")
public  class SuperiorEmployee extends Employee {

    //If not set in defineSpecific method, using one year
    @Column(name = "work_experience", nullable = false)
    private int workExperience = 1;

    public SuperiorEmployee() {
    }

    public SuperiorEmployee(Builder builder) {
        super(builder);
        this.workExperience = builder.workExperience;
    }

    public void setWorkExperience(int workExperience) {
        this.workExperience = workExperience;
    }

    public static class Builder<T extends SuperiorEmployee.Builder<T>> extends Employee.Builder<T> {

        private int workExperience;

        @Override
        T self() {
            return super.self();
        }

        public T workExperience(int workExperience) {
            this.workExperience = workExperience;
            return self();
        }

        @Override
        public Employee build() {
            return new SuperiorEmployee(this);
        }
    }

}
