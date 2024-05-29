package services;

import dao.interfaces.DepartamentDAO;
import domain.departament.Departament;
import dto.departament.DepartamentDTO;
import enums.departament.DepartamentDeleteOption;
import enums.departament.DepartamentFindOption;
import enums.departament.DepartamentUpdateOption;
import enums.menu.DefaultMessage;
import exceptions.DepartamentException;
import lombok.AllArgsConstructor;
import mappers.DepartamentMapper;
import utils.FormatterUtils;
import utils.ReaderUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class DepartamentService {

    private final DepartamentDAO dao;
    private final DepartamentMapper mapper;

    public Departament createDepartament(final String name) {
        return Departament.builder()
                .name(name)
                .creationDate(LocalDateTime.now())
                .lastUpdateDate(null)
                .build();
    }

    public void saveDepartament(final Departament departament) {
        dao.save(departament);
    }

    public <T extends Enum<T>> T receiveOption(final List<T> list) {
        return ReaderUtils.readElement("a", list);

    }

    public List<Departament> findByOption(final DepartamentFindOption option) {

        return switch (option) {
            case ID -> {
                final long id = this.receiveId();
                yield Collections.singletonList(this.findById(id));
            }
            case NAME -> {
                final String name = this.receiveName();
                yield this.findByName(name);
            }
            case CREATION_DATE -> {
                final LocalDate creationDateWithoutTime = this.receiveCreationDate();
                yield this.findByCreationDate(creationDateWithoutTime);
            }
        };
    }

    private LocalDate receiveCreationDate() {

        String creationDateInString = ReaderUtils.readString("creation date(pattern dd/mm/yyyy)");
        LocalDate date;
        while (Objects.isNull(date = parseAndValidateDate(creationDateInString))) {
            System.out.println(DefaultMessage.INVALID.getValue() + " Pattern dd/mm/yyyy !!!");
            creationDateInString = ReaderUtils.readString("creation date(pattern dd/mm/yyyy)");
        }

        return date;
    }

    public LocalDate parseAndValidateDate(final String creationDateInString) {
        try {
            return LocalDate.parse(creationDateInString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public List<Departament> findByCreationDate(final LocalDate creationDateWithoutTime) {

        final List<DepartamentDTO> departaments = dao.findbyCreationDate(creationDateWithoutTime);
        if (departaments.isEmpty()) throw new DepartamentException("Departaments not found!");

        return departaments.stream()
                .map(mapper::dtoToEntity)
                .collect(Collectors.toList());
    }

    public List<Departament> findByName(final String name) {

        final List<DepartamentDTO> departaments = dao.findByName(name);
        if (departaments.isEmpty()) throw new DepartamentException("Departaments not found!");

        return departaments.stream()
                .map(mapper::dtoToEntity)
                .collect(Collectors.toList());
    }

    public String receiveName() {

        String name;
        while (!validName(name = ReaderUtils.readString("departament name"))) {
            System.out.println(DefaultMessage.INVALID.getValue());
        }

        name = FormatterUtils.formatName(name);

        return name;
    }

    private long receiveId() {

        System.out.printf("%s %s: ", DefaultMessage.ENTER_WITH.getValue(), "departament id");
        long id = 2;

        while (id < 1) {
            System.out.println(DefaultMessage.INVALID.getValue());
            System.out.printf("%s %s: ", DefaultMessage.ENTER_WITH.getValue(), "departament id");

        }

        return id;
    }

    public Departament findById(final long id) {
        return dao.findById(id)
                .map(mapper::dtoToEntity)
                .orElseThrow(() -> new DepartamentException("Departament not found!"));
    }

    public List<DepartamentDTO> findAll() {
        return dao.findAll();
    }

    public List<Departament> mapDepartaments(final List<DepartamentDTO> departaments) {

        if (departaments.isEmpty()) throw new DepartamentException("Departaments not found!");

        return departaments.stream()
                .map(mapper::dtoToEntity)
                .collect(Collectors.toList());
    }

    public Departament receiveDepartament(final List<Departament> departamentsFound) {

        System.out.println("More than one departament returned! Choose one");
        System.out.printf("\n%s %s:\n", DefaultMessage.ENTER_WITH.getValue(), "departament id");

        for (Departament d : departamentsFound) System.out.printf("%s", d);

        long choice = 1;
        return departamentsFound.stream()
                .filter(d -> d.getId().equals(choice))
                .findFirst()
                .orElseThrow(() -> new DepartamentException(DefaultMessage.INVALID.getValue()));
    }

    public void updateByOption(final DepartamentUpdateOption option,
                               final Departament departament) {

        //Open for extensions
        switch (option) {
            case NAME -> {
                String newName = this.receiveName();
                this.updateName(departament, newName);
            }
        }

    }

    public void updateName(final Departament departament, final String newName) {


        if (!validName(newName, departament.getName())) {
            throw new DepartamentException("Name can´t be equals to current name!");
        }

        dao.updateName(departament, newName);
        departament.setName(newName);
        departament.setLastUpdateDate(LocalDateTime.now());
    }

    public int deleteByOption(final DepartamentDeleteOption departamentDeleteOption) {

        return switch (departamentDeleteOption) {
            case ID -> {
                final long id = this.receiveId();
                yield this.deleteById(id);
            }
            case NAME -> {
                final String name = this.receiveName();
                yield this.deleteByName(name);
            }
        };

    }

    public int deleteById(final long id) {
        return dao.deleteById(id);
    }


    public int deleteByName(final String name) {
        return dao.deleteByName(name);
    }

    public boolean validName(final String name) {
        return name.matches("[a-zA-ZÀ-ÿ,\". ]+$");
    }

    public boolean validName(final String newName, final String oldName) {
        return !newName.equalsIgnoreCase(oldName);
    }

    public boolean validId(final long id) {
        return id > 0;
    }

}
