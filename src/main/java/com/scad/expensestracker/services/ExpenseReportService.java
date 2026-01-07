package com.scad.expensestracker.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scad.expensestracker.models.ExpensesTrackerItem;
import com.scad.expensestracker.repositories.ExpensesTrackerRepository;

@Service
public class ExpenseReportService {
    
    @Autowired
    private ExpensesTrackerRepository expensesTrackerRepository;
    
    public Map<String, Object> getExpenseSummary() {
        List<ExpensesTrackerItem> expenses = (List<ExpensesTrackerItem>) expensesTrackerRepository.findAll();
        Map<String, Object> summary = new HashMap<>();
        
        // Total expenses
        double totalExpenses = expenses.stream()
                .mapToDouble(ExpensesTrackerItem::getPrice)
                .sum();
        
        // Average daily expenses
        double averageDailyExpenses = calculateAverageDailyExpense(expenses);
        
        // Most expensive category
        String mostExpensiveCategory = findMostExpensiveCategory(expenses);
        
        // Expenses by category
        Map<String, Double> expensesByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                    ExpensesTrackerItem::getCategory,
                    Collectors.summingDouble(ExpensesTrackerItem::getPrice)
                ));
        
        // Number of transactions
        long transactionCount = expenses.size();
        
        summary.put("totalExpenses", totalExpenses);
        summary.put("averageDailyExpenses", averageDailyExpenses);
        summary.put("mostExpensiveCategory", mostExpensiveCategory);
        summary.put("expensesByCategory", expensesByCategory);
        summary.put("transactionCount", transactionCount);
        
        return summary;
    }
    
    public Map<String, Object> getExpenseReportByCategory() {
        List<ExpensesTrackerItem> expenses = (List<ExpensesTrackerItem>) expensesTrackerRepository.findAll();
        
        Map<String, Object> report = expenses.stream()
            .collect(Collectors.groupingBy(
                ExpensesTrackerItem::getCategory,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> {
                        Map<String, Object> categoryReport = new HashMap<>();
                        double total = list.stream().mapToDouble(ExpensesTrackerItem::getPrice).sum();
                        double average = list.isEmpty() ? 0 : total / list.size();
                        
                        categoryReport.put("total", total);
                        categoryReport.put("average", average);
                        categoryReport.put("count", (long) list.size());
                        categoryReport.put("items", list);
                        
                        return categoryReport;
                    }
                )
            ));
        
        return report;
    }
    
    public Map<String, Object> getExpenseReportByDateRange(LocalDate startDate, LocalDate endDate) {
        List<ExpensesTrackerItem> expenses = (List<ExpensesTrackerItem>) expensesTrackerRepository.findAll();
        
        List<ExpensesTrackerItem> filteredExpenses = expenses.stream()
            .filter(item -> {
                if (item.getCreatedDate() == null) return false;
                LocalDate itemDate = item.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate();
                return !itemDate.isBefore(startDate) && !itemDate.isAfter(endDate);
            })
            .collect(Collectors.toList());
        
        Map<String, Object> report = new HashMap<>();
        double total = filteredExpenses.stream().mapToDouble(ExpensesTrackerItem::getPrice).sum();
        
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalExpenses", total);
        report.put("expenses", filteredExpenses);
        report.put("count", (long) filteredExpenses.size());
        
        return report;
    }
    
    private double calculateAverageDailyExpense(List<ExpensesTrackerItem> expenses) {
        if (expenses.isEmpty()) {
            return 0.0;
        }
        
        LocalDate startDate = expenses.stream()
            .filter(item -> item.getCreatedDate() != null)
            .map(item -> item.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDate())
            .min(LocalDate::compareTo)
            .orElse(LocalDate.now());

        long days = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        days = Math.max(days, 1);
        
        double totalExpenses = expenses.stream().mapToDouble(ExpensesTrackerItem::getPrice).sum();
        return totalExpenses / days;
    }
    
    private String findMostExpensiveCategory(List<ExpensesTrackerItem> expenses) {
        if (expenses.isEmpty()) {
            return null;
        }
        
        return expenses.stream()
            .collect(Collectors.groupingBy(
                ExpensesTrackerItem::getCategory,
                Collectors.summingDouble(ExpensesTrackerItem::getPrice)
            ))
            .entrySet()
            .stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
}