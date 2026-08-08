package com.pulse_gym.ms_reports.controller;

import com.pulse_gym.lb_common.dto.ReporteMoraResponseDTO;
import com.pulse_gym.lb_common.exception.SecurityAuthorizationException;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_reports.service.ExportacionMoraService;
import com.pulse_gym.ms_reports.service.ReporteMoraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reportes/mora")
@RequiredArgsConstructor
@Slf4j
public class ReporteMoraController {

    private final ReporteMoraService reporteMoraService;
    private final ExportacionMoraService exportacionMoraService;

    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @GetMapping
    public ResponseEntity<ReporteMoraResponseDTO> obtenerMora(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        validarRol(userRol);
        log.info("Consultando socios en mora con filtros: inicio={}, fin={}", fechaInicio, fechaFin);

        long start = System.currentTimeMillis();
        ReporteMoraResponseDTO dto = reporteMoraService.obtenerSociosEnMora(fechaInicio, fechaFin);
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > 5000) log.warn("Tiempo de respuesta > 5s: {} ms", elapsed);

        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarPDF(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        validarRol(userRol);
        log.info("Exportando PDF de mora");

        ReporteMoraResponseDTO dto = reporteMoraService.obtenerSociosEnMora(fechaInicio, fechaFin);
        byte[] pdf = exportacionMoraService.generarPDFMora(dto, fechaInicio, fechaFin);

        String filename = "socios_mora_" + LocalDate.now().format(FILE_DATE_FORMATTER) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(value = "/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        validarRol(userRol);
        log.info("Exportando Excel de mora");

        ReporteMoraResponseDTO dto = reporteMoraService.obtenerSociosEnMora(fechaInicio, fechaFin);
        byte[] excel = exportacionMoraService.generarExcelMora(dto, fechaInicio, fechaFin);

        String filename = "socios_mora_" + LocalDate.now().format(FILE_DATE_FORMATTER) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    private void validarRol(String userRol) {
        try {
            ValidacionDeRoles.validarAdmin(userRol);
        } catch (SecurityAuthorizationException e) {
            log.warn("Acceso no autorizado a reporte de mora: {}", e.getMessage());
            throw e;
        }
    }
}