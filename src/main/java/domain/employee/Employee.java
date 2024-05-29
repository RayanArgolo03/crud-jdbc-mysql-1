package domain.employee;

import domain.departament.Departament;
import domain.departament.Level;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

@SuperBuilder
@Getter
public abstract class Employee {

    @Setter
    private Long id;
    @Setter
    private String name;
    @Setter
    private String document;
    private final LocalDate birthDate;
    private int age;
    private final Map<Departament, Map<Level, BigDecimal>> departamentsAndLevelsAndSalaries;
    private final LocalDateTime hireDate;
    private LocalDateTime lastUpdateDate;

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();
        sb.append(name).append(" who was born ").append(birthDate).append("\n")
                .append(age).append(" years").append("\n")
                .append("With document ").append(document).append("\n");

        sb.append("Work informations: ").append("\n");
        for (Departament d : departamentsAndLevelsAndSalaries.keySet()) {
            sb.append("Departament ").append(d);
            Level l = departamentsAndLevelsAndSalaries.get(d).keySet().stream().findFirst().get();
            sb.append("Seniority ").append(l.name());
            BigDecimal salary = departamentsAndLevelsAndSalaries.get(d).get(l);
            sb.append(" receiving ").append(NumberFormat.getCurrencyInstance().format(salary));
            sb.append("\n");
        }

        //To print information about the children´s entity more the hire date, if receives
        if (Objects.nonNull(hireDate)) {
            sb.append("Hire date: ").append(
                    hireDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ).append("\n");
        }

        if (Objects.nonNull(lastUpdateDate)) {
            sb.append("Last update date: ").append(
                    lastUpdateDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            ).append("\n");
        }


        return sb.toString();
    }
}
