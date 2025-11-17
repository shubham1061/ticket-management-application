package com.shubham.csa.ExcelExport;


import com.shubham.csa.Repository.TicketRepository;
import com.shubham.csa.entity.Ticket;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    @Autowired
    private TicketRepository ticketRepository;

    public byte[] exportTicketsToExcel() throws IOException {
        List<Ticket> tickets = ticketRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create main tickets sheet
            createTicketsSheet(workbook, tickets);
            
            // Create summary/aggregation sheet
            createSummarySheet(workbook, tickets);
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createTicketsSheet(Workbook workbook, List<Ticket> tickets) {
        Sheet sheet = workbook.createSheet("Tickets");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Ticket ID", "Title", "Description","Category","Status", 
                           "Priority", "Customer", "Assigned Agent", 
                           "Created At", "Updated At"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Fill data rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        
        for (Ticket ticket : tickets) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(ticket.getTicketNumber());
            row.createCell(1).setCellValue(ticket.getTitle());
            row.createCell(2).setCellValue(ticket.getDescription());
            row.createCell(3).setCellValue(ticket.getType().toString());
            row.createCell(4).setCellValue(ticket.getStatus().toString());
            row.createCell(5).setCellValue(ticket.getPriority().toString());
            row.createCell(6).setCellValue(ticket.getCustomerName());
            row.createCell(7).setCellValue(ticket.getAssignedToName() != null ? 
                                          ticket.getAssignedToName() : "Unassigned");
            
            Cell createdCell = row.createCell(8);
            createdCell.setCellValue(ticket.getCreatedAt().format(formatter));
            createdCell.setCellStyle(dateStyle);
            
            Cell updatedCell = row.createCell(9);
            updatedCell.setCellValue(ticket.getUpdatedAt().format(formatter));
            updatedCell.setCellStyle(dateStyle);
            
            // if (ticket.getResolvedAt() != null) {
            //     Cell resolvedCell = row.createCell(10);
            //     resolvedCell.setCellValue(ticket.getResolvedAt().format(formatter));
            //     resolvedCell.setCellStyle(dateStyle);
            // }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSummarySheet(Workbook workbook, List<Ticket> tickets) {
        Sheet sheet = workbook.createSheet("Summary");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        
        int rowNum = 0;
        
        // Total tickets
        Row totalRow = sheet.createRow(rowNum++);
        Cell totalLabelCell = totalRow.createCell(0);
        totalLabelCell.setCellValue("Total Tickets");
        totalLabelCell.setCellStyle(titleStyle);
        totalRow.createCell(1).setCellValue(tickets.size());
        rowNum++; // Empty row
        
        // By Status
        Row statusHeaderRow = sheet.createRow(rowNum++);
        Cell statusHeaderCell = statusHeaderRow.createCell(0);
        statusHeaderCell.setCellValue("By Status");
        statusHeaderCell.setCellStyle(titleStyle);
        
        Map<String, Long> statusCounts = tickets.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                t -> t.getStatus().toString(), 
                java.util.stream.Collectors.counting()
            ));
        
        Row statusLabelRow = sheet.createRow(rowNum++);
        statusLabelRow.createCell(0).setCellValue("Status");
        statusLabelRow.createCell(1).setCellValue("Count");
        statusLabelRow.getCell(0).setCellStyle(headerStyle);
        statusLabelRow.getCell(1).setCellStyle(headerStyle);
        
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
        rowNum++; // Empty row
        
        // By Priority
        Row priorityHeaderRow = sheet.createRow(rowNum++);
        Cell priorityHeaderCell = priorityHeaderRow.createCell(0);
        priorityHeaderCell.setCellValue("By Priority");
        priorityHeaderCell.setCellStyle(titleStyle);
        
        Map<String, Long> priorityCounts = tickets.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                t -> t.getPriority().toString(), 
                java.util.stream.Collectors.counting()
            ));
        
        Row priorityLabelRow = sheet.createRow(rowNum++);
        priorityLabelRow.createCell(0).setCellValue("Priority");
        priorityLabelRow.createCell(1).setCellValue("Count");
        priorityLabelRow.getCell(0).setCellStyle(headerStyle);
        priorityLabelRow.getCell(1).setCellStyle(headerStyle);
        
        for (Map.Entry<String, Long> entry : priorityCounts.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }
        rowNum++; // Empty row
        
        // // By Category
        // Row categoryHeaderRow = sheet.createRow(rowNum++);
        // Cell categoryHeaderCell = categoryHeaderRow.createCell(0);
        // categoryHeaderCell.setCellValue("By Category");
        // categoryHeaderCell.setCellStyle(titleStyle);
        
        // Map<String, Long> categoryCounts = tickets.stream()
        //     .collect(java.util.stream.Collectors.groupingBy(
        //         Ticket::getCategory, 
        //         java.util.stream.Collectors.counting()
        //     ));
        
        // Row categoryLabelRow = sheet.createRow(rowNum++);
        // categoryLabelRow.createCell(0).setCellValue("Category");
        // categoryLabelRow.createCell(1).setCellValue("Count");
        // categoryLabelRow.getCell(0).setCellStyle(headerStyle);
        // categoryLabelRow.getCell(1).setCellStyle(headerStyle);
        
        // for (Map.Entry<String, Long> entry : categoryCounts.entrySet()) {
        //     Row row = sheet.createRow(rowNum++);
        //     row.createCell(0).setCellValue(entry.getKey());
        //     row.createCell(1).setCellValue(entry.getValue());
        // }
        
        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
     
     
        return style;
    }



    public byte[] exportFilteredTickets(Ticket.Status status, Ticket.Priority priority, String assignedToId, String customerId) throws IOException {
    List<Ticket> tickets;
    
    if (status != null || priority != null || assignedToId != null|| customerId!=null) {
        // Apply filters (you'll need to create this query method)
        tickets = (List<Ticket>) ticketRepository.findTicketsWithFiltersForExport(status, priority, assignedToId,customerId);
    } else {
        tickets = ticketRepository.findAll();
    }
    
    try (Workbook workbook = new XSSFWorkbook()) {
        createTicketsSheet(workbook, tickets);
        createSummarySheet(workbook, tickets);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}

public byte[] generateImportTemplate() throws IOException {
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Tickets Template");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        // Create header row with instructions
        Row instructionRow = sheet.createRow(0);
        Cell instructionCell = instructionRow.createCell(0);
        instructionCell.setCellValue("Instructions: Fill in the columns below. Required fields are marked with *");
        
        CellStyle instructionStyle = workbook.createCellStyle();
        Font instructionFont = workbook.createFont();
        instructionFont.setBold(true);
        instructionFont.setColor(IndexedColors.DARK_RED.getIndex());
        instructionStyle.setFont(instructionFont);
        instructionCell.setCellStyle(instructionStyle);
        
        // Merge instruction cells
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 10));
        
        // Create header row
        Row headerRow = sheet.createRow(1);
        String[] headers = {
            "Ticket ID (Optional)", 
            "Title *", 
            "Description *", 
            "Status *", 
            "Priority *", 
            "Category", 
            "Customer Email *", 
            "Assigned Agent Email", 
            "Created At (yyyy-MM-dd HH:mm:ss)", 
            "Updated At (yyyy-MM-dd HH:mm:ss)", 
            "Resolved At (yyyy-MM-dd HH:mm:ss)"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add sample data row
        Row sampleRow = sheet.createRow(2);
        sampleRow.createCell(0).setCellValue(""); // Leave ID empty for new tickets
        sampleRow.createCell(1).setCellValue("Sample Ticket Title");
        sampleRow.createCell(2).setCellValue("Sample ticket description");
        sampleRow.createCell(3).setCellValue("OPEN");
        sampleRow.createCell(4).setCellValue("MEDIUM");
        sampleRow.createCell(5).setCellValue("Technical");
        sampleRow.createCell(6).setCellValue("customer@example.com");
        sampleRow.createCell(7).setCellValue("agent@example.com");
        sampleRow.createCell(8).setCellValue("2024-11-14 10:00:00");
        sampleRow.createCell(9).setCellValue("2024-11-14 10:00:00");
        sampleRow.createCell(10).setCellValue("");
        
        // Add validation info
        Row validationRow = sheet.createRow(4);
        validationRow.createCell(0).setCellValue("Valid Status values:");
        validationRow.createCell(1).setCellValue("OPEN, IN_PROGRESS, RESOLVED, CLOSED");
        
        Row priorityRow = sheet.createRow(5);
        priorityRow.createCell(0).setCellValue("Valid Priority values:");
        priorityRow.createCell(1).setCellValue("LOW, MEDIUM, HIGH, URGENT");
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}
}
