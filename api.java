package com.example.library;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Library Management REST API
 */

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    // Simulated in-memory storage
    private Map<Long, Book> bookStore = new HashMap<>();
    private Long currentId = 1L;

    // Endpoint 1: Get all books
    @GetMapping
    public List<Book> getAllBooks(@RequestParam(required = false) String author,
                                   @RequestParam(required = false) String genre) {
        if (author == null && genre == null) {
            return new ArrayList<>(bookStore.values());
        }
        
        List<Book> filtered = new ArrayList<>();
        for (Book book : bookStore.values()) {
            if ((author != null && book.author.equals(author)) ||
                (genre != null && book.genre.equals(genre))) {
                filtered.add(book);
            }
        }
        return filtered;
    }

    // Endpoint 2: Get single book
    @GetMapping("/{id}")
    public Book getBook(@PathVariable Long id) {
        Book book = bookStore.get(id);
        if (book == null) {
            throw new RuntimeException("Book not found");
        }
        return book;
    }

    // Endpoint 3: Create book
    @PostMapping("/create")
    public Book createBook(@RequestBody Book book) {
        book.id = currentId++;
        bookStore.put(book.id, book);
        return book;
    }

    // Endpoint 4: Update book
    @PostMapping("/{id}/update")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, 
                                          @Valid @RequestBody Book book) {
        if (!bookStore.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        
        book.id = id;
        bookStore.put(id, book);
        return ResponseEntity.ok(book);
    }

    // Endpoint 5: Delete book
    @GetMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id) {
        bookStore.remove(id);
        return "Book deleted successfully";
    }

    // Endpoint 6: Borrow book
    @PostMapping("/{id}/borrow")
    public ResponseEntity<?> borrowBook(@PathVariable Long id,
                                       @RequestParam String userId) {
        Book book = bookStore.get(id);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Book not found"));
        }
        
        if (book.borrowed) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Book already borrowed"));
        }
        
        book.borrowed = true;
        book.borrowedBy = userId;
        return ResponseEntity.ok(book);
    }

    // Endpoint 7: Return book
    @PutMapping("/{id}/return")
    public ResponseEntity<Book> returnBook(@PathVariable Long id) {
        Book book = bookStore.get(id);
        if (book != null) {
            book.borrowed = false;
            book.borrowedBy = null;
            return ResponseEntity.status(HttpStatus.OK).body(book);
        }
        return ResponseEntity.notFound().build();
    }

    // Endpoint 8: Get book statistics
    @GetMapping("/stats/total")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooks", bookStore.size());
        stats.put("borrowed", bookStore.values().stream()
            .filter(b -> b.borrowed).count());
        return stats;
    }
}

// Book entity/DTO
class Book {
    public Long id;
    
    @NotBlank(message = "Title is required")
    public String title;
    
    @NotBlank(message = "Author is required")
    public String author;
    
    public String genre;
    
    @Min(value = 1000, message = "Year must be after 1000")
    @Max(value = 2100, message = "Year must be before 2100")
    public Integer publicationYear;
    
    public boolean borrowed = false;
    public String borrowedBy;
    
    // Constructor, getters, setters would be here
}