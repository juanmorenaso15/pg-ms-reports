package com.pulse_gym.ms_reports.controller;

import com.pulse_gym.lb_common.exception.SecurityAuthorizationException;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_reports.service.ExportacionAfluenciaService;
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
@RequestMapping("/api/reportes/afluencia/exportar")
@RequiredArgsConstructor
@Slf4j
public class ExportacionAfluenciaController {

    private final ExportacionAfluenciaService exportacionService;

    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Exporta el reporte de socios por día en formato PDF.
     *
     * @param fecha   Fecha en formato ISO (yyyy-MM-dd)
     * @param userRol Rol del usuario autenticado (header X-User-Rol)
     * @return Archivo PDF para descargar
     */
    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        validarRol(userRol);

        log.info("Exportando PDF de socios por día para fecha: {}", fecha);

        byte[] pdfBytes = exportacionService.generarPDFSociosPorDia(fecha);

        String filename = "reporte_socios_" + fecha.format(FILE_DATE_FORMATTER) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Exporta el reporte de socios por día en formato Excel.
     *
     * @param fecha   Fecha en formato ISO (yyyy-MM-dd)
     * @param userRol Rol del usuario autenticado (header X-User-Rol)
     * @return Archivo Excel para descargar
     */
    @GetMapping(value = "/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        validarRol(userRol);

        log.info("Exportando Excel de socios por día para fecha: {}", fecha);

        byte[] excelBytes = exportacionService.generarExcelSociosPorDia(fecha);

        String filename = "reporte_socios_" + fecha.format(FILE_DATE_FORMATTER) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    private void validarRol(String userRol) {
        try {
            ValidacionDeRoles.validarAdminORecepcionista(userRol);
        } catch (SecurityAuthorizationException e) {
            log.warn("Intento de exportación no autorizado: {}", e.getMessage());
            throw e;
        }
    }
}