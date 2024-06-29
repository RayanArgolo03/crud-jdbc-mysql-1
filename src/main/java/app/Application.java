package app;


import controllers.DepartmentController;
import controllers.EmployeeController;
import controllers.UserController;
import domain.department.Department;
import domain.employee.Employee;
import domain.user.User;
import enums.departament.DepartmentFindOption;
import enums.departament.DepartmentMenuOption;
import enums.employee.EmployeeMenuOption;
import enums.menu.DefaultMessage;
import enums.menu.MenuOption;
import enums.user.UserMenuOption;
import exceptions.EmployeeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import mappers.impl.DepartmentMapperImpl;
import mappers.impl.NormalEmployeeMapperImpl;
import mappers.impl.SuperiorEmployeeMapperImpl;
import mappers.impl.UserMapperImpl;
import repositories.impl.DepartmentRepositoryImpl;
import repositories.impl.EmployeeRepositoryImpl;
import repositories.impl.UserRepositoryImpl;
import services.DepartmentService;
import services.EmployeeService;
import services.UserService;
import utils.EnumListUtils;
import utils.ReaderUtils;

import java.util.InputMismatchException;
import java.util.List;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Application {
    private final static DepartmentController dc = new DepartmentController(
            new DepartmentService(new DepartmentRepositoryImpl(), new DepartmentMapperImpl())
    );
    private final static EmployeeController ec = new EmployeeController(
            new EmployeeService(new NormalEmployeeMapperImpl(), new SuperiorEmployeeMapperImpl(), new EmployeeRepositoryImpl())
    );
    private final static UserController uc = new UserController(
            new UserService(new UserRepositoryImpl(), new UserMapperImpl())
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

                        final Employee employee = ec.chooseEmployeeToUpdate(employeesFound);
                        System.out.printf("\n Employee before update:\n %s", employee);

                        ec.update(employee);
                        System.out.printf("Employee after update:\n %s", employee);
                    }
                    case SHOW -> {
                        ec.find().forEach(employee -> System.out.printf("%s \n", employee));
                    }
                    case DELETE -> {
                        System.out.println("Employees dismissed: " + ec.delete(dc.findAll()));
                    }
                }

            } catch (InputMismatchException e) {
                log.error(DefaultMessage.INVALID.getValue());
                System.exit(0);
            } catch (EmployeeException e) {
                log.error(e.getMessage());
            }

        } while (true);

    }

    private static void departamentsMenu() {

        do {
            try {

                DepartmentMenuOption option = ReaderUtils.readElement(
                        "departament option",
                        EnumListUtils.getEnumList(DepartmentMenuOption.class)
                );

                switch (option) {
                    case CREATE -> {
                        dc.create();
                    }
                    case UPDATE -> {

                        final List<Department> departamentsFound = dc.find();
                        ;

                        final Department department = dc.chooseDepartamentToUpdate(departamentsFound);
                        System.out.printf("\nDepartament before update:\n%s", department);

                        dc.update(department);
                        System.out.printf("\nDepartament after update:\n%s", department);
                    }
                    case SHOW -> {
                        dc.find().forEach(d -> System.out.printf("%s\n", d));
                    }
                    case DELETE -> {
                        System.out.printf("Departament closed! %d employees dismissed! \n", dc.delete());
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
