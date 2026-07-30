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
     * Repositorio para la entidad EventoPago
     */
    private final EventoPagoRepository eventoPagoRepository;

    /**
     * Procesa un evento de pago recibido desde otro microservicio.
     * Valida los datos del evento y lo guarda en la base de datos.
     * 
     * @param request DTO con los datos del evento de pago
     * @return MessegeGlobalDTO con el resultado del procesamiento
     */
    @Transactional
    public MessegeGlobalDTO procesarEventoPago(EventoPagoRequestDTO request) {
        // Validaciones
        if (request.getSocioId() == null) {
            log.warn("Evento de pago rechazado: falta socioId");
            return new MessegeGlobalDTO("Error: falta el identificador del socio");
        }
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Evento de pago rechazado: monto inválido");
            return new MessegeGlobalDTO("Error: monto inválido o menor o igual a cero");
        }
        if (request.getFechaPago() == null) {
            request.setFechaPago(LocalDateTime.now());
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
}