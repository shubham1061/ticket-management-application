package com.shubham.csa.Controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Sort;

import com.shubham.csa.ExcelExport.ExcelExportService;
import com.shubham.csa.ExcelImport.ExcelImportService;
import com.shubham.csa.Service.TicketService;
import com.shubham.csa.dto.CreateTicketDto;
import com.shubham.csa.dto.TicketDto;
import com.shubham.csa.dto.TicketStatsDto;
import com.shubham.csa.dto.UpdateTicketDto;
import com.shubham.csa.entity.Ticket;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
@Autowired
private TicketService ticketService;
@Autowired
private ExcelExportService excelExportService;

@Autowired
 private ExcelImportService excelImportService;
    // Create new ticket
    @PostMapping
    public ResponseEntity<TicketDto> createTicket(@Valid @RequestBody CreateTicketDto createDto) {
        try {
            TicketDto ticket = ticketService.createTicket(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/bulk")
public ResponseEntity<List<TicketDto>> createTickets(@Valid @RequestBody List<CreateTicketDto> createDtos) {
    List<TicketDto> createdTickets = new ArrayList<>();

    for (CreateTicketDto dto : createDtos) {
        TicketDto ticket = ticketService.createTicket(dto);
        createdTickets.add(ticket);
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(createdTickets);
}

    // Get ticket by ID
    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable String id) {
        try {
            TicketDto ticket = ticketService.getTicketById(id);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get ticket by ticket number
    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<TicketDto> getTicketByNumber(@PathVariable String ticketNumber) {
        try {
            TicketDto ticket = ticketService.getTicketByNumber(ticketNumber);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get all tickets with pagination and sorting
    @GetMapping
    public ResponseEntity<Page<TicketDto>> getAllTickets(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TicketDto> tickets = ticketService.getAllTickets(pageable);
        return ResponseEntity.ok(tickets);
    }

    // Get tickets by customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<TicketDto>> getTicketsByCustomer(
            @PathVariable String customerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TicketDto> tickets = ticketService.getTicketsByCustomer(customerId, pageable);
        return ResponseEntity.ok(tickets);
    }

    // Get tickets assigned to agent
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<Page<TicketDto>> getTicketsByAgent(
            @PathVariable String agentId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TicketDto> tickets = ticketService.getTicketsByAgent(agentId, pageable);
        return ResponseEntity.ok(tickets);
    }

    // Get tickets by status
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TicketDto>> getTicketsByStatus(
            @PathVariable Ticket.Status status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TicketDto> tickets = ticketService.getTicketsByStatus(status, pageable);
        return ResponseEntity.ok(tickets);
    }

    // Get unassigned tickets
    @GetMapping("/unassigned")
    public ResponseEntity<Page<TicketDto>> getUnassignedTickets(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TicketDto> tickets = ticketService.getUnassignedTickets(pageable);
        return ResponseEntity.ok(tickets);
    }

    // Search tickets with filters
    @GetMapping("/search")
    public ResponseEntity<Page<TicketDto>> searchTickets(
            @RequestParam(required = false) Ticket.Status status,
            @RequestParam(required = false) Ticket.Priority priority,
            @RequestParam(required = false) String assignedToId,
            @RequestParam(required = false) String customerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<TicketDto> tickets = ticketService.searchTickets(status, priority, assignedToId, customerId, pageable);
        return ResponseEntity.ok(tickets);
    }

    // Text search in tickets
    @GetMapping("/search/fulltext")
    public ResponseEntity<Page<TicketDto>> searchTicketsByText(
            @RequestParam String searchText,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<TicketDto> tickets = ticketService.searchTicketsByText(searchText, pageable);
        return ResponseEntity.ok(tickets);
    }

    // Update ticket
    @PutMapping("/{id}")
    public ResponseEntity<TicketDto> updateTicket(
            @PathVariable String id, 
            @Valid @RequestBody UpdateTicketDto updateDto) {
        try {
            TicketDto ticket = ticketService.updateTicket(id, updateDto);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



    // Assign ticket to agent
    @PostMapping("/{id}/assign")
    public ResponseEntity<TicketDto> assignTicket(
            @PathVariable String id,
            @RequestParam(required = false) String agentId) {
        try {
            TicketDto ticket = ticketService.assignTicket(id, agentId);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Change ticket status
    @PostMapping("/{id}/status")
    public ResponseEntity<TicketDto> changeTicketStatus(
            @PathVariable String id,
            @RequestParam Ticket.Status status) {
        try {
            TicketDto ticket = ticketService.changeTicketStatus(id, status);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get ticket statistics
    @GetMapping("/stats")
    public ResponseEntity<TicketStatsDto> getTicketStatistics() {
        TicketStatsDto stats = ticketService.getTicketStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/full")
    public ResponseEntity<TicketDto> getTicketWithMessages(@PathVariable String id) {
        try {
            TicketDto ticket = ticketService.getTicketWithMessages(id);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


      @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportTicketsToExcel() throws IOException {
        byte[] excelFile = excelExportService.exportTicketsToExcel();
        
        String filename = "tickets_" + 
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                         ".xlsx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelFile.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelFile);
    }

@GetMapping("/export/excel/filtered")
public ResponseEntity<byte[]> exportFilteredTickets(
        @RequestParam(required = false) Ticket.Status status,
        @RequestParam(required = false) Ticket.Priority priority,
        @RequestParam(required = false) String assignedToId,
        @RequestParam(required = false) String customerId) throws IOException {
    
    // Remove pageable parameter - not needed for export
    byte[] excelFile = excelExportService.exportFilteredTickets(
        status, priority, assignedToId, customerId
    );
    
    String filename = "filtered_tickets_" + 
                     LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                     ".xlsx";
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", filename);
    headers.setContentLength(excelFile.length);
    
    return ResponseEntity.ok()
            .headers(headers)
            .body(excelFile);
}


 // Import tickets from Excel
    @PostMapping("/import/excel")
    public ResponseEntity<Map<String, Object>> importTicketsFromExcel(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate file type
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                response.put("success", false);
                response.put("message", "Only Excel files (.xlsx, .xls) are allowed");
                return ResponseEntity.badRequest().body(response);
            }

            // Import tickets
            ExcelImportService.ImportResult result = excelImportService.importTicketsFromExcel(file);

            response.put("success", true);
            response.put("message", "Import completed");
            response.put("totalRows", result.getTotalRows());
            response.put("successCount", result.getSuccessCount());
            response.put("failureCount", result.getFailureCount());
            response.put("errors", result.getErrors());

            if (result.getFailureCount() > 0) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Error reading file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


     @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadImportTemplate() throws IOException {
        byte[] template = excelExportService.generateImportTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "ticket_import_template.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(template);
    }
}
