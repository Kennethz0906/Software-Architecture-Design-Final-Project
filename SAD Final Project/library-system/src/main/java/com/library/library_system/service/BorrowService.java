package com.library.library_system.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.library_system.model.Book;
import com.library.library_system.model.BorrowRecord;
import com.library.library_system.model.User;
import com.library.library_system.repository.BookRepository;
import com.library.library_system.repository.BorrowRepository;
import com.library.library_system.repository.UserRepository;

@Service
@Transactional
public class BorrowService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BorrowRepository borrowRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final int DEFAULT_LOAN_DAYS = 14;
    private static final double FINE_PER_DAY = 1.0;
    
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Book not found"));
        
        // Check availability
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Book is currently unavailable");
        }
        
        // Create borrow record
        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(DEFAULT_LOAN_DAYS));
        record.setStatus("BORROWED");
        
        // Decrement available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        
        return borrowRepository.save(record);
    }
    
    public double returnBook(Long recordId) {
        BorrowRecord record = borrowRepository.findById(recordId)
            .orElseThrow(() -> new RuntimeException("Record not found"));
        
        LocalDate returnDate = LocalDate.now();
        record.setReturnDate(returnDate);
        
        double fine = 0.0;
        if (returnDate.isAfter(record.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(record.getDueDate(), returnDate);
            fine = daysOverdue * FINE_PER_DAY;
        }
        record.setFineAmount(fine);
        record.setStatus("RETURNED");
        
        // Increment available copies back
        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
        
        borrowRepository.save(record);
        return fine;
    }
    
    public BorrowRecord getBorrowRecord(Long id) {
        return borrowRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Record not found"));
    }
}