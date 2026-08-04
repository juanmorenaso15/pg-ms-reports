package com.pulse_gym.ms_reports.service;

import com.pulse_gym.lb_common.dto.ReporteSociosPorDiaDTO;
import com.pulse_gym.ms_reports.repository.EventoAccesoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteAfluenciaService {

    private final EventoAccesoRepository eventoAccesoRepository;

    /**
     * Obtiene el total de socios que ingresaron en una fecha específica.
     * Considera solo eventos de tipo ENTRADA (o todos los accesos, según requieras).
     * 
     * @param fecha Fecha a consultar
     * @return DTO con la fecha y el total de socios
     */
    public ReporteSociosPorDiaDTO obtenerTotalSociosPorDia(LocalDate fecha) {
        log.info("Consultando total de socios para fecha: {}", fecha);

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        Long total = eventoAccesoRepository.countByFechaRegistroBetween(inicio, fin);

        ReporteSociosPorDiaDTO dto = new ReporteSociosPorDiaDTO();
        dto.setFecha(fecha);
        dto.setTotalSocios(total);

        if (total == 0) {
            dto.setMensaje("No hay registros en la fecha seleccionada");
        }

        return dto;
    }
}