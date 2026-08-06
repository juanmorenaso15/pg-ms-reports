package com.pulse_gym.ms_reports.controller;

import com.pulse_gym.lb_common.dto.TendenciaPeriodoDTO;
import com.pulse_gym.lb_common.exception.SecurityAuthorizationException;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_reports.service.ExportacionTendenciaService;
import com.pulse_gym.ms_reports.service.TendenciaAfluenciaService;
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
@RequestMapping("/api/reportes/tendencia/exportar")
@RequiredArgsConstructor
@Slf4j
public class ExportacionTendenciaController {

    /**
     * Servicio para obtener la tendencia de afluencia.
     */
    private final TendenciaAfluenciaService tendenciaService;

    /**
     * Servicio para generar archivos de exportación (PDF, Excel).
     */
    private final ExportacionTendenciaService exportacionService;

    
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Exporta el reporte de tendencia en formato PDF.
     * @param tipoReporte "SEMANAL" o "MENSUAL"
     * @param fechaReferencia Fecha de referencia (YYYY-MM-DD)
     * @param userRol Rol del usuario autenticado
     * @return ResponseEntity con el archivo PDF
     */
    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarPDF(
            @RequestParam String tipoReporte,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReferencia,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        validarRol(userRol);

        log.info("Exportando PDF de tendencia {} para fecha: {}", tipoReporte, fechaReferencia);

        TendenciaPeriodoDTO dto = tendenciaService.obtenerTendencia(tipoReporte, fechaReferencia);
        byte[] pdfBytes = exportacionService.generarPDFTendencia(dto);

        String filename = "tendencia_" + tipoReporte.toLowerCase() + "_" + fechaReferencia.format(FILE_DATE_FORMATTER) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Exporta el reporte de tendencia en formato Excel.
     * @param tipoReporte 
     * @param fechaReferencia   
     * @param userRol
     * @return ResponseEntity con el archivo Excel
     */
    @GetMapping(value = "/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam String tipoReporte,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReferencia,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        validarRol(userRol);

        log.info("Exportando Excel de tendencia {} para fecha: {}", tipoReporte, fechaReferencia);

        TendenciaPeriodoDTO dto = tendenciaService.obtenerTendencia(tipoReporte, fechaReferencia);
        byte[] excelBytes = exportacionService.generarExcelTendencia(dto);

        String filename = "tendencia_" + tipoReporte.toLowerCase() + "_" + fechaReferencia.format(FILE_DATE_FORMATTER) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    private void validarRol(String userRol) {
        try {
            ValidacionDeRoles.validarAdmin(userRol);
        } catch (SecurityAuthorizationException e) {
            log.warn("Intento de exportación de tendencia no autorizado: {}", e.getMessage());
            throw e;
        }
    }
}