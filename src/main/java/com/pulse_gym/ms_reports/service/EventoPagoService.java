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

    /**
     * Repositorio para manejar las operaciones de la entidad EventoPago en la base de datos.
     */
    private final EventoPagoRepository eventoPagoRepository;
    
    /**
     * Servicio para validar los datos recibidos y registrar incidencias en caso de errores.
     */
    private final DataValidationService validationService;

    /**
     * Procesa un evento de pago, validando los datos y registrando el evento en la base de datos.
     * @param request Objeto que contiene los datos del evento de pago
     * @return Mensaje indicando el resultado del procesamiento
     */
    @Transactional
    public MessegeGlobalDTO procesarEventoPago(EventoPagoRequestDTO request) {
        String tipoDato = "PAGO";

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

        if (request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            String error = "Monto debe ser mayor a 0: " + request.getMonto();
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }

        if (request.getFechaPago().isAfter(LocalDateTime.now())) {
            String error = "Fecha de pago futura no permitida: " + request.getFechaPago();
            validationService.registrarIncidencia(tipoDato, request, error);
            return new MessegeGlobalDTO("Error: " + error);
        }

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

    @Transactional
    public MessegeGlobalDTO anularEventoPago(Long socioId, LocalDateTime fechaPago) {
        String socioIdStr = socioId.toString();
        EventoPago evento = eventoPagoRepository.findBySocioIdentificadorAndFechaPago(socioIdStr, fechaPago)
                .orElse(null);

        if (evento != null) {
            evento.setAnulado(true);
            eventoPagoRepository.save(evento);
            log.info("Evento de pago marcado como anulado para socio ID: {} en fecha: {}", socioId, fechaPago);
            return new MessegeGlobalDTO("Evento de pago anulado correctamente en reportes");
        }
        
        log.warn("No se encontró el evento de pago para anular con socio ID: {} y fecha: {}", socioId, fechaPago);
        return new MessegeGlobalDTO("Evento de pago no encontrado en reportes para anular");
    }
}