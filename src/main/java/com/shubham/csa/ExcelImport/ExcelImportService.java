package com.shubham.csa.ExcelImport;

import com.shubham.csa.entity.Ticket;
import com.shubham.csa.Repository.TicketRepository;
import com.shubham.csa.Repository.UserRepository;
import com.shubham.csa.entity.User;

import org.apache.commons.compress.archivers.dump.DumpArchiveEntry.TYPE;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ExcelImportService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ImportResult importTicketsFromExcel(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Read first sheet (Tickets)
            
            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                
                if (row == null) {
                    continue;
                }

                try {
                    Ticket ticket = parseRowToTicket(row, i);
                    
                    // Validate ticket
                    if (validateTicket(ticket, errors, i)) {
                        ticketRepository.save(ticket);
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } catch (Exception e) {
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                    failureCount++;
                }
            }
        }

        result.setSuccessCount(successCount);
        result.setFailureCount(failureCount);
        result.setErrors(errors);
        result.setTotalRows(successCount + failureCount);

        return result;
    }

    private Ticket parseRowToTicket(Row row, int rowNum) {
        Ticket ticket = new Ticket();

        try {
            // Column 0: Ticket ID (optional - skip if importing new tickets)
            String ticketId = getCellValueAsString(row.getCell(0));
            if (ticketId != null && !ticketId.isEmpty()) {
                ticket.setId(ticketId);
            }

            // Column 1: Title (required)
            String title = getCellValueAsString(row.getCell(1));
            ticket.setTitle(title);

            // Column 2: Description (required)
            String description = getCellValueAsString(row.getCell(2));
            ticket.setDescription(description);

            // Column 3: Status (required)
            String statusStr = getCellValueAsString(row.getCell(3));
            if (statusStr != null) {
                ticket.setStatus(Ticket.Status.valueOf(statusStr.toUpperCase().replace(" ", "_")));
            }

            // Column 4: Priority (required)
            String priorityStr = getCellValueAsString(row.getCell(4));
            if (priorityStr != null) {
                ticket.setPriority(Ticket.Priority.valueOf(priorityStr.toUpperCase()));
            }

            // Column 5: Category (optional)
            String typeStr = getCellValueAsString(row.getCell(5));
         if (typeStr != null && !typeStr.isEmpty()) {
            try {
        ticket.setType(Ticket.Type.valueOf(typeStr.toUpperCase().replace(" ", "_")));
    } catch (IllegalArgumentException e) {
        throw new RuntimeException("Invalid Type value: " + typeStr + 
            ". Valid values are: " + Arrays.toString(Ticket.Type.values()));
    }
}

            // Column 6: Customer (email or name)
            String customerIdentifier = getCellValueAsString(row.getCell(6));
            if (customerIdentifier != null) {
                User customer = findUserByEmailOrName(customerIdentifier);
                if (customer != null) {
                    ticket.setCustomerId(customer.getId());
                    ticket.setCustomerName(customer.getName());
                    ticket.setCustomerEmail(customer.getEmail());
                }
            }

            // Column 7: Assigned Agent (email or name) - optional
            String agentIdentifier = getCellValueAsString(row.getCell(7));
            if (agentIdentifier != null && !agentIdentifier.isEmpty() 
                && !agentIdentifier.equalsIgnoreCase("Unassigned")) {
                User agent = findUserByEmailOrName(agentIdentifier);
                if (agent != null && (agent.getRole() == User.Role.AGENT || agent.getRole() == User.Role.ADMIN)) {
                    ticket.setAssignedToId(agent.getId());
                    ticket.setAssignedToName(agent.getName());
                }
            }

            // Column 8: Created At (optional - use current time if not provided)
            String createdAtStr = getCellValueAsString(row.getCell(8));
            if (createdAtStr != null && !createdAtStr.isEmpty()) {
                ticket.setCreatedAt(LocalDateTime.parse(createdAtStr, DATE_FORMATTER));
            } else {
                ticket.setCreatedAt(LocalDateTime.now());
            }

            // Column 9: Updated At (optional - use current time if not provided)
            String updatedAtStr = getCellValueAsString(row.getCell(9));
            if (updatedAtStr != null && !updatedAtStr.isEmpty()) {
                ticket.setUpdatedAt(LocalDateTime.parse(updatedAtStr, DATE_FORMATTER));
            } else {
                ticket.setUpdatedAt(LocalDateTime.now());
            }

            // Column 10: Resolved At (optional)
            // String resolvedAtStr = getCellValueAsString(row.getCell(10));
            // if (resolvedAtStr != null && !resolvedAtStr.isEmpty()) {
            //     ticket.setResolvedAt(LocalDateTime.parse(resolvedAtStr, DATE_FORMATTER));
            // }

            // Generate ticket number if not exists
            if (ticket.getTicketNumber() == null || ticket.getTicketNumber().isEmpty()) {
                ticket.setTicketNumber(generateTicketNumber());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error parsing row: " + e.getMessage(), e);
        }

        return ticket;
    }

    private boolean validateTicket(Ticket ticket, List<String> errors, int rowNum) {
        boolean isValid = true;
        int rowNumber = rowNum + 1;

        if (ticket.getTitle() == null || ticket.getTitle().trim().isEmpty()) {
            errors.add("Row " + rowNumber + ": Title is required");
            isValid = false;
        }

        if (ticket.getDescription() == null || ticket.getDescription().trim().isEmpty()) {
            errors.add("Row " + rowNumber + ": Description is required");
            isValid = false;
        }

        if (ticket.getStatus() == null) {
            errors.add("Row " + rowNumber + ": Status is required");
            isValid = false;
        }

        if (ticket.getPriority() == null) {
            errors.add("Row " + rowNumber + ": Priority is required");
            isValid = false;
        }

        if (ticket.getCustomerId() == null || ticket.getCustomerId().isEmpty()) {
            errors.add("Row " + rowNumber + ": Valid customer is required");
            isValid = false;
        }

        return isValid;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DATE_FORMATTER);
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return null;
        }
    }
    

    private User findUserByEmailOrName(String identifier) {
        // Try to find by email first
        Optional<User> userByEmail = userRepository.findByEmail(identifier);
        if (userByEmail.isPresent()) {
            return userByEmail.get();
        }

        // Try to find by name
        List<User> users = userRepository.findAll();
        return users.stream()
                .filter(u -> u.getName().equalsIgnoreCase(identifier))
                .findFirst()
                .orElse(null);
    }

    private String generateTicketNumber() {
        return "TKT-" + System.currentTimeMillis();
    }

    // Inner class for import result
    public static class ImportResult {
        private int totalRows;
        private int successCount;
        private int failureCount;
        private List<String> errors;

        public ImportResult() {
            this.errors = new ArrayList<>();
        }

        // Getters and Setters
        public int getTotalRows() {
            return totalRows;
        }

        public void setTotalRows(int totalRows) {
            this.totalRows = totalRows;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(int successCount) {
            this.successCount = successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public void setFailureCount(int failureCount) {
            this.failureCount = failureCount;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }
    }
}