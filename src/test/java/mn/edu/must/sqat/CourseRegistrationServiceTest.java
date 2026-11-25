package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;
    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private Student activeStudent() {
        return new Student("S1", "s1@must.edu.mn", true, List.of("CS101"));
    }

    // A1: when + verify
    @Test
    void register_activeStudentWithAllPrerequisites_returnsOkSavesAndSendsEmail() {
        when(repository.findStudent("S1")).thenReturn(activeStudent());
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        String result = service.register("S1", "CS201");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S1", "CS201");
        verify(emailSender).send(eq("s1@must.edu.mn"), anyString(), anyString());
    }

    // A2: verify(..., never())
    @Test
    void register_unknownStudent_returnsErrorNoStudentAndDoesNotSaveOrEmail() {
        when(repository.findStudent("S9")).thenReturn(null);

        String result = service.register("S9", "CS201");

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3: verifyNoInteractions
    @Test
    void register_inactiveStudent_returnsErrorInactiveAndNeverTouchesEmail() {
        Student inactive = new Student("S2", "s2@must.edu.mn", false, List.of());
        when(repository.findStudent("S2")).thenReturn(inactive);

        String result = service.register("S2", "CS201");

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verifyNoInteractions(emailSender);
    }

    // A4: when + never
    @Test
    void register_missingPrerequisite_returnsErrorPrerequisitesAndDoesNotSave() {
        Student newbie = new Student("S3", "s3@must.edu.mn", true, List.of());
        when(repository.findStudent("S3")).thenReturn(newbie);
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        String result = service.register("S3", "CS201");

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    // A5: doThrow
    @Test
    void register_emailSenderThrows_stillReturnsOkAndRegistrationSaved() {
        when(repository.findStudent("S1")).thenReturn(activeStudent());
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));
        doThrow(new RuntimeException("SMTP down"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        String result = service.register("S1", "CS201");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S1", "CS201");
    }

    // A6: assertThrows (хоёр аргумент тус бүр)
    @Test
    void register_nullArguments_throwsIllegalArgumentAndTouchesNoDependencies() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "CS201"));
        assertThrows(IllegalArgumentException.class, () -> service.register("S1", null));

        verifyNoInteractions(repository, emailSender);
    }

    // A7: ArgumentCaptor
    @Test
    void register_success_emailBodyContainsCourseCode() {
        when(repository.findStudent("S1")).thenReturn(activeStudent());
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        service.register("S1", "CS201");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(eq("s1@must.edu.mn"), anyString(), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains("CS201"));
    }

    // A8: параметржүүлсэн тест, 3 алдааны анги
    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "NO_STUDENT, ERROR_NO_STUDENT",
        "INACTIVE_STUDENT, ERROR_INACTIVE_STUDENT",
        "PREREQUISITES, ERROR_PREREQUISITES"
    })
    void register_errorScenarios_returnExpectedErrorCode(String scenario, String expected) {
        // Мөр бүр өөр stub ашигладаг тул lenient() хэрэглэв (strict stubs-д
        // UnnecessaryStubbingException өгөхөөс сэргийлнэ).
        if (scenario.equals("NO_STUDENT")) {
            lenient().when(repository.findStudent("S1")).thenReturn(null);
        } else if (scenario.equals("INACTIVE_STUDENT")) {
            lenient().when(repository.findStudent("S1"))
                    .thenReturn(new Student("S1", "s1@must.edu.mn", false, List.of()));
        } else {
            lenient().when(repository.findStudent("S1"))
                    .thenReturn(new Student("S1", "s1@must.edu.mn", true, List.of()));
            lenient().when(repository.courseExists("CS201")).thenReturn(true);
            lenient().when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));
        }

        assertEquals(expected, service.register("S1", "CS201"));
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }
}
