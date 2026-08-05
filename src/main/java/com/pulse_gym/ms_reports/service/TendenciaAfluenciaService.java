package com.pulse_gym.ms_reports.service;

import com.pulse_gym.lb_common.dto.DatoPeriodoDTO;
import com.pulse_gym.lb_common.dto.TendenciaPeriodoDTO;
import com.pulse_gym.ms_reports.repository.EventoAccesoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TendenciaAfluenciaService {

    private final EventoAccesoRepository eventoAccesoRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Obtiene la tendencia de afluencia para un período (semanal o mensual).
     *
     * @param tipoReporte "SEMANAL" o "MENSUAL"
     * @param fechaReferencia Fecha de referencia para el análisis
     * @return DTO con la tendencia
     */
    public TendenciaPeriodoDTO obtenerTendencia(String tipoReporte, LocalDate fechaReferencia) {
        if (!"SEMANAL".equalsIgnoreCase(tipoReporte) && !"MENSUAL".equalsIgnoreCase(tipoReporte)) {
            throw new IllegalArgumentException("Tipo de reporte debe ser SEMANAL o MENSUAL");
        }

        PeriodoDTO periodoActual = calcularPeriodo(tipoReporte, fechaReferencia);
        PeriodoDTO periodoAnterior = calcularPeriodoAnterior(tipoReporte, periodoActual);

        log.info("Analizando tendencia {} para período: {} a {}", tipoReporte, 
                 periodoActual.getInicio(), periodoActual.getFin());

        List<DatoPeriodoDTO> datosActuales = obtenerDatosPorPeriodo(periodoActual);
        List<DatoPeriodoDTO> datosAnteriores = obtenerDatosPorPeriodo(periodoAnterior);

        long totalActual = datosActuales.stream().mapToLong(DatoPeriodoDTO::getTotalSocios).sum();
        long totalAnterior = datosAnteriores.stream().mapToLong(DatoPeriodoDTO::getTotalSocios).sum();

        if (totalActual == 0 && totalAnterior == 0) {
            TendenciaPeriodoDTO dto = new TendenciaPeriodoDTO();
            dto.setTipoReporte(tipoReporte);
            dto.setPeriodo(formatearPeriodo(periodoActual));
            dto.setMensaje("Datos insuficientes para el análisis");
            return dto;
        }

        Double variacion = null;
        if (totalAnterior > 0) {
            variacion = ((double) totalActual - totalAnterior) / totalAnterior * 100;
        }

        TendenciaPeriodoDTO dto = new TendenciaPeriodoDTO();
        dto.setTipoReporte(tipoReporte);
        dto.setPeriodo(formatearPeriodo(periodoActual));
        dto.setDatosPeriodoActual(datosActuales);
        dto.setDatosPeriodoAnterior(datosAnteriores);
        dto.setVariacionPorcentual(variacion);

        if (datosActuales.isEmpty()) {
            dto.setMensaje("No hay datos para el período seleccionado");
        } else if (datosAnteriores.isEmpty()) {
            dto.setMensaje("No hay datos para comparar con el período anterior");
        }

        return dto;
    }

    /**
     * Calcula los límites del período según el tipo (semanal o mensual).
     */
    private PeriodoDTO calcularPeriodo(String tipo, LocalDate fechaReferencia) {
        if ("SEMANAL".equalsIgnoreCase(tipo)) {
            LocalDate inicio = fechaReferencia.with(java.time.DayOfWeek.MONDAY);
            LocalDate fin = fechaReferencia.with(java.time.DayOfWeek.SUNDAY);
            return new PeriodoDTO(inicio, fin);
        } else {
            LocalDate inicio = fechaReferencia.withDayOfMonth(1);
            LocalDate fin = fechaReferencia.withDayOfMonth(fechaReferencia.lengthOfMonth());
            return new PeriodoDTO(inicio, fin);
        }
    }

    /**
     * Calcula el período anterior al actual.
     */
    private PeriodoDTO calcularPeriodoAnterior(String tipo, PeriodoDTO actual) {
        LocalDate inicioAnterior, finAnterior;
        if ("SEMANAL".equalsIgnoreCase(tipo)) {
            inicioAnterior = actual.getInicio().minusWeeks(1);
            finAnterior = actual.getFin().minusWeeks(1);
        } else { // MENSUAL
            LocalDate mesAnterior = actual.getInicio().minusMonths(1);
            inicioAnterior = mesAnterior.withDayOfMonth(1);
            finAnterior = mesAnterior.withDayOfMonth(mesAnterior.lengthOfMonth());
        }
        return new PeriodoDTO(inicioAnterior, finAnterior);
    }

    /**
     * Obtiene los datos (fecha, total) de la base de datos para un período.
     */
    private List<DatoPeriodoDTO> obtenerDatosPorPeriodo(PeriodoDTO periodo) {
        LocalDateTime inicio = periodo.getInicio().atStartOfDay();
        LocalDateTime fin = periodo.getFin().atTime(LocalTime.MAX);

        List<Object[]> resultados = eventoAccesoRepository.countByDayGrouped(inicio, fin);

        return resultados.stream()
                .map(obj -> {
                    LocalDate fecha = (LocalDate) obj[0];
                    Long total = ((Number) obj[1]).longValue();
                    return new DatoPeriodoDTO(fecha, fecha.format(DATE_FORMATTER), total);
                })
                .collect(Collectors.toList());
    }

    /**
     * Formatea el período para mostrar.
     */
    private String formatearPeriodo(PeriodoDTO periodo) {
        return periodo.getInicio().format(DATE_FORMATTER) + " al " + periodo.getFin().format(DATE_FORMATTER);
    }

    /**
     * Clase interna para representar un período.
     */
    private static class PeriodoDTO {
        private final LocalDate inicio;
        private final LocalDate fin;

        public PeriodoDTO(LocalDate inicio, LocalDate fin) {
            this.inicio = inicio;
            this.fin = fin;
        }

        public LocalDate getInicio() { return inicio; }
        public LocalDate getFin() { return fin; }
    }
}