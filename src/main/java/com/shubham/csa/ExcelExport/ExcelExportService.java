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
            
            // Create instruction row
            Row instructionRow = sheet.createRow(0);
            Cell instructionCell = instructionRow.createCell(0);
            instructionCell.setCellValue("INSTRUCTIONS: Fill in the columns below. Required fields are marked with *. Do NOT modify this header row.");
            
            CellStyle instructionStyle = workbook.createCellStyle();
            Font instructionFont = workbook.createFont();
            instructionFont.setBold(true);
            instructionFont.setColor(IndexedColors.DARK_RED.getIndex());
            instructionFont.setFontHeightInPoints((short) 12);
            instructionStyle.setFont(instructionFont);
            instructionCell.setCellStyle(instructionStyle);
            
            // Merge instruction cells
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 11));
            
            // Create header row
            Row headerRow = sheet.createRow(1);
            String[] headers = {
                "Ticket ID (Optional)", 
                "Title *", 
                "Description *", 
                "Status *", 
                "Priority *",
                "Type *",
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
            sampleRow.createCell(1).setCellValue("Login issue - cannot access dashboard");
            sampleRow.createCell(2).setCellValue("User is unable to login to the dashboard after password reset");
            sampleRow.createCell(3).setCellValue("OPEN");
            sampleRow.createCell(4).setCellValue("HIGH");
            sampleRow.createCell(5).setCellValue("ISSUE"); // Adjust based on your Type enum
            sampleRow.createCell(6).setCellValue("Technical Support");
            sampleRow.createCell(7).setCellValue("customer@example.com");
            sampleRow.createCell(8).setCellValue("agent@example.com");
            sampleRow.createCell(9).setCellValue("2024-11-17 10:00:00");
            sampleRow.createCell(10).setCellValue("2024-11-17 10:00:00");
            sampleRow.createCell(11).setCellValue("");
            
            // Add another sample for variety
            Row sample2Row = sheet.createRow(3);
            sample2Row.createCell(0).setCellValue("");
            sample2Row.createCell(1).setCellValue("Feature request - Dark mode");
            sample2Row.createCell(2).setCellValue("Request to add dark mode to the application");
            sample2Row.createCell(3).setCellValue("OPEN");
            sample2Row.createCell(4).setCellValue("LOW");
            sample2Row.createCell(5).setCellValue("REQUEST");
            sample2Row.createCell(6).setCellValue("Feature Request");
            sample2Row.createCell(7).setCellValue("user@example.com");
            sample2Row.createCell(8).setCellValue("");
            sample2Row.createCell(9).setCellValue("");
            sample2Row.createCell(10).setCellValue("");
            sample2Row.createCell(11).setCellValue("");
            
            // Add empty row
            sheet.createRow(4);
            
            // Add validation info section
            CellStyle infoStyle = workbook.createCellStyle();
            Font infoFont = workbook.createFont();
            infoFont.setBold(true);
            infoFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            infoStyle.setFont(infoFont);
            
            Row validationHeaderRow = sheet.createRow(5);
            Cell validationHeaderCell = validationHeaderRow.createCell(0);
            validationHeaderCell.setCellValue("VALID VALUES REFERENCE:");
            validationHeaderCell.setCellStyle(infoStyle);
            
            Row statusRow = sheet.createRow(6);
            statusRow.createCell(0).setCellValue("Status:");
            statusRow.createCell(1).setCellValue("OPEN, IN_PROGRESS, RESOLVED, CLOSED");
            
            Row priorityRow = sheet.createRow(7);
            priorityRow.createCell(0).setCellValue("Priority:");
            priorityRow.createCell(1).setCellValue("LOW, MEDIUM, HIGH, URGENT");
            
            Row typeRow = sheet.createRow(8);
            typeRow.createCell(0).setCellValue("Type:");
            typeRow.createCell(1).setCellValue("ISSUE, REQUEST, QUESTION, COMPLAINT");
            
            Row notesRow = sheet.createRow(10);
            notesRow.createCell(0).setCellValue("NOTES:");
            notesRow.getCell(0).setCellStyle(infoStyle);
            
            Row note1Row = sheet.createRow(11);
            note1Row.createCell(0).setCellValue("• Leave Ticket ID empty to create new tickets");
            
            Row note2Row = sheet.createRow(12);
            note2Row.createCell(0).setCellValue("• Customer Email must match an existing user in the system");
            
            Row note3Row = sheet.createRow(13);
            note3Row.createCell(0).setCellValue("• Assigned Agent Email is optional, leave empty for unassigned tickets");
            
            Row note4Row = sheet.createRow(14);
            note4Row.createCell(0).setCellValue("• Date format must be: yyyy-MM-dd HH:mm:ss (e.g., 2024-11-17 14:30:00)");
            
            Row note5Row = sheet.createRow(15);
            note5Row.createCell(0).setCellValue("• Leave date fields empty to use current date/time");
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Add some extra width for readability
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            // Set specific widths for description and title
            sheet.setColumnWidth(1, 8000); // Title
            sheet.setColumnWidth(2, 12000); // Description
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }


}
