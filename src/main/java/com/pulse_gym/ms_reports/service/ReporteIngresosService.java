package com.pulse_gym.ms_reports.service;

import com.pulse_gym.lb_common.dto.MembresiaIngresoDTO;
import com.pulse_gym.lb_common.dto.ReporteIngresosDiariosDTO;
import com.pulse_gym.lb_common.dto.ReporteIngresosMensualesDTO;
import com.pulse_gym.lb_common.entity.reports.EventoPago;
import com.pulse_gym.ms_reports.repository.EventoPagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteIngresosService {

    /**
     * Repositorio para acceder a los eventos de pago.
     */
    private final EventoPagoRepository eventoPagoRepository;

    /**
     * Obtiene el reporte de ingresos diarios para una fecha específica.
     *
     * @param fecha La fecha para la cual se desea obtener el reporte.
     * @return Un objeto ReporteIngresosDiariosDTO con los detalles del reporte.
     */
    public ReporteIngresosDiariosDTO obtenerIngresosDiarios(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        BigDecimal total = eventoPagoRepository.sumMontoByFechaPagoBetween(inicio, fin);
        Long cantidad = eventoPagoRepository.countByFechaPago(fecha);

        if (total == null)
            total = BigDecimal.ZERO;
        if (cantidad == null)
            cantidad = 0L;

        ReporteIngresosDiariosDTO dto = new ReporteIngresosDiariosDTO();
        dto.setFecha(fecha);
        dto.setTotalIngresos(total);
        dto.setCantidadPagos(cantidad);

        if (cantidad == 0) {
            dto.setMensaje("No hay pagos registrados en esta fecha");
        }

        return dto;
    }

    /**
     * Obtiene la lista de eventos de pago para una fecha específica.
     *
     * @param fecha La fecha para la cual se desea obtener los eventos de pago.
     * @return Una lista de objetos EventoPago correspondientes a la fecha
     *         especificada.
     */
    public List<EventoPago> obtenerPagosPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);
        return eventoPagoRepository.findByFechaPagoBetween(inicio, fin);
    }

    /**
     * Obtiene el reporte de ingresos mensuales para un mes y año específicos.
     *
     * @param mes  El mes para el cual se desea obtener el reporte (1-12).
     * @param anio El año para el cual se desea obtener el reporte.
     * @return Un objeto ReporteIngresosMensualesDTO con los detalles del reporte.
     */
    public ReporteIngresosMensualesDTO obtenerIngresosMensuales(Long mes, Long anio) {
        int mesInt = mes.intValue();
        int anioInt = anio.intValue();
        LocalDate inicioMes = LocalDate.of(anioInt, mesInt, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        LocalDateTime inicio = inicioMes.atStartOfDay();
        LocalDateTime fin = finMes.atTime(LocalTime.MAX);

        log.info("Consultando ingresos mensuales: {}/{}", mes, anio);

        List<Object[]> resultados = eventoPagoRepository.sumMontoByTipoMembresiaBetween(inicio, fin);

        List<MembresiaIngresoDTO> detalle = new ArrayList<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        if (resultados == null || resultados.isEmpty()) {
            return new ReporteIngresosMensualesDTO(
                    mes, anio,
                    Collections.emptyList(),
                    BigDecimal.ZERO,
                    "No hay pagos registrados en este mes");
        }

        for (Object[] row : resultados) {
            String tipo = (String) row[0];
            BigDecimal monto = (BigDecimal) row[1];
            if (monto == null)
                monto = BigDecimal.ZERO;
            detalle.add(new MembresiaIngresoDTO(tipo, monto));
            totalGeneral = totalGeneral.add(monto);
        }

        return new ReporteIngresosMensualesDTO(mes, anio, detalle, totalGeneral, null);
    }

    public ReporteIngresosMensualesDTO obtenerIngresosPorMembresia(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        List<Object[]> resultados = eventoPagoRepository.sumMontoByTipoMembresiaBetween(inicio, fin);

        List<MembresiaIngresoDTO> detalle = new ArrayList<>();
        BigDecimal totalGeneral = BigDecimal.ZERO;

        if (resultados == null || resultados.isEmpty()) {
            return new ReporteIngresosMensualesDTO(0L, 0L, Collections.emptyList(), BigDecimal.ZERO,
                    "No hay ingresos en el período seleccionado");
        }

        for (Object[] row : resultados) {
            String tipo = (String) row[0];
            BigDecimal monto = (BigDecimal) row[1];
            if (monto == null)
                monto = BigDecimal.ZERO;
            detalle.add(new MembresiaIngresoDTO(tipo, monto));
            totalGeneral = totalGeneral.add(monto);
        }

        return new ReporteIngresosMensualesDTO(0L, 0L, detalle, totalGeneral, null);
    }
}