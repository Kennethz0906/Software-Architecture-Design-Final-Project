package com.library.library_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.library.library_system.model.BorrowRecord;
import com.library.library_system.model.User;
import com.library.library_system.repository.BorrowRepository;
import com.library.library_system.service.BorrowService;

@Controller
@RequestMapping("/borrow")
public class BorrowController {
    
    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private BorrowRepository borrowRepository;
    
    @GetMapping("/{bookId}")
    public String borrowBook(@PathVariable Long bookId, Authentication auth, Model model) {
        try {
            User user = (User) auth.getPrincipal();
            BorrowRecord record = borrowService.borrowBook(user.getId(), bookId);
            model.addAttribute("message", "Book borrowed successfully! Due date: " + record.getDueDate());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/books/" + bookId;
    }
    
    @GetMapping("/return/{recordId}")
    public String returnBook(@PathVariable Long recordId, Model model) {
        try {
            double fine = borrowService.returnBook(recordId);
            if (fine > 0) {
                model.addAttribute("message", "Book returned. Fine: $" + fine);
            } else {
                model.addAttribute("message", "Book returned successfully!");
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/my-books";
    }
    
    @GetMapping("/history")
    public String myHistory(Authentication auth, Model model) {
        User user = (User) auth.getPrincipal();
        model.addAttribute("records", borrowRepository.findByUserId(user.getId()));
        return "my-books";
    }
}