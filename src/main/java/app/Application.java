package app;


import controllers.DepartamentController;
import controllers.EmployeeController;
import controllers.UserController;
import dao.impl.DepartamentDAOImpl;
import dao.impl.EmployeeDAOImpl;
import dao.impl.UserDAOImpl;
import domain.departament.Departament;
import domain.employee.Employee;
import domain.user.User;
import enums.departament.DepartamentFindOption;
import enums.departament.DepartamentMenuOption;
import enums.employee.EmployeeMenuOption;
import enums.menu.DefaultMessage;
import enums.menu.MenuOption;
import enums.user.UserMenuOption;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mappers.DepartamentMapper;
import mappers.NormalEmployeeMapper;
import mappers.SuperiorEmployeeMapper;
import mappers.UserMapper;
import services.DepartamentService;
import services.EmployeeService;
import services.UserService;
import utils.EnumListUtils;
import utils.ReaderUtils;

import java.util.InputMismatchException;
import java.util.List;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Application {
    private final static DepartamentController dc = new DepartamentController(
            new DepartamentService(new DepartamentDAOImpl(), new DepartamentMapper())
    );
    private final static EmployeeController ec = new EmployeeController(
            new EmployeeService(new NormalEmployeeMapper(), new SuperiorEmployeeMapper(), new EmployeeDAOImpl())
    );
    private final static UserController uc = new UserController(
            new UserService(new UserDAOImpl(), new UserMapper())
    );

    public static void main(String[] args) {
        mainMenu();
        System.out.println("Thanks for use! :)");
    }

    public static void mainMenu() {

        UserMenuOption option = null;
        do {
            try {
                option = ReaderUtils.readElement("user option",
                        EnumListUtils.getEnumList(UserMenuOption.class));

                User user;
                switch (option) {
                    case LOGIN -> {
                        user = uc.find();
                        loginMenu(user.getUsername());
                    }
                    case CREATE_USER -> {
                        user = uc.create();
                        loginMenu(user.getUsername());
                    }
                    case DELETE_USER -> {
                        user = uc.find();
                        System.out.printf("User of id %d has been deleted!\n", uc.delete(user));
                    }
                }

            } catch (InputMismatchException e) {
                log.error(DefaultMessage.INVALID.getValue());
                System.exit(0);
            } catch (NullPointerException e) {
                log.error("Null value! {}", e.getMessage());
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        } while (option != UserMenuOption.OUT);
    }

    private static void loginMenu(final String username) {

        log.info("{} logged into the system! \n", username);

        final MenuOption option = ReaderUtils.readElement("menu option",
                EnumListUtils.getEnumList(MenuOption.class));

        switch (option) {
            case OUT -> log.info("Logout {}.. \n", username);
            case EMPLOYEES -> employeesMenu();
            case DEPARTAMENTS -> departamentsMenu();
        }

    }

    private static void employeesMenu() {

        do {
            try {
                EmployeeMenuOption option = ReaderUtils.readElement("employee option",
                        EnumListUtils.getEnumList(EmployeeMenuOption.class));

                switch (option) {
                    case OUT -> {
                        return;
                    }
                    case HIRE -> {
                        ec.create(dc.findAll());
                    }
                    case UPDATE -> {

                        final List<Employee> employeesFound = ec.find();

                        //Todo melhorar método
                        final Employee employee = ec.chooseEmployeeToUpdate(employeesFound);
                        System.out.printf("\nEmployee before update: \n%s", employee);

                        ec.update(employee);
                        System.out.printf("Employee after update:\n%s", employee);
                    }
                    case SHOW -> {
                        ec.find().forEach(employee -> System.out.printf("\n%s \n", employee));
                    }
                    case DELETE -> {
                        System.out.println("Employees dismissed: " + ec.delete(dc.findAll()));
                    }
                }

            } catch (InputMismatchException e) {
                log.error(DefaultMessage.INVALID.getValue());
                System.exit(0);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        } while (true);

    }

    private static void departamentsMenu() {

        do {
            try {

                DepartamentMenuOption option = ReaderUtils.readElement(
                        "departament option",
                        EnumListUtils.getEnumList(DepartamentMenuOption.class)
                );

                switch (option) {
                    case CREATE -> {
                        dc.create();
                    }
                    case UPDATE -> {

                        final List<Departament> departamentsFound = dc.find(
                                List.of(DepartamentFindOption.NAME, DepartamentFindOption.ID)
                        );

                        final Departament departament = dc.chooseDepartamentToUpdate(departamentsFound);
                        System.out.printf("\nDepartament before update:\n%s", departament);

                        dc.update(departament);
                        System.out.printf("\nDepartament after update:\n%s", departament);
                    }
                    case SHOW -> {
                        dc.find(EnumListUtils.getEnumList(DepartamentFindOption.class))
                                .forEach(d -> System.out.printf("\n%s\n", d));
                    }
                    case DELETE -> {
                        System.out.printf("Departament closed! %d employees dismissed!", dc.delete());
                    }
                    case OUT -> {
                        return;
                    }

                }

            } catch (InputMismatchException e) {
                log.error(DefaultMessage.INVALID.getValue());
                System.exit(0);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        } while (true);

    }

}
