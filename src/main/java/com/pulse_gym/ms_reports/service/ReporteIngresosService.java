package com.pulse_gym.ms_reports.service;

import com.pulse_gym.lb_common.dto.ReporteIngresosDiariosDTO;
import com.pulse_gym.lb_common.entity.reports.EventoPago;
import com.pulse_gym.ms_reports.repository.EventoPagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

        if (total == null) total = BigDecimal.ZERO;
        if (cantidad == null) cantidad = 0L;

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
     * @return Una lista de objetos EventoPago correspondientes a la fecha especificada.
     */
    public List<EventoPago> obtenerPagosPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);
        return eventoPagoRepository.findByFechaPagoBetween(inicio, fin);
    }
}