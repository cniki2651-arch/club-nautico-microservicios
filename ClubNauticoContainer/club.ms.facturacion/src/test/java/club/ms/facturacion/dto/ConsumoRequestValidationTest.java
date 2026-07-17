package club.ms.facturacion.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Equivalente Java de CF-CN-CONS-02 (consumoValidacion.test.js del backend monolitico
// original): "Evitar monto vacio o negativo".
class ConsumoRequestValidationTest {

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

    private ConsumoRequest requestValido() {
        ConsumoRequest request = new ConsumoRequest();
        request.setIdSocio(1L);
        request.setServicio("Cafeteria");
        request.setMonto(new BigDecimal("45.50"));
        return request;
    }

    // Test 1 (equivalente) — monto negativo -50.00
    @Test
    void montoNegativoEsRechazado() {
        ConsumoRequest request = requestValido();
        request.setMonto(new BigDecimal("-50.00"));

        Set<ConstraintViolation<ConsumoRequest>> violaciones = validator.validate(request);

        assertFalse(violaciones.isEmpty());
        assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("monto")));
    }

    // Test 2 — monto cero
    @Test
    void montoCeroEsRechazado() {
        ConsumoRequest request = requestValido();
        request.setMonto(BigDecimal.ZERO);

        Set<ConstraintViolation<ConsumoRequest>> violaciones = validator.validate(request);

        assertFalse(violaciones.isEmpty());
        assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("monto")));
    }

    // Test 3 (Negativo) — monto vacio/null
    @Test
    void montoNuloEsRechazado() {
        ConsumoRequest request = requestValido();
        request.setMonto(null);

        Set<ConstraintViolation<ConsumoRequest>> violaciones = validator.validate(request);

        assertFalse(violaciones.isEmpty());
        assertTrue(violaciones.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("monto")));
    }

    @Test
    void montoPositivoEsAceptado() {
        ConsumoRequest request = requestValido();

        Set<ConstraintViolation<ConsumoRequest>> violaciones = validator.validate(request);

        assertTrue(violaciones.isEmpty());
    }
}
