package com.pulse_gym.ms_reports.service;

import com.pulse_gym.ms_reports.repository.EventoAccesoRepository;
import com.pulse_gym.ms_reports.repository.EventoMaquinaRepository;
import com.pulse_gym.ms_reports.repository.EventoPagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LimpiezaHistoricaService {

    private final EventoAccesoRepository eventoAccesoRepository;
    private final EventoPagoRepository eventoPagoRepository;
    private final EventoMaquinaRepository eventoMaquinaRepository;

    /**
     * Tarea programada que se ejecuta el primer día de cada mes a las 2:00 AM.
     * Elimina registros con más de 2 años de antigüedad.
     */
    @Scheduled(cron = "0 0 2 1 * ?") // Primer día de cada mes a las 2 AM
    @Transactional
    public void limpiarDatosHistoricos() {
        LocalDateTime fechaCorte = LocalDateTime.now().minusYears(2);
        log.info("Iniciando limpieza de datos históricos anteriores a: {}", fechaCorte);

        try {
            long eliminadosAcceso = eventoAccesoRepository.deleteAllByFechaRegistroBefore(fechaCorte);
            log.info("Eventos de acceso eliminados: {}", eliminadosAcceso);

            long eliminadosPago = eventoPagoRepository.deleteAllByFechaPagoBefore(fechaCorte);
            log.info("Eventos de pago eliminados: {}", eliminadosPago);

            long eliminadosMaquina = eventoMaquinaRepository.deleteAllByFechaReporteBefore(fechaCorte);
            log.info("Eventos de máquina eliminados: {}", eliminadosMaquina);

        } catch (Exception e) {
            log.error("Error durante la limpieza de datos históricos: {}", e.getMessage(), e);
        }
    }
}