package model;

public class NormalEmployee extends Employee {

    private final boolean hasFaculty;

    public NormalEmployee(Builder builder) {
        super(builder);
        this.hasFaculty = builder.hasFaculty;
    }

    public static class Builder<T extends Builder<T>> extends Employee.Builder<T> {

        protected boolean hasFaculty;

        @Override
        T builder() {
            return super.builder();
        }

        public T hasFaculty(boolean hasFaculty) {
            this.hasFaculty = hasFaculty;
            return builder();
        }

        @Override
        public Employee build() {
            return new NormalEmployee(this);
        }
    }
}
