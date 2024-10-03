package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public final class SuperiorEmployee extends Employee {

    private final int workExperience;

    public SuperiorEmployee(Builder builder) {
        super(builder);
        this.workExperience = builder.workExperience;
    }

    public static class Builder<T extends SuperiorEmployee.Builder<T>> extends Employee.Builder<T> {

        private int workExperience;

        @Override
        T builder() {
            return super.builder();
        }

        public T workExperience(int workExperience) {
            this.workExperience = workExperience;
            return builder();
        }

        @Override
        public Employee build() {
            return new SuperiorEmployee(this);
        }
    }

}
