package app;


import controllers.DepartmentController;
import controllers.EmployeeController;
import controllers.UserController;
import dtos.UserResponse;
import enums.departament.DepartmentMenuOption;
import enums.employee.EmployeeMenuOption;
import enums.menu.DefaultMessage;
import enums.menu.MenuOption;
import enums.UserOption;
import exceptions.EmployeeException;
import lombok.extern.log4j.Log4j2;
import mappers.DepartmentMapper;
import mappers.EmployeeMapper;
import mappers.UserMapper;
import model.department.Department;
import model.employee.Employee;
import org.mapstruct.factory.Mappers;
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
public final class Application {

    private Application() {
    }

    private final static DepartmentController DC = new DepartmentController(
            new DepartmentService(new DepartmentRepositoryImpl(), Mappers.getMapper(DepartmentMapper.class))
    );
    private final static EmployeeController EC = new EmployeeController(
            new EmployeeService(new EmployeeRepositoryImpl(), Mappers.getMapper(EmployeeMapper.class))
    );
    private final static UserController UC = new UserController(
            new UserService(new UserRepositoryImpl(), Mappers.getMapper(UserMapper.class))
    );

    public static void main(String[] args) {
        mainMenu();
        System.out.println("Thanks for use! :)");
    }

    public static void mainMenu() {

        UserOption option;

        while ((option = ReaderUtils.readEnum("user option", UserOption.class)) != UserOption.OUT) {

            try {
                option =;

                UserResponse user;
                switch (option) {
                    case LOGIN -> {
                        user = UC.find();
                        loginMenu(user.username());
                    }
                    case CREATE_USER -> {
                        loginMenu(UC.create().getUsername());
                    }
                    case DELETE_USER -> {
                        user = UC.find();
                        System.out.printf("User %s has been deleted!\n", UC.delete(user.id()));
                    }
                }

            } catch (InputMismatchException e) {
                log.error(DefaultMessage.INVALID.getValue());
                System.exit(0);
            } catch (Exception e) {
                log.error(e.getMessage());
            }


        }
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
                        EC.create(DC.findAll());
                    }
                    case UPDATE -> {

                        final List<Employee> employeesFound = EC.find();

                        final Employee employee = EC.chooseEmployeeToUpdate(employeesFound);
                        System.out.printf("\n Employee before update:\n %s", employee);

                        EC.update(employee);
                        System.out.printf("Employee after update:\n %s", employee);
                    }
                    case SHOW -> {
                        EC.find().forEach(employee -> System.out.printf("%s \n", employee));
                    }
                    case DELETE -> {
                        System.out.println("Employees dismissed: " + EC.delete(DC.findAll()));
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
                        DC.create();
                    }
                    case UPDATE -> {

                        final List<Department> departamentsFound = DC.find();
                        ;

                        final Department department = DC.chooseDepartamentToUpdate(departamentsFound);
                        System.out.printf("\nDepartament before update:\n%s", department);

                        DC.update(department);
                        System.out.printf("\nDepartament after update:\n%s", department);
                    }
                    case SHOW -> {
                        DC.find().forEach(d -> System.out.printf("%s\n", d));
                    }
                    case DELETE -> {
                        System.out.printf("Departament closed! %d employees dismissed! \n", DC.delete());
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
