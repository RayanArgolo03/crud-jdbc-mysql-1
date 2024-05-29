package domain.departament;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Builder
@Getter
public final class Departament {
    @Setter
    private Long id;
    @Setter
    private String name;
    private final LocalDateTime creationDate;
    @Setter
    private LocalDateTime lastUpdateDate;
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        sb.append(id).append(" - ").append(name).append(" - ").append(" Created at ").append(creationDate.format(dtf))
                .append(" - ")
                .append("Last update date: ").append(Objects.isNull(lastUpdateDate) ? "No updates" : lastUpdateDate.format(dtf))
                .append("\n");
        return sb.toString();
    }
}
