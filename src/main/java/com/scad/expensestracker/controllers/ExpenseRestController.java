package com.scad.expensestracker.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scad.expensestracker.models.ExpensesTrackerItem;
import com.scad.expensestracker.repositories.ExpensesTrackerRepository;
import com.scad.expensestracker.services.ExpenseReportService;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseRestController {
    
    @Autowired
    private ExpensesTrackerRepository expensesTrackerRepository;
    
    @Autowired
    private ExpenseReportService expenseReportService;
    
    // Get all expenses
    @GetMapping
    public ResponseEntity<List<ExpensesTrackerItem>> getAllExpenses() {
        List<ExpensesTrackerItem> expenses = (List<ExpensesTrackerItem>) expensesTrackerRepository.findAll();
        return new ResponseEntity<>(expenses, HttpStatus.OK);
    }
    
    // Get expense by ID
    @GetMapping("/{id}")
    public ResponseEntity<ExpensesTrackerItem> getExpenseById(@PathVariable("id") Long id) {
        Optional<ExpensesTrackerItem> expense = expensesTrackerRepository.findById(id);
        if (expense.isPresent()) {
            return new ResponseEntity<>(expense.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Create new expense
    @PostMapping
    public ResponseEntity<ExpensesTrackerItem> createExpense(@Valid @RequestBody ExpensesTrackerItem expense) {
        expense.setCreatedDate(java.time.Instant.now());
        expense.setModifiedDate(java.time.Instant.now());
        ExpensesTrackerItem savedExpense = expensesTrackerRepository.save(expense);
        return new ResponseEntity<>(savedExpense, HttpStatus.CREATED);
    }
    
    // Update expense
    @PutMapping("/{id}")
    public ResponseEntity<ExpensesTrackerItem> updateExpense(@PathVariable("id") Long id, 
            @Valid @RequestBody ExpensesTrackerItem expenseDetails) {
        Optional<ExpensesTrackerItem> optionalExpense = expensesTrackerRepository.findById(id);
        
        if (optionalExpense.isPresent()) {
            ExpensesTrackerItem expense = optionalExpense.get();
            expense.setDescription(expenseDetails.getDescription());
            expense.setPrice(expenseDetails.getPrice());
            expense.setCategory(expenseDetails.getCategory());
            expense.setModifiedDate(java.time.Instant.now());
            
            ExpensesTrackerItem updatedExpense = expensesTrackerRepository.save(expense);
            return new ResponseEntity<>(updatedExpense, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Delete expense
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteExpense(@PathVariable("id") Long id) {
        Optional<ExpensesTrackerItem> expense = expensesTrackerRepository.findById(id);
        
        if (expense.isPresent()) {
            expensesTrackerRepository.delete(expense.get());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Get expense summary report
    @GetMapping("/reports/summary")
    public ResponseEntity<Map<String, Object>> getExpenseSummary() {
        Map<String, Object> summary = expenseReportService.getExpenseSummary();
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }
    
    // Get expense report by category
    @GetMapping("/reports/category")
    public ResponseEntity<Map<String, Object>> getExpenseReportByCategory() {
        Map<String, Object> report = expenseReportService.getExpenseReportByCategory();
        return new ResponseEntity<>(report, HttpStatus.OK);
    }
    
    // Get expense report by date range
    @GetMapping("/reports/date-range")
    public ResponseEntity<Map<String, Object>> getExpenseReportByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1); // Default to last month
        }
        if (endDate == null) {
            endDate = LocalDate.now(); // Default to today
        }
        
        Map<String, Object> report = expenseReportService.getExpenseReportByDateRange(startDate, endDate);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }
}