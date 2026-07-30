package com.pulse_gym.ms_reports.service;

import com.pulse_gym.lb_common.dto.EventoAccesoRequestDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.entity.reports.EventoAcceso;
import com.pulse_gym.lb_common.enums.EnumTipoAcceso;
import com.pulse_gym.ms_reports.repository.EventoAccesoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoAccesoService {

    /**
     * Repositorio para la entidad EventoAcceso
     */
    private final EventoAccesoRepository eventoAccesoRepository;

    /**
     * Procesa un evento de acceso recibido desde otro microservicio.
     * Valida los datos del evento y lo guarda en la base de datos.
     * 
     * @param request DTO con los datos del evento de acceso
     * @return MessegeGlobalDTO con el resultado del procesamiento
     */
    @Transactional
    public MessegeGlobalDTO procesarEventoAcceso(EventoAccesoRequestDTO request) {
        
        if (request.getSocioId() == null) {
            log.warn("Evento de acceso rechazado: falta socioId");
            return new MessegeGlobalDTO("Error: falta el identificador del socio");
        }
        if (request.getFechaHora() == null) {
            log.warn("Evento de acceso rechazado: falta fechaHora");
            return new MessegeGlobalDTO("Error: falta la fecha y hora del evento");
        }
        if (request.getTipoAcceso() == null || request.getTipoAcceso().isEmpty()) {
            log.warn("Evento de acceso rechazado: falta tipoAcceso");
            return new MessegeGlobalDTO("Error: falta el tipo de acceso");
        }
        if (request.getTipoEvento() == null || request.getTipoEvento().isEmpty()) {
            // Por defecto asumimos que es ENTRADA
            request.setTipoEvento("ENTRADA");
        }

        EventoAcceso evento = new EventoAcceso();
        evento.setSocioIdentificacion(request.getSocioId());
        evento.setFechaRegistro(request.getFechaHora());
        evento.setTipoAcceso(EnumTipoAcceso.valueOf(request.getTipoAcceso().toUpperCase()));

       
        eventoAccesoRepository.save(evento);

        log.info("Evento de acceso guardado correctamente para socio ID: {}", request.getSocioId());

        return new MessegeGlobalDTO("Evento de acceso procesado correctamente");
    }
}