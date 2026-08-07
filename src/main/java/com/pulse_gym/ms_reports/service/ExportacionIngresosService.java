package com.pulse_gym.ms_reports.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.pulse_gym.lb_common.dto.MembresiaIngresoDTO;
import com.pulse_gym.lb_common.dto.ReporteIngresosMensualesDTO;
import com.pulse_gym.lb_common.entity.reports.EventoPago;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportacionIngresosService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ==================== INGRESOS DIARIOS ====================

    public byte[] generarPDFIngresosDiarios(LocalDate fecha, List<EventoPago> pagos, BigDecimal total, Long cantidad) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Reporte de Ingresos Diarios")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Fecha: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total de ingresos: $" + total.toString())
                    .setFontSize(14)
                    .setBold());
            document.add(new Paragraph("Cantidad de pagos: " + cantidad)
                    .setFontSize(14));
            document.add(new Paragraph(" "));

            if (!pagos.isEmpty()) {
                Table table = new Table(UnitValue.createPercentArray(new float[]{30, 30, 40}))
                        .setWidth(UnitValue.createPercentValue(100));

                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("ID Socio").setBold()));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Monto").setBold()));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Fecha/Hora").setBold()));

                for (EventoPago pago : pagos) {
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(pago.getSocioIdentificador())));
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(pago.getMonto().toString())));
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(pago.getFechaPago().format(DATE_FORMATTER))));
                }
                document.add(table);
            } else {
                document.add(new Paragraph("No hay pagos registrados en esta fecha.")
                        .setFontColor(ColorConstants.RED));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Reporte generado por Pulse Gym")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF de ingresos diarios: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    public byte[] generarExcelIngresosDiarios(LocalDate fecha, List<EventoPago> pagos, BigDecimal total, Long cantidad) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Ingresos diarios");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reporte de Ingresos Diarios");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 2));

            Row infoRow1 = sheet.createRow(1);
            infoRow1.createCell(0).setCellValue("Fecha: " + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            Row infoRow2 = sheet.createRow(2);
            infoRow2.createCell(0).setCellValue("Total ingresos: $" + total.toString());

            Row infoRow3 = sheet.createRow(3);
            infoRow3.createCell(0).setCellValue("Cantidad de pagos: " + cantidad);

            int rowNum = 5;

            if (!pagos.isEmpty()) {
                Row headerRow = sheet.createRow(rowNum++);
                headerRow.createCell(0).setCellValue("ID Socio");
                headerRow.createCell(1).setCellValue("Monto");
                headerRow.createCell(2).setCellValue("Fecha/Hora");
                for (int i = 0; i < 3; i++) {
                    headerRow.getCell(i).setCellStyle(headerStyle);
                }

                for (EventoPago pago : pagos) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(pago.getSocioIdentificador());
                    row.createCell(1).setCellValue(pago.getMonto().doubleValue());
                    row.createCell(2).setCellValue(pago.getFechaPago().format(DATE_FORMATTER));
                }

                for (int i = 0; i < 3; i++) {
                    sheet.autoSizeColumn(i);
                }
            } else {
                Row emptyRow = sheet.createRow(rowNum);
                emptyRow.createCell(0).setCellValue("No hay pagos en esta fecha.");
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar Excel de ingresos diarios: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el Excel", e);
        }
    }

    // ==================== INGRESOS MENSUALES ====================

    public byte[] generarPDFIngresosMensuales(ReporteIngresosMensualesDTO dto) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Reporte de Ingresos Mensuales")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Mes: " + dto.getMes() + "/" + dto.getAnio())
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));

            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Tipo de Membresía").setBold()));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Total Ingresos").setBold()));

            for (MembresiaIngresoDTO item : dto.getDetalle()) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(item.getTipoMembresia())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("$" + item.getTotal().toString())));
            }

            com.itextpdf.layout.element.Cell totalLabelCell = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("TOTAL GENERAL").setBold());
            com.itextpdf.layout.element.Cell totalValueCell = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("$" + dto.getTotalGeneral().toString()).setBold());
            table.addCell(totalLabelCell);
            table.addCell(totalValueCell);

            document.add(table);

            if (dto.getMensaje() != null) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph(dto.getMensaje())
                        .setFontColor(ColorConstants.RED));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Reporte generado por Pulse Gym")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF de ingresos mensuales: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    public byte[] generarExcelIngresosMensuales(ReporteIngresosMensualesDTO dto) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Ingresos mensuales");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reporte de Ingresos Mensuales - " + dto.getMes() + "/" + dto.getAnio());
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

            int rowNum = 2;

            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Tipo de Membresía");
            headerRow.createCell(1).setCellValue("Total Ingresos");
            headerRow.getCell(0).setCellStyle(headerStyle);
            headerRow.getCell(1).setCellStyle(headerStyle);

            for (MembresiaIngresoDTO item : dto.getDetalle()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getTipoMembresia());
                row.createCell(1).setCellValue(item.getTotal().doubleValue());
            }

            Row totalRow = sheet.createRow(rowNum++);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTAL GENERAL");
            totalLabel.setCellStyle(boldStyle);
            Cell totalValue = totalRow.createCell(1);
            totalValue.setCellValue(dto.getTotalGeneral().doubleValue());
            totalValue.setCellStyle(boldStyle);

            if (dto.getMensaje() != null) {
                Row msgRow = sheet.createRow(rowNum++);
                Cell msgCell = msgRow.createCell(0);
                msgCell.setCellValue(dto.getMensaje());
                CellStyle redStyle = workbook.createCellStyle();
                Font redFont = workbook.createFont();
                redFont.setColor(IndexedColors.RED.getIndex());
                redStyle.setFont(redFont);
                msgCell.setCellStyle(redStyle);
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar Excel de ingresos mensuales: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el Excel", e);
        }
    }

    /**
     * Genera PDF para el reporte de ingresos agrupados por tipo de membresía en un rango de fechas.
     * @param dto
     * @param fechaInicio
     * @param fechaFin
     * @return Un arreglo de bytes que representa el archivo PDF generado.
     */
    public byte[] generarPDFIngresosPorMembresia(ReporteIngresosMensualesDTO dto, LocalDate fechaInicio, LocalDate fechaFin) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Ingresos por tipo de membresía")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Período: " +
                            fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " +
                            fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));

            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Tipo de Membresía").setBold()));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Total Ingresos").setBold()));

            for (MembresiaIngresoDTO item : dto.getDetalle()) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(item.getTipoMembresia())));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("$" + item.getTotal().toString())));
            }

            com.itextpdf.layout.element.Cell totalLabelCell = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("TOTAL GENERAL").setBold());
            com.itextpdf.layout.element.Cell totalValueCell = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("$" + dto.getTotalGeneral().toString()).setBold());
            table.addCell(totalLabelCell);
            table.addCell(totalValueCell);

            document.add(table);

            if (dto.getMensaje() != null) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph(dto.getMensaje())
                        .setFontColor(ColorConstants.RED));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Reporte generado por Pulse Gym")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF de ingresos por membresía: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    /**
     * Genera un archivo Excel para el reporte de ingresos agrupados por tipo de membresía en un rango de fechas.
     * @param dto
     * @param fechaInicio
     * @param fechaFin
     * @return Un arreglo de bytes que representa el archivo Excel generado.
     */
    public byte[] generarExcelIngresosPorMembresia(ReporteIngresosMensualesDTO dto, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Ingresos por membresía");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Ingresos por tipo de membresía");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

            Row periodRow = sheet.createRow(1);
            periodRow.createCell(0).setCellValue("Período: " +
                    fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " +
                    fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            int rowNum = 3;

            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Tipo de Membresía");
            headerRow.createCell(1).setCellValue("Total Ingresos");
            headerRow.getCell(0).setCellStyle(headerStyle);
            headerRow.getCell(1).setCellStyle(headerStyle);

            for (MembresiaIngresoDTO item : dto.getDetalle()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getTipoMembresia());
                row.createCell(1).setCellValue(item.getTotal().doubleValue());
            }

            Row totalRow = sheet.createRow(rowNum++);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTAL GENERAL");
            totalLabel.setCellStyle(boldStyle);
            Cell totalValue = totalRow.createCell(1);
            totalValue.setCellValue(dto.getTotalGeneral().doubleValue());
            totalValue.setCellStyle(boldStyle);

            if (dto.getMensaje() != null) {
                Row msgRow = sheet.createRow(rowNum++);
                Cell msgCell = msgRow.createCell(0);
                msgCell.setCellValue(dto.getMensaje());
                CellStyle redStyle = workbook.createCellStyle();
                Font redFont = workbook.createFont();
                redFont.setColor(IndexedColors.RED.getIndex());
                redStyle.setFont(redFont);
                msgCell.setCellStyle(redStyle);
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar Excel de ingresos por membresía: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el Excel", e);
        }
    }
}