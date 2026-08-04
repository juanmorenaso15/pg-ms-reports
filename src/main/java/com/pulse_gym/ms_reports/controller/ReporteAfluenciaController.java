package com.pulse_gym.ms_reports.controller;

import com.pulse_gym.lb_common.dto.ReporteSociosPorDiaDTO;
import com.pulse_gym.lb_common.exception.SecurityAuthorizationException;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_reports.service.ReporteAfluenciaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes/afluencia")
@RequiredArgsConstructor
@Slf4j
public class ReporteAfluenciaController {

    private final ReporteAfluenciaService reporteAfluenciaService;

    /**
     * Endpoint para obtener el total de socios que ingresaron en una fecha específica.
     * Solo accesible para Administradores o Recepcionistas.
     *
     * @param fecha   Fecha en formato ISO (yyyy-MM-dd)
     * @param userRol Rol del usuario autenticado (header X-User-Rol)
     * @return DTO con el total de socios
     */
    @GetMapping("/socios-por-dia")
    public ResponseEntity<ReporteSociosPorDiaDTO> obtenerSociosPorDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        try {
            ValidacionDeRoles.validarAdminORecepcionista(userRol);
        } catch (SecurityAuthorizationException e) {
            log.warn("Intento de acceso no autorizado a reporte: {}", e.getMessage());
            throw e;
        }

        log.info("Generando reporte de socios por día para fecha: {}, usuario rol: {}", fecha, userRol);

        long startTime = System.currentTimeMillis();

        ReporteSociosPorDiaDTO resultado = reporteAfluenciaService.obtenerTotalSociosPorDia(fecha);

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("Reporte generado en {} ms", elapsedTime);

        if (elapsedTime > 3000) {
            log.warn("El tiempo de respuesta del reporte excedió los 3 segundos: {} ms", elapsedTime);
        }

        return ResponseEntity.ok(resultado);
    }
}