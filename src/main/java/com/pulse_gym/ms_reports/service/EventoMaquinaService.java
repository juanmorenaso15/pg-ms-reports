package com.pulse_gym.ms_reports.service;

import com.pulse_gym.lb_common.dto.EventoMaquinaRequestDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.entity.reports.EventoMaquina;
import com.pulse_gym.lb_common.enums.EnumEstado;
import com.pulse_gym.ms_reports.repository.EventoMaquinaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoMaquinaService {

    /**
     * inyeccion de dependencias del repositorio de eventos de máquina
     */
    private final EventoMaquinaRepository eventoMaquinaRepository;

    /**
     * Procesa un evento de máquina y lo guarda en la base de datos.
     *
     * @param request el objeto EventoMaquinaRequestDTO que contiene los datos del evento de máquina
     * @return un objeto MessegeGlobalDTO con el resultado del procesamiento
     */
    @Transactional
    public MessegeGlobalDTO procesarEventoMaquina(EventoMaquinaRequestDTO request) {
        if (request.getNombreMaquina() == null || request.getNombreMaquina().isBlank()) {
            log.warn("Reporte de máquina rechazado: falta nombre de máquina");
            return new MessegeGlobalDTO("Error: el nombre de la máquina es obligatorio");
        }
        if (request.getEstado() == null || request.getEstado().isBlank()) {
            log.warn("Reporte de máquina rechazado: falta estado");
            return new MessegeGlobalDTO("Error: el estado es obligatorio");
        }
        if (request.getFechaReporte() == null) {
            request.setFechaReporte(LocalDate.now());
        }

        EventoMaquina evento = new EventoMaquina();
        evento.setNombreMaquina(request.getNombreMaquina());
        evento.setEstado(EnumEstado.valueOf(request.getEstado().toUpperCase()));
        evento.setFechaReporte(request.getFechaReporte());
        evento.setDescripcionProblema(request.getDescripcionProblema());
        evento.setFechaReparacion(request.getFechaReparacion());

        eventoMaquinaRepository.save(evento);

        log.info("Reporte de máquina guardado correctamente: {}", request.getNombreMaquina());

        return new MessegeGlobalDTO("Reporte de máquina procesado correctamente");
    }
}