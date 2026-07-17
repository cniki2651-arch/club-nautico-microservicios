package club.ms.socios.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Equivalente Java de CP-CN-REG-01 (dniValidacion.test.js del backend monolitico original):
// "Validar formato de DNI del postulante" (exactamente 8 digitos numericos).
class CrearSolicitudRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private CrearSolicitudRequest requestValido(String dni) {
        CrearSolicitudRequest request = new CrearSolicitudRequest();
        request.setIdTipoDoc(1);
        request.setDni(dni);
        request.setNombres("Demo");
        request.setApellidos("Presentacion");
        request.setClasificacion("REGULAR");
        request.setTipoSolicitud("Inscripcion");
        return request;
    }

    @Test
    void dniConLetrasEsRechazado() {
        Set<ConstraintViolation<CrearSolicitudRequest>> violaciones =
                validator.validate(requestValido("1234567A"));

        assertTrue(violaciones.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dni")));
    }

    @Test
    void dniDeLongitudCortaEsRechazado() {
        Set<ConstraintViolation<CrearSolicitudRequest>> violaciones =
                validator.validate(requestValido("12345"));

        assertTrue(violaciones.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dni")));
    }

    @Test
    void dniDeNueveDigitosEsRechazado() {
        Set<ConstraintViolation<CrearSolicitudRequest>> violaciones =
                validator.validate(requestValido("123456789"));

        assertTrue(violaciones.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dni")));
    }

    @Test
    void dniVacioEsRechazado() {
        Set<ConstraintViolation<CrearSolicitudRequest>> violaciones =
                validator.validate(requestValido(""));

        assertTrue(violaciones.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dni")));
    }

    @Test
    void dniDeOchoDigitosEsAceptado() {
        Set<ConstraintViolation<CrearSolicitudRequest>> violaciones =
                validator.validate(requestValido("12345678"));

        assertTrue(violaciones.stream().noneMatch(v -> v.getPropertyPath().toString().equals("dni")));
    }
}
