package com.pulse_gym.ms_reports.controller;

import com.pulse_gym.lb_common.dto.EventoPagoRequestDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.ms_reports.service.EventoPagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos/pago")
@RequiredArgsConstructor
@Slf4j
public class EventoPagoController {

    /**
     * Repositorio para la entidad EventoPago
     */
    private final EventoPagoService eventoPagoService;

    /**
     * Recibe un evento de pago desde otro microservicio, lo procesa y lo guarda en la base de datos.
     * 
     * @param request DTO con los datos del evento de pago
     * @return ResponseEntity con el resultado del procesamiento
     */
    @PostMapping
    public ResponseEntity<MessegeGlobalDTO> recibirEventoPago(
            @Valid @RequestBody EventoPagoRequestDTO request) {
        log.info("Recibiendo evento de pago para socio ID: {}", request.getSocioId());
        MessegeGlobalDTO response = eventoPagoService.procesarEventoPago(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para anular un evento de pago específico.
     * 
     * @param socioId   ID del socio asociado al evento de pago
     * @param fechaPago Fecha del pago para identificar el evento
     * @return ResponseEntity con el resultado de la anulación
     */
    @PutMapping("/anular")
    public ResponseEntity<MessegeGlobalDTO> anularEventoPago(
            @RequestParam("socioId") Long socioId,
            @RequestParam("fechaPago") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaPago) {
        log.info("Recibiendo solicitud para anular evento de pago de socio ID: {}", socioId);
        MessegeGlobalDTO response = eventoPagoService.anularEventoPago(socioId, fechaPago);
        return ResponseEntity.ok(response);
    }
}