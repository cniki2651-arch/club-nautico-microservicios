package club.ms.facturacion.service;

import club.ms.facturacion.repository.FacturaRepository;
import club.ms.facturacion.repository.MorosidadInteresRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// Equivalente Java de pagoValidacion.test.js del backend monolitico original (calculo de
// intereses/mora sobre deuda vencida). El sistema nuevo no tiene "abono parcial" como el
// monolito viejo, asi que se prueba el calculo real que reemplaza esa logica: interes
// moratorio compuesto sobre facturas vencidas.
@ExtendWith(MockitoExtension.class)
class CobranzaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;
    @Mock
    private MorosidadInteresRepository morosidadInteresRepository;

    private CobranzaService cobranzaService;

    @BeforeEach
    void setUp() {
        cobranzaService = new CobranzaService(facturaRepository, morosidadInteresRepository);
        ReflectionTestUtils.setField(cobranzaService, "tasaInteresAnual", 0.15);
    }

    @Test
    void facturaAunNoVencidaNoGeneraDiasDeMora() {
        LocalDate vencimientoFuturo = LocalDate.now().plusDays(10);

        long dias = cobranzaService.calcularDiasMora(vencimientoFuturo);

        assertEquals(0, dias);
    }

    @Test
    void facturaVencidaHace30DiasGeneraDiasDeMoraPositivos() {
        LocalDate vencimientoPasado = LocalDate.now().minusDays(30);

        long dias = cobranzaService.calcularDiasMora(vencimientoPasado);

        assertEquals(30, dias);
    }

    @Test
    void interesEsCeroCuandoLaFacturaNoHaVencido() {
        BigDecimal interes = cobranzaService.calcularInteresParaFactura(
                new BigDecimal("500.00"), LocalDate.now().plusDays(5));

        assertEquals(0, BigDecimal.ZERO.compareTo(interes));
    }

    @Test
    void interesEsMayorACeroCuandoLaFacturaEstaVencida() {
        BigDecimal interes = cobranzaService.calcularInteresParaFactura(
                new BigDecimal("500.00"), LocalDate.now().minusDays(30));

        assertTrue(interes.compareTo(BigDecimal.ZERO) > 0,
                "El interes moratorio de una factura vencida debe ser mayor a cero");
    }

    @Test
    void aMasDiasDeMoraElInteresGeneradoEsMayor() {
        BigDecimal interes15Dias = cobranzaService.calcularInteresParaFactura(
                new BigDecimal("500.00"), LocalDate.now().minusDays(15));
        BigDecimal interes60Dias = cobranzaService.calcularInteresParaFactura(
                new BigDecimal("500.00"), LocalDate.now().minusDays(60));

        assertTrue(interes60Dias.compareTo(interes15Dias) > 0,
                "A mayor mora, el interes acumulado debe ser mayor");
    }
}
