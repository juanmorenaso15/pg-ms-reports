package com.pulse_gym.ms_reports.controller;

import com.pulse_gym.lb_common.dto.TendenciaPeriodoDTO;
import com.pulse_gym.lb_common.exception.SecurityAuthorizationException;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_reports.service.TendenciaAfluenciaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes/tendencia")
@RequiredArgsConstructor
@Slf4j
public class TendenciaAfluenciaController {

    private final TendenciaAfluenciaService tendenciaService;

    /**
     * Obtiene la tendencia de afluencia (semanal o mensual).
     * Solo accesible para Administradores.
     *
     * @param tipoReporte "SEMANAL" o "MENSUAL"
     * @param fechaReferencia Fecha de referencia (YYYY-MM-DD)
     * @param userRol Rol del usuario autenticado
     * @return DTO con la tendencia
     */
    @GetMapping
    public ResponseEntity<TendenciaPeriodoDTO> obtenerTendencia(
            @RequestParam String tipoReporte,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReferencia,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        try {
            ValidacionDeRoles.validarAdmin(userRol);
        } catch (SecurityAuthorizationException e) {
            log.warn("Intento de acceso no autorizado a tendencia: {}", e.getMessage());
            throw e;
        }

        log.info("Generando reporte de tendencia {} para fecha: {}", tipoReporte, fechaReferencia);

        long startTime = System.currentTimeMillis();
        TendenciaPeriodoDTO resultado = tendenciaService.obtenerTendencia(tipoReporte, fechaReferencia);
        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("Reporte de tendencia generado en {} ms", elapsedTime);

        if (elapsedTime > 5000) {
            log.warn("El tiempo de respuesta del reporte excedió los 5 segundos: {} ms", elapsedTime);
        }

        return ResponseEntity.ok(resultado);
    }
}