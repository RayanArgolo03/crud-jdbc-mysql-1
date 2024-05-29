package domain.employee;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public final class SuperiorEmployee extends Employee {
    private int workExperience;

    public void setWorkExperience(int workExperience) {
        this.workExperience = workExperience;
    }

    @Override
    public String toString() {
        return super.toString() + "Work experience: " + workExperience + " years\n";
    }
}
