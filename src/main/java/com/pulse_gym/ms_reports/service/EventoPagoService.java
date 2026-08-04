package com.pulse_gym.ms_reports.service;

import com.pulse_gym.lb_common.dto.EventoPagoRequestDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.entity.reports.EventoPago;
import com.pulse_gym.ms_reports.repository.EventoPagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoPagoService {

    private final EventoPagoRepository eventoPagoRepository;
    private final DataValidationService validationService;

    @Transactional
    public MessegeGlobalDTO procesarEventoPago(EventoPagoRequestDTO request) {
        String tipoDato = "PAGO";

        // 1. Validar campos obligatorios
        if (request.getSocioId() == null || request.getSocioId() <= 0) {
            String error = "Campo obligatorio faltante o inválido: socioId";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }
        if (request.getMonto() == null) {
            String error = "Campo obligatorio faltante: monto";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }
        if (request.getFechaPago() == null) {
            // Si no viene fecha, asignamos la actual para continuar, pero registramos una advertencia? 
            // Mejor rechazar porque es obligatorio.
            String error = "Campo obligatorio faltante: fechaPago";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }
        if (request.getTipoMembresia() == null || request.getTipoMembresia().isBlank()) {
            // Podría ser opcional, pero lo marcamos como obligatorio para el reporte
            String error = "Campo obligatorio faltante: tipoMembresia";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }
        if (request.getMetodoPago() == null || request.getMetodoPago().isBlank()) {
            String error = "Campo obligatorio faltante: metodoPago";
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }

        // 2. Validar formato (números)
        if (request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            String error = "Monto debe ser mayor a 0: " + request.getMonto();
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }

        // 3. Validar que la fecha no sea futura (opcional pero buena práctica)
        if (request.getFechaPago().isAfter(LocalDateTime.now())) {
            String error = "Fecha de pago futura no permitida: " + request.getFechaPago();
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }

        // 4. Guardar
        EventoPago evento = new EventoPago();
        evento.setSocioIdentificador(request.getSocioId().toString());
        evento.setMonto(request.getMonto());
        evento.setFechaPago(request.getFechaPago());
        evento.setTipoMembresia(request.getTipoMembresia());
        evento.setMetodoPago(request.getMetodoPago());

        eventoPagoRepository.save(evento);
        log.info("Evento de pago guardado correctamente para socio ID: {}", request.getSocioId());

        return new MessegeGlobalDTO("Evento de pago procesado correctamente");
    }
}