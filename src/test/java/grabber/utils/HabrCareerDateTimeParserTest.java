package grabber.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;

class HabrCareerDateTimeParserTest {
    @Test
    void okWhenCorrect() {
        String dateTime = "2024-11-23T16:33:12+03:00";
        LocalDateTime expected = LocalDateTime.of(2024, 11, 23, 16, 33, 12);
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        assertThat(parser.parse(dateTime)).isEqualTo(expected);
    }

    @Test
    void errorWhenMonth13() {
        String dateTime = "2024-13-23T16:33:12+03:00";
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        DateTimeParseException thrown = Assertions.assertThrows(DateTimeParseException.class, () -> {
            parser.parse(dateTime);
        });
        Assertions.assertEquals(String.format("Text '%s' could not be parsed: Invalid value for MonthOfYear (valid values 1 - 12): 13", dateTime),
                thrown.getMessage());
    }

    @Test
    void errorWhenYearsMore4Symbols() {
        String dateTime = "20241-10-23T16:33:12+03:00";
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        DateTimeParseException thrown = Assertions.assertThrows(DateTimeParseException.class, () -> {
            parser.parse(dateTime);
        });
        Assertions.assertEquals(String.format("Text '%s' could not be parsed at index 0", dateTime),
                thrown.getMessage());
    }

    @Test
    void errorWhenDays32Symbols() {
        String dateTime = "2024-10-32T16:33:12+03:00";
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        DateTimeParseException thrown = Assertions.assertThrows(DateTimeParseException.class, () -> {
            parser.parse(dateTime);
        });
        Assertions.assertEquals(String.format("Text '%s' could not be parsed: Invalid value for DayOfMonth (valid values 1 - 28/31): 32", dateTime),
                thrown.getMessage());
    }
}