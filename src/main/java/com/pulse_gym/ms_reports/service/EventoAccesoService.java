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
     * Repositorio para manejar las operaciones de la entidad EventoAcceso en la base de datos.
     */
    private final EventoAccesoRepository eventoAccesoRepository;

    /**
     * Servicio para validar los datos recibidos y registrar incidencias en caso de errores.
     */
    private final DataValidationService validationService;

    /**
     * Procesa un evento de acceso, validando los datos y registrando el evento en la base de datos.
     *
     * @param request Objeto que contiene los datos del evento de acceso
     * @return Mensaje indicando el resultado del procesamiento
     */
    @Transactional
    public MessegeGlobalDTO procesarEventoAcceso(EventoAccesoRequestDTO request) {
        String tipoDato = "ACCESO";

        if (request.getSocioId() == null || request.getSocioId() <= 0) {
            String error = "Campo obligatorio faltante o inválido: socioId";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }
        if (request.getFechaHora() == null) {
            String error = "Campo obligatorio faltante: fechaHora";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }
        if (request.getTipoAcceso() == null || request.getTipoAcceso().isBlank()) {
            String error = "Campo obligatorio faltante: tipoAcceso";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }
        if (request.getTipoEvento() == null || request.getTipoEvento().isBlank()) {
            request.setTipoEvento("ENTRADA");
        }

        EnumTipoAcceso tipoAccesoEnum;
        try {
            tipoAccesoEnum = EnumTipoAcceso.valueOf(request.getTipoAcceso().toUpperCase());
        } catch (IllegalArgumentException e) {
            String error = "Formato inválido para tipoAcceso: " + request.getTipoAcceso() +
                    ". Valores permitidos: WEB, APP, BIOMETRICO";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }

        if (!"ENTRADA".equalsIgnoreCase(request.getTipoEvento()) &&
                !"SALIDA".equalsIgnoreCase(request.getTipoEvento())) {
            String error = "Formato inválido para tipoEvento: " + request.getTipoEvento() +
                    ". Valores permitidos: ENTRADA, SALIDA";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }

        EventoAcceso evento = new EventoAcceso();
        evento.setSocioIdentificacion(request.getSocioId());
        evento.setFechaRegistro(request.getFechaHora());
        evento.setTipoAcceso(tipoAccesoEnum);

        eventoAccesoRepository.save(evento);
        log.info("Evento de acceso guardado correctamente para socio ID: {}", request.getSocioId());

        return new MessegeGlobalDTO("Evento de acceso procesado correctamente");
    }
}