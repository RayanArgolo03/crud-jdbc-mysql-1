package utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormatterUtilsTest {

    @DisplayName("Should be return name in itself name format when receives not standardised name")
    @Test
    void givenFormatName_whenNameIsAValidName_thenReturnNameWithFirstLetterInUpperCase() {
        assertEquals("Nameean", FormatterUtils.formatName("NaMeEaN"));
    }

    @DisplayName("Should be return the money formatted in the BigDecimal pattern")
    @Test
    void givenFormatMoney_whenMoneyIsAValidMoney_thenReturnMoneyWithDot() {
        assertEquals("1200.35", FormatterUtils.formatMoney("1200,35"));
    }

}