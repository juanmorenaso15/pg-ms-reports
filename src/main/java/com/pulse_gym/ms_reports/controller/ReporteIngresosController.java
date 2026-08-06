package com.pulse_gym.ms_reports.controller;

import com.pulse_gym.lb_common.dto.ReporteIngresosDiariosDTO;
import com.pulse_gym.lb_common.exception.SecurityAuthorizationException;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.lb_common.entity.reports.EventoPago;
import com.pulse_gym.ms_reports.service.ExportacionIngresosService;
import com.pulse_gym.ms_reports.service.ReporteIngresosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reportes/ingresos")
@RequiredArgsConstructor
@Slf4j
public class ReporteIngresosController {

    /**
     * Servicio para obtener los datos del reporte de ingresos.
     */
    private final ReporteIngresosService reporteIngresosService;

    /**
     * Servicio para exportar los datos del reporte de ingresos a PDF o Excel.
     */
    private final ExportacionIngresosService exportacionIngresosService;

    /**
     * Formato de fecha para mostrar en el reporte.
     */
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Obtiene el reporte de ingresos diarios para una fecha específica.
     *
     * @param fecha La fecha para la cual se desea obtener el reporte.
     * @param userRol El rol del usuario que realiza la solicitud (opcional).
     * @return Un objeto ReporteIngresosDiariosDTO con los detalles del reporte.
     */
    @GetMapping("/diarios")
    public ResponseEntity<ReporteIngresosDiariosDTO> obtenerIngresosDiarios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        validarRol(userRol);

        log.info("Consultando ingresos para fecha: {}", fecha);
        long startTime = System.currentTimeMillis();

        ReporteIngresosDiariosDTO dto = reporteIngresosService.obtenerIngresosDiarios(fecha);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Ingresos consultados en {} ms", elapsed);
        if (elapsed > 2000) {
            log.warn("Tiempo de respuesta > 2 segundos: {} ms", elapsed);
        }

        return ResponseEntity.ok(dto);
    }

    /**
     * Exporta el reporte de ingresos diarios a PDF.
     * 
     * @param fecha La fecha para la cual se desea exportar el reporte.
     * @param userRol El rol del usuario que realiza la solicitud (opcional).
     * @return Un arreglo de bytes que representa el archivo PDF generado.
     */
    @GetMapping(value = "/diarios/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportarPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        validarRol(userRol);

        log.info("Exportando PDF de ingresos para fecha: {}", fecha);

        ReporteIngresosDiariosDTO dto = reporteIngresosService.obtenerIngresosDiarios(fecha);
        List<EventoPago> pagos = reporteIngresosService.obtenerPagosPorFecha(fecha);

        byte[] pdfBytes = exportacionIngresosService.generarPDFIngresosDiarios(
                fecha, pagos, dto.getTotalIngresos(), dto.getCantidadPagos());

        String filename = "ingresos_" + fecha.format(FILE_DATE_FORMATTER) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Exporta el reporte de ingresos diarios a Excel.
     *
     * @param fecha La fecha para la cual se desea exportar el reporte.
     * @param userRol El rol del usuario que realiza la solicitud (opcional).
     * @return Un arreglo de bytes que representa el archivo Excel generado.
     */
    @GetMapping(value = "/diarios/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        validarRol(userRol);

        log.info("Exportando Excel de ingresos para fecha: {}", fecha);

        ReporteIngresosDiariosDTO dto = reporteIngresosService.obtenerIngresosDiarios(fecha);
        List<EventoPago> pagos = reporteIngresosService.obtenerPagosPorFecha(fecha);

        byte[] excelBytes = exportacionIngresosService.generarExcelIngresosDiarios(
                fecha, pagos, dto.getTotalIngresos(), dto.getCantidadPagos());

        String filename = "ingresos_" + fecha.format(FILE_DATE_FORMATTER) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    private void validarRol(String userRol) {
        try {
            ValidacionDeRoles.validarAdmin(userRol);
        } catch (SecurityAuthorizationException e) {
            log.warn("Intento de acceso no autorizado a reporte de ingresos: {}", e.getMessage());
            throw e;
        }
    }
}