package app;


import controllers.DepartmentController;
import controllers.EmployeeController;
import controllers.UserController;
import database.HibernateConnection;
import database.MongoConnection;
import dtos.response.UserResponse;
import enums.department.DepartmentMenu;
import enums.user.UserOption;
import enums.employee.EmployeeMenu;
import enums.menu.MainMenu;
import lombok.extern.log4j.Log4j2;
import mappers.DepartmentMapper;
import mappers.EmployeeMapper;
import mappers.UserMapper;
import model.Department;
import model.Employee;
import org.mapstruct.factory.Mappers;
import repositories.impl.DepartmentRepositoryImpl;
import repositories.impl.EmployeeRepositoryImpl;
import repositories.impl.UserRepositoryImpl;
import services.DepartmentService;
import services.EmployeeService;
import services.UserService;
import utils.ReaderUtils;

import java.util.InputMismatchException;
import java.util.List;

@Log4j2
public final class Application {

    private Application() {
    }

    private final static MongoConnection MONGO = MongoConnection.getINSTANCE();
    private final static HibernateConnection HIBERNATE = HibernateConnection.getINSTANCE("mysql");

    private final static DepartmentController DC = new DepartmentController(
            new DepartmentService(new DepartmentRepositoryImpl(HIBERNATE), Mappers.getMapper(DepartmentMapper.class))
    );
    private final static EmployeeController EC = new EmployeeController(
            new EmployeeService(new EmployeeRepositoryImpl(), Mappers.getMapper(EmployeeMapper.class))
    );
    private final static UserController UC = new UserController(
            new UserService(new UserRepositoryImpl(MONGO), Mappers.getMapper(UserMapper.class))
    );

    public static void main(String[] args) {
        mainMenu();
        System.out.println("Thanks for use! :)");
    }

    public static void mainMenu() {

        UserOption option = null;
        do {

            try {
                option = ReaderUtils.readEnum("user option", UserOption.class);

                UserResponse user;
                switch (option) {

                    case LOGIN -> {
                        user = UC.find();
                        loginMenu(user.username());
                    }

                    case CREATE_USER -> loginMenu(UC.create().username());

                    case DELETE_USER -> {
                        System.out.printf("User %s has been deleted!\n", UC.delete());
                    }
                }

            } catch (InputMismatchException e) {
                log.fatal("Invalid entry! Stopping the program..");
                System.exit(0);

            } catch (Exception e) {
                log.error(e.getMessage());
            }

        } while (option != UserOption.OUT);

    }


    private static void loginMenu(final String username) {

        log.info("{} logged into the system! \n", username);

        switch (ReaderUtils.readEnum("menu option", MainMenu.class)) {

            case OUT -> log.info("Logout by {}.. \n", username);

            case EMPLOYEES -> employeesMenu();

            case DEPARTAMENTS -> departmentsMenu();
        }

    }

    private static void employeesMenu() {

        EmployeeMenu option = null;
        do {

            try {
                option = ReaderUtils.readEnum("employee option", EmployeeMenu.class);

                switch (option) {
                    case HIRE -> EC.create(DC.findAll());

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
                log.fatal("Invalid entry! Stopping the program..");
                System.exit(0);

            } catch (Exception e) {
                log.error(e.getMessage());
            }

        } while (option != EmployeeMenu.OUT);

    }

    private static void departmentsMenu() {

        DepartmentMenu option = null;
        do {

            try {
                option = ReaderUtils.readEnum("Department option", DepartmentMenu.class);

                switch (option) {
                    case CREATE -> log.info("Department created: {}", DC.create());

                    case UPDATE -> {

                        Department department;

                        if ((department = DC.findByOption()) != null) {
                            System.out.printf("Department found:\n%s", department);
                            System.out.printf("\n ----- Department after update:\n%s", DC.update(department));
                        }
                    }

                    case SHOW -> DC.findByFilters().forEach(d -> System.out.printf("%s\n", d));

                    case DELETE -> {

                        final Department department = DC.findAndDelete();
                        System.out.printf("Department %s closed! %d employees dismissed! \n", department.getName(), department.getEmployees().size());

                    }

                }

            } catch (InputMismatchException e) {
                log.fatal("Invalid entry! Stopping the program..");
                System.exit(0);

            } catch (Exception e) {
                log.error(e.getMessage());
            }

        } while (option != DepartmentMenu.OUT);


    }

}
