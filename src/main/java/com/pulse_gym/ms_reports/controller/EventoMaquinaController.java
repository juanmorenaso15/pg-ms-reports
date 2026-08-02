package com.pulse_gym.ms_reports.controller;

import com.pulse_gym.lb_common.dto.EventoMaquinaRequestDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.ms_reports.service.EventoMaquinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos/maquina")
@RequiredArgsConstructor
@Slf4j
public class EventoMaquinaController {

    /**
     * Repositorio para la entidad EventoMaquina
     */
    private final EventoMaquinaService eventoMaquinaService;

    /**
     * Recibe un evento de máquina desde otro microservicio, lo procesa y lo guarda en la base de datos.
     * 
     * @param request DTO con los datos del evento de máquina
     * @return ResponseEntity con el resultado del procesamiento
     */
    @PostMapping
    public ResponseEntity<MessegeGlobalDTO> recibirEventoMaquina(
            @Valid @RequestBody EventoMaquinaRequestDTO request) {
        log.info("Recibiendo reporte de máquina: {}", request.getNombreMaquina());
        MessegeGlobalDTO response = eventoMaquinaService.procesarEventoMaquina(request);
        return ResponseEntity.ok(response);
    }
}