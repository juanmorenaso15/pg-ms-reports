package com.pulse_gym.ms_reports.controller;

import com.pulse_gym.lb_common.dto.EventoAccesoRequestDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.ms_reports.service.EventoAccesoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos/acceso")
@RequiredArgsConstructor
@Slf4j
public class EventoAccesoController {

    /**
     *  Repositorio para la entidad EventoAcceso
     */
    private final EventoAccesoService eventoAccesoService;

    /**
     * Recibe un evento de acceso desde otro microservicio, lo procesa y lo guarda en la base de datos.
     * 
     * @param request DTO con los datos del evento de acceso
     * @return ResponseEntity con el resultado del procesamiento
     */
    @PostMapping
    public ResponseEntity<MessegeGlobalDTO> recibirEventoAcceso(
            @Valid @RequestBody EventoAccesoRequestDTO request) {
        log.info("Recibiendo evento de acceso para socio ID: {}", request.getSocioId());
        MessegeGlobalDTO response = eventoAccesoService.procesarEventoAcceso(request);
        return ResponseEntity.ok(response);
    }
}