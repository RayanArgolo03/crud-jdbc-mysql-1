package controllers;

import domain.departaments.Departament;
import enums.departament.DepartamentDeleteOption;
import enums.departament.DepartamentFindOption;
import enums.departament.DepartamentUpdateOption;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import services.DepartamentService;

import java.util.List;

@Log4j2
@AllArgsConstructor
public final class DepartamentController {

    private final DepartamentService service;

    public void create() {

        final String name = service.receiveName();
        final Departament departament = service.createDepartament(name);

        service.saveDepartament(departament);

        System.out.println(departament);
    }

    public List<Departament> findAll() {
        return service.findAllDepartaments();
    }

    public List<Departament> find(final List<DepartamentFindOption> options) {
        System.out.println("Receiving option to find..");
        final DepartamentFindOption option = service.receiveOption(options);
        return service.findByOption(option);
    }

    public Departament chooseDepartamentToUpdate(final List<Departament> departamentsFound) {
        return (departamentsFound.size() == 1)
                ? departamentsFound.get(0)
                : service.receiveDepartament(departamentsFound);
    }

    public void update(final Departament departament) {
        //The departament can only have its name updated, but new options can be added
        final DepartamentUpdateOption option = DepartamentUpdateOption.NAME;
        service.updateByOption(option, departament);
    }

    public int delete() {
        System.out.println("Receiving option to delete..");
        final DepartamentDeleteOption departamentDeleteOption = service.receiveOption(
                List.of(DepartamentDeleteOption.ID, DepartamentDeleteOption.NAME)
        );
        return service.deleteByOption(departamentDeleteOption);
    }
}
