package dtos.response;

import model.Job;
import model.Level;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Set;

public record EmployeeResponse(
        Long id,
        String name,
        String birthDate,
        Integer age,
        String document,
        Set<Job> jobs,
        String createdDate,
        String lastUpdate,
        Integer workExperience,
        Boolean hasFaculty
) {

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();
        sb.append(id).append(" - ").append(name).append(" who was born ").append(birthDate).append("\n")
                .append(age).append(" years").append("\n")
                .append("With document ").append(document).append("\n");

        sb.append("Work informations: ").append("\n");
        for (Job job : jobs) {
            sb.append("Departament ").append(job.getDepartment());
            Level l = job.getLevel();
            sb.append("Seniority ").append(l.name());
            BigDecimal salary = job.getSalary();
            sb.append(" receiving ").append(NumberFormat.getCurrencyInstance().format(salary));
            sb.append("\n");
        }

        sb.append("Created date: ").append(createdDate).append("\n");
        sb.append("Last update date: ").append(lastUpdate).append("\n");

        if (workExperience != null) sb.append("Work experience: ").append(workExperience).append("\n");

        if (hasFaculty != null) {
            sb.append("Has faculty: ")
                    .append((hasFaculty == Boolean.TRUE) ? "Yes" : "No")
                    .append("\n");
        }

        return sb.toString();
    }
}
