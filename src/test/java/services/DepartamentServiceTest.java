package services;

import dao.impl.DepartamentDAOImpl;
import exceptions.DepartamentException;
import mappers.DepartamentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DepartamentServiceTest {
    private DepartamentService service;

    @BeforeEach
    void setUp() {
        service = new DepartamentService(
                new DepartamentDAOImpl(), new DepartamentMapper()
        );
    }

    @DisplayName("Should be throw Departament Exception when departaments list to map is empty")
    @Test
    void givenMapDepartaments_whenDepartamentsListIsEmpty_thenThrowDepartamentException() {

        DepartamentException e = assertThrows(DepartamentException.class,
                () -> service.mapDepartaments(List.of()));

        String message = "Departaments not found!";
        assertEquals(message, e.getMessage());
    }

    //Continue

}