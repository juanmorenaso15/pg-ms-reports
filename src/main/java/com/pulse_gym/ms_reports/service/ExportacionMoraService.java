package com.pulse_gym.ms_reports.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.pulse_gym.lb_common.dto.ReporteMoraResponseDTO;
import com.pulse_gym.lb_common.dto.SocioMoraDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportacionMoraService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generarPDFMora(ReporteMoraResponseDTO dto, LocalDate fechaInicio, LocalDate fechaFin) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Reporte de Socios en Mora")
                    .setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER));

            String periodo = (fechaInicio != null && fechaFin != null) ?
                    "Período: " + fechaInicio.format(DATE_FORMATTER) + " al " + fechaFin.format(DATE_FORMATTER) :
                    "Sin filtro de fechas";
            document.add(new Paragraph(periodo).setFontSize(12).setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(" "));

            if (dto.getSociosEnMora().isEmpty()) {
                document.add(new Paragraph("No hay socios en mora").setFontColor(ColorConstants.RED));
            } else {
                Table table = new Table(UnitValue.createPercentArray(new float[]{15, 20, 15, 20, 15, 15}))
                        .setWidth(UnitValue.createPercentValue(100));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("ID").setBold()));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Nombre").setBold()));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Identificación").setBold()));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Teléfono").setBold()));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Email").setBold()));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Membresía").setBold()));

                for (SocioMoraDTO socio : dto.getSociosEnMora()) {
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(socio.getIdSocio().toString())));
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(socio.getNombreCompleto())));
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(socio.getIdentificacion() != null ? socio.getIdentificacion() : "-")));
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(socio.getTelefono() != null ? socio.getTelefono() : "-")));
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(socio.getEmail())));
                    table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(socio.getTipoMembresia() + " (" + socio.getEstadoMembresia() + ")")));
                }
                document.add(table);
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generando PDF de mora", e);
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    public byte[] generarExcelMora(ReporteMoraResponseDTO dto, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Socios en mora");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Nombre", "Identificación", "Teléfono", "Email", "Membresía", "Estado", "Vencimiento"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (SocioMoraDTO socio : dto.getSociosEnMora()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(socio.getIdSocio());
                row.createCell(1).setCellValue(socio.getNombreCompleto());
                row.createCell(2).setCellValue(socio.getIdentificacion() != null ? socio.getIdentificacion() : "-");
                row.createCell(3).setCellValue(socio.getTelefono() != null ? socio.getTelefono() : "-");
                row.createCell(4).setCellValue(socio.getEmail());
                row.createCell(5).setCellValue(socio.getTipoMembresia());
                row.createCell(6).setCellValue(socio.getEstadoMembresia());
                row.createCell(7).setCellValue(socio.getFechaVencimiento());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generando Excel de mora", e);
            throw new RuntimeException("Error al generar Excel", e);
        }
    }
}