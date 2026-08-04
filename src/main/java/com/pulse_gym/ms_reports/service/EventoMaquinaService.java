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

    private final EventoMaquinaRepository eventoMaquinaRepository;
    private final DataValidationService validationService;

    @Transactional
    public MessegeGlobalDTO procesarEventoMaquina(EventoMaquinaRequestDTO request) {
        String tipoDato = "MAQUINA";

        if (request.getNombreMaquina() == null || request.getNombreMaquina().isBlank()) {
            String error = "Campo obligatorio faltante: nombreMaquina";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }
        if (request.getEstado() == null || request.getEstado().isBlank()) {
            String error = "Campo obligatorio faltante: estado";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }
        if (request.getFechaReporte() == null) {
            String error = "Campo obligatorio faltante: fechaReporte. Se asigna fecha actual.";
            validationService.registrarIncidencia(tipoDato, request, error);
            request.setFechaReporte(LocalDate.now());
        }

        EnumEstado estadoEnum;
        try {
            estadoEnum = EnumEstado.valueOf(request.getEstado().toUpperCase());
        } catch (IllegalArgumentException e) {
            String error = "Formato inválido para estado: " + request.getEstado() +
                    ". Valores permitidos: OPERATIVO, MANTENIMIENTO, FUERA_DE_SERVICIO, RETIRADO";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }

        if (request.getFechaReporte().isAfter(LocalDate.now())) {
            String error = "Fecha de reporte futura no permitida: " + request.getFechaReporte();
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }

        if (estadoEnum == EnumEstado.OPERATIVO) {
            if (request.getFechaReparacion() == null) {
                String error = "Falta fecha de reparación para una máquina en estado OPERATIVO";
                validationService.registrarIncidencia(tipoDato, request, error);
                return new MessegeGlobalDTO("Error: " + error);
            }
        }

        EventoMaquina evento = new EventoMaquina();
        evento.setNombreMaquina(request.getNombreMaquina());
        evento.setEstado(estadoEnum);
        evento.setFechaReporte(request.getFechaReporte());
        evento.setDescripcionProblema(request.getDescripcionProblema());
        evento.setFechaReparacion(request.getFechaReparacion());

        eventoMaquinaRepository.save(evento);
        log.info("Reporte de máquina guardado correctamente: {}", request.getNombreMaquina());

        return new MessegeGlobalDTO("Reporte de máquina procesado correctamente");
    }
}