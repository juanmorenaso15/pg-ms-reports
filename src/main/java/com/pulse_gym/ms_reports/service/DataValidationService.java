package com.pulse_gym.ms_reports.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulse_gym.lb_common.entity.reports.LogIncidencias;
import com.pulse_gym.ms_reports.repository.LogIncidenciasRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataValidationService {

    private final LogIncidenciasRepository logIncidenciasRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Registra una incidencia en la base de datos.
     *
     * @param tipoDato      Tipo de dato (ACCESO, PAGO, MAQUINA)
     * @param datosRecibidos Objeto recibido (se convertirá a JSON)
     * @param error          Descripción del error
     */
    public void registrarIncidencia(String tipoDato, Object datosRecibidos, String error) {
        LogIncidencias incidencia = new LogIncidencias();
        incidencia.setTipoDato(tipoDato);
        incidencia.setErrorDescripcion(error);

        try {
            String json = objectMapper.writeValueAsString(datosRecibidos);
            incidencia.setDatosRecibidos(json);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar datos recibidos: {}", e.getMessage());
            incidencia.setDatosRecibidos("Error al serializar: " + e.getMessage());
        }

        logIncidenciasRepository.save(incidencia);
        log.warn("Incidencia registrada: {} - {}", tipoDato, error);
    }
}