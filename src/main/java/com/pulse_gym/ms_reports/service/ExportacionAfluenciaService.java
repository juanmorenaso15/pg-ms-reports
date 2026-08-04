package com.pulse_gym.ms_reports.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.pulse_gym.lb_common.entity.reports.EventoAcceso;
import com.pulse_gym.ms_reports.repository.EventoAccesoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportacionAfluenciaService {

    private final EventoAccesoRepository eventoAccesoRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Genera un PDF con la lista de accesos de una fecha específica.
     *
     * @param fecha Fecha a consultar
     * @return Array de bytes del PDF
     */
    public byte[] generarPDFSociosPorDia(LocalDate fecha) {
        List<EventoAcceso> accesos = obtenerAccesosPorFecha(fecha);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Título
            document.add(new Paragraph("Reporte de Socios por Día")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

            document.add(new Paragraph("Fecha: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(12)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

            document.add(new Paragraph(" "));

            // Resumen
            document.add(new Paragraph("Total de socios: " + accesos.size())
                    .setFontSize(14)
                    .setBold());

            document.add(new Paragraph(" "));

            // Tabla
            Table table = new Table(UnitValue.createPercentArray(new float[]{30, 30, 40}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Encabezados
            table.addCell(new Cell().add(new Paragraph("ID Socio").setBold()));
            table.addCell(new Cell().add(new Paragraph("Tipo Acceso").setBold()));
            table.addCell(new Cell().add(new Paragraph("Fecha/Hora").setBold()));

            // Datos
            for (EventoAcceso acceso : accesos) {
                table.addCell(new Cell().add(new Paragraph(acceso.getSocioIdentificacion().toString())));
                table.addCell(new Cell().add(new Paragraph(acceso.getTipoAcceso() != null ? acceso.getTipoAcceso().name() : "N/A")));
                table.addCell(new Cell().add(new Paragraph(acceso.getFechaRegistro().format(DATE_FORMATTER))));
            }

            document.add(table);

            // Footer
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Reporte generado por Pulse Gym")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    /**
     * Genera un Excel con la lista de accesos de una fecha específica.
     *
     * @param fecha Fecha a consultar
     * @return Array de bytes del Excel
     */
    public byte[] generarExcelSociosPorDia(LocalDate fecha) {
        List<EventoAcceso> accesos = obtenerAccesosPorFecha(fecha);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Usar Apache POI Cell y Sheet (sin conflictos porque están en paquete diferente)
            Sheet sheet = workbook.createSheet("Socios por día");

            // Estilo para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Crear fila de encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID Socio", "Tipo Acceso", "Fecha/Hora"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Datos
            int rowNum = 1;
            for (EventoAcceso acceso : accesos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(acceso.getSocioIdentificacion());
                row.createCell(1).setCellValue(acceso.getTipoAcceso() != null ? acceso.getTipoAcceso().name() : "N/A");
                row.createCell(2).setCellValue(acceso.getFechaRegistro().format(DATE_FORMATTER));
            }

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Resumen
            int summaryRowNum = rowNum + 2;
            Row summaryRow = sheet.createRow(summaryRowNum);
            org.apache.poi.ss.usermodel.Cell summaryCell = summaryRow.createCell(0);
            summaryCell.setCellValue("Total de socios: " + accesos.size());
            CellStyle summaryStyle = workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryFont.setFontHeightInPoints((short) 14);
            summaryStyle.setFont(summaryFont);
            summaryCell.setCellStyle(summaryStyle);

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el Excel", e);
        }
    }

    /**
     * Obtiene los accesos de una fecha específica.
     */
    private List<EventoAcceso> obtenerAccesosPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        List<EventoAcceso> accesos = eventoAccesoRepository.findByFechaRegistroBetweenOrderByFechaRegistroAsc(inicio, fin);
        log.info("Accesos encontrados para fecha {}: {}", fecha, accesos.size());
        return accesos;
    }
}