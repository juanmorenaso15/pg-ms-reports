package com.pulse_gym.ms_reports.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.pulse_gym.lb_common.dto.DatoPeriodoDTO;
import com.pulse_gym.lb_common.dto.TendenciaPeriodoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportacionTendenciaService {

    /**
     * Genera un PDF del reporte de tendencia.
     */
    private static final DecimalFormat DF = new DecimalFormat("#.##");

    /**
     * Genera un PDF del reporte de tendencia.
     * @param dto DTO con los datos de la tendencia
     * @return byte[] con el archivo PDF
     */
    public byte[] generarPDFTendencia(TendenciaPeriodoDTO dto) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Reporte de Tendencia de Afluencia")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Tipo: " + dto.getTipoReporte())
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Período: " + dto.getPeriodo())
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(" "));

            if (dto.getVariacionPorcentual() != null) {
                String variacionTexto = String.format("Variación: %s%%", 
                        DF.format(dto.getVariacionPorcentual()));
                document.add(new Paragraph(variacionTexto)
                        .setFontSize(14)
                        .setBold());
                document.add(new Paragraph(" "));
            }

            if (dto.getMensaje() != null && !dto.getMensaje().isEmpty()) {
                document.add(new Paragraph(dto.getMensaje())
                        .setFontSize(12)
                        .setFontColor(ColorConstants.RED));
                document.add(new Paragraph(" "));
            }

            List<DatoPeriodoDTO> actual = dto.getDatosPeriodoActual();
            List<DatoPeriodoDTO> anterior = dto.getDatosPeriodoAnterior();

            if (!actual.isEmpty() || !anterior.isEmpty()) {
                Table table = new Table(UnitValue.createPercentArray(new float[]{30, 35, 35}))
                        .setWidth(UnitValue.createPercentValue(100));

                table.addCell(new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph("Fecha").setBold()));
                table.addCell(new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph("Período Actual").setBold()));
                table.addCell(new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph("Período Anterior").setBold()));

                int maxSize = Math.max(actual.size(), anterior.size());
                for (int i = 0; i < maxSize; i++) {
                    DatoPeriodoDTO datoActual = i < actual.size() ? actual.get(i) : null;
                    DatoPeriodoDTO datoAnterior = i < anterior.size() ? anterior.get(i) : null;

                    String fecha = datoActual != null ? datoActual.getEtiqueta() 
                            : (datoAnterior != null ? datoAnterior.getEtiqueta() : "");

                    String valActual = datoActual != null ? String.valueOf(datoActual.getTotalSocios()) : "-";
                    String valAnterior = datoAnterior != null ? String.valueOf(datoAnterior.getTotalSocios()) : "-";

                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(fecha)));
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(valActual)));
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(valAnterior)));
                }

                document.add(table);
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Reporte generado por Pulse Gym")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF de tendencia: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    /**
     * Genera un Excel del reporte de tendencia.
     * @param dto DTO con los datos de la tendencia
     * @return byte[] con el archivo Excel
     */
    public byte[] generarExcelTendencia(TendenciaPeriodoDTO dto) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Tendencia");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reporte de Tendencia de Afluencia");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 2));

            Row infoRow1 = sheet.createRow(1);
            infoRow1.createCell(0).setCellValue("Tipo: " + dto.getTipoReporte());
            Row infoRow2 = sheet.createRow(2);
            infoRow2.createCell(0).setCellValue("Período: " + dto.getPeriodo());

            int rowNum = 3;
            if (dto.getVariacionPorcentual() != null) {
                Row varRow = sheet.createRow(rowNum++);
                varRow.createCell(0).setCellValue("Variación: " + DF.format(dto.getVariacionPorcentual()) + "%");
            }

            if (dto.getMensaje() != null && !dto.getMensaje().isEmpty()) {
                Row msgRow = sheet.createRow(rowNum++);
                Cell msgCell = msgRow.createCell(0);
                msgCell.setCellValue(dto.getMensaje());
                org.apache.poi.ss.usermodel.CellStyle redStyle = workbook.createCellStyle();
                Font redFont = workbook.createFont();
                redFont.setColor(IndexedColors.RED.getIndex());
                redStyle.setFont(redFont);
                msgCell.setCellStyle(redStyle);
            }

            rowNum++; 

            List<DatoPeriodoDTO> actual = dto.getDatosPeriodoActual();
            List<DatoPeriodoDTO> anterior = dto.getDatosPeriodoAnterior();

            if (!actual.isEmpty() || !anterior.isEmpty()) {
                Row headerRow = sheet.createRow(rowNum++);
                headerRow.createCell(0).setCellValue("Fecha");
                headerRow.createCell(1).setCellValue("Período Actual");
                headerRow.createCell(2).setCellValue("Período Anterior");
                for (int i = 0; i < 3; i++) {
                    headerRow.getCell(i).setCellStyle(headerStyle);
                }

                int maxSize = Math.max(actual.size(), anterior.size());
                for (int i = 0; i < maxSize; i++) {
                    Row row = sheet.createRow(rowNum++);
                    DatoPeriodoDTO datoActual = i < actual.size() ? actual.get(i) : null;
                    DatoPeriodoDTO datoAnterior = i < anterior.size() ? anterior.get(i) : null;

                    String fecha = datoActual != null ? datoActual.getEtiqueta() 
                            : (datoAnterior != null ? datoAnterior.getEtiqueta() : "");
                    String valActual = datoActual != null ? String.valueOf(datoActual.getTotalSocios()) : "-";
                    String valAnterior = datoAnterior != null ? String.valueOf(datoAnterior.getTotalSocios()) : "-";

                    row.createCell(0).setCellValue(fecha);
                    row.createCell(1).setCellValue(valActual);
                    row.createCell(2).setCellValue(valAnterior);
                }

                for (int i = 0; i < 3; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar Excel de tendencia: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el Excel", e);
        }
    }
}