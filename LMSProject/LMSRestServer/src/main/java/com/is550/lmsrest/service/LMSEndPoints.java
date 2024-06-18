package com.is550.lmsrest.service;

import com.is550.lmsrest.database.LMSDatabase;
import com.is550.lmsrest.exceptions.InvalidParametersException;
import com.is550.lmsrest.variables.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.CollectionModel;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@RestController
public class LMSEndPoints {
    private final LMSDatabase lmsDatabase;

    @Autowired
    public LMSEndPoints(LMSDatabase lmsDatabase) {
        this.lmsDatabase = lmsDatabase;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<UserLoginInfos>> login(@RequestBody LoginRequest login) {
        UserLoginInfos userInfo = lmsDatabase.findUserId(login.getEmail(), login.getPassword());
        EntityModel<UserLoginInfos> resource = EntityModel.of(userInfo);
        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).login(login)).withSelfRel());

        if (UserType.USER.equals(userInfo.getUserType())) {
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).getBookById(null, userInfo.getUserId())).withRel("get-book"));
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).searchBooks(null, null, userInfo.getUserId())).withRel("search-books"));
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).getUserBorrowingInfos(lmsDatabase.translateUserIdToStudentId(userInfo.getUserId()), userInfo.getUserId())).withRel("borrowing-info"));
        } else if (UserType.LIBRARIAN.equals(userInfo.getUserType())) {
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).getBookById(null, userInfo.getUserId())).withRel("get-book"));
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).searchBooks(null, null, userInfo.getUserId())).withRel("search-books"));
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).getUserBorrowingInfos(null, userInfo.getUserId())).withRel("borrowing-info"));
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).addBorrowBook(null, userInfo.getUserId())).withRel("borrow-book"));
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).addReturnBook(null, userInfo.getUserId())).withRel("return-book"));
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).addBook(null, userInfo.getUserId())).withRel("add-book"));
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).deleteBook(null, userInfo.getUserId())).withRel("delete-book"));
        }

        return ResponseEntity.ok(resource);
    }

    @GetMapping(value = "/user/info/{studentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionModel<EntityModel<UserBorrowingInfos>>> getUserBorrowingInfos(@PathVariable Long studentId, @RequestHeader("Login-ID") Long loginId) {
        List<UserBorrowingInfos> borrowingInfos = lmsDatabase.findUserInfo(loginId, studentId);
        List<EntityModel<UserBorrowingInfos>> resourceList = new ArrayList<>();
        for (UserBorrowingInfos borrowingInfo : borrowingInfos) {
            EntityModel<UserBorrowingInfos> resource = EntityModel.of(borrowingInfo);
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).getBookById(borrowingInfo.getBook().getId(), loginId)).withRel("get-book"));
            if (lmsDatabase.checkLoginIdIsLibrarian(loginId)) {
                resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).addBorrowBook(null, loginId)).withRel("borrow-book"));
                resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).addReturnBook(null, loginId)).withRel("return-book"));
                resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).deleteBook(borrowingInfo.getBook().getId(), loginId)).withRel("delete-book"));
            }

            resourceList.add(resource);
        }

        CollectionModel<EntityModel<UserBorrowingInfos>> resources = CollectionModel.of(resourceList);

        return ResponseEntity.ok(resources);
    }

    @GetMapping(value = "/book/{bookId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<Book>> getBookById(@PathVariable Long bookId, @RequestHeader("Login-ID") Long loginId) {
        Book book = lmsDatabase.findBookById(loginId, bookId);
        EntityModel<Book> resource = EntityModel.of(book);
        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).getBookById(bookId, loginId)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).searchBooks(book.getName(), book.getAuthor(), loginId)).withRel("search-books"));
        if (lmsDatabase.checkLoginIdIsLibrarian(loginId)) {
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).deleteBook(book.getId(), loginId)).withRel("delete-book"));
        }
        return ResponseEntity.ok(resource);
    }

    @GetMapping(value = "/book/{name}/{author}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionModel<EntityModel<Book>>> searchBooks(
            @PathVariable(required = false) String name,
            @PathVariable(required = false) String author,
            @RequestHeader("Login-ID") Long loginId) {
        try {
            name = URLDecoder.decode(name, StandardCharsets.UTF_8.name());
            author = URLDecoder.decode(author, StandardCharsets.UTF_8.name());
        }catch (UnsupportedEncodingException e) {
            ;
        }

        List<Book> books = null;
        if (!Objects.equals(name, "null") && Objects.equals(author, "null")) {
            books = lmsDatabase.findBooksByName(loginId, name);
        } else if (!Objects.equals(author, "null") && Objects.equals(name, "null")) {
            books = lmsDatabase.findBooksByAuthor(loginId, author);
        } else if (!Objects.equals(name, "null") && !Objects.equals(author, "null")) {
            List<Book> name_books = lmsDatabase.findBooksByName(loginId, name);
            List<Book> author_books = lmsDatabase.findBooksByAuthor(loginId, author);
            name_books.retainAll(author_books);
            books = name_books;
        } else {
            throw new InvalidParametersException("Invalid parameters");
        }

        List<EntityModel<Book>> bookResources = new ArrayList<>();

        for(Book book : books){
            EntityModel<Book> resource = EntityModel.of(book);
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).getBookById(book.getId(), loginId)).withRel("get-book"));
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).searchBooks(name, author, loginId)).withSelfRel());
            if (lmsDatabase.checkLoginIdIsLibrarian(loginId)) {
                resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).deleteBook(book.getId(), loginId)).withRel("delete-book"));
                resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).addBorrowBook(null, loginId)).withRel("borrow-book"));
            }

            bookResources.add(resource);
        }
        CollectionModel<EntityModel<Book>> resources = CollectionModel.of(bookResources);
        return ResponseEntity.ok(resources);
    }

    @PostMapping(value = "/book/borrow", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ReturnTypeResponse>> addBorrowBook(@RequestBody AddBorrowBookRequest borrowBook, @RequestHeader("Login-ID") Long loginId) {
        ReturnType result = lmsDatabase.addBorrowBook(loginId,
                borrowBook.getBorrowBook().getStudentId(),
                borrowBook.getBorrowBook().getBookId(),
                borrowBook.getBorrowBook().getBorrowingTime(),
                borrowBook.getBorrowBook().getDueDate());

        ReturnTypeResponse returnTypeResponse = new ReturnTypeResponse();
        returnTypeResponse.setReturnVal(result);
        EntityModel<ReturnTypeResponse> resource = EntityModel.of(returnTypeResponse);
        if (lmsDatabase.checkLoginIdIsLibrarian(loginId)) {
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).addBorrowBook(null, loginId)).withSelfRel());
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).addReturnBook(null, loginId)).withRel("return-book"));
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).deleteBook(borrowBook.getBorrowBook().getBookId(), loginId)).withRel("delete-book"));
        }
        return ResponseEntity.ok(resource);
    }

    @PutMapping(value = "/book/return", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ReturnTypeResponse>> addReturnBook(@RequestBody AddReturnBookRequest returnBook, @RequestHeader("Login-ID") Long loginId) {
        ReturnType result = lmsDatabase.addReturnBook(
                loginId,
                returnBook.getReturnBook().getStudentId(),
                returnBook.getReturnBook().getBookId(),
                returnBook.getReturnBook().getReturningTime()
        );
        ReturnTypeResponse returnTypeResponse = new ReturnTypeResponse();
        returnTypeResponse.setReturnVal(result);
        EntityModel<ReturnTypeResponse> resource = EntityModel.of(returnTypeResponse);
        if (lmsDatabase.checkLoginIdIsLibrarian(loginId)) {
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).addReturnBook(null, loginId)).withSelfRel());
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).deleteBook(returnBook.getReturnBook().getBookId(), loginId)).withRel("delete-book"));
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).getBookById(returnBook.getReturnBook().getBookId(), loginId)).withRel("get-book"));
        }
        return ResponseEntity.ok(resource);
    }

    @PostMapping(value = "/book/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ReturnTypeResponse>> addBook(@RequestBody AddBookRequest request, @RequestHeader("Login-ID") Long loginId) {
        Long bookId = lmsDatabase.addBook(loginId, request.getAddBook().getName(), request.getAddBook().getType(), request.getAddBook().getAuthor(), request.getAddBook().getLocation());
        ReturnTypeResponse returnTypeResponse = new ReturnTypeResponse();
        returnTypeResponse.setReturnLongVal(bookId);
        EntityModel<ReturnTypeResponse> resource = EntityModel.of(returnTypeResponse);
        if (lmsDatabase.checkLoginIdIsLibrarian(loginId)) {
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).addBook(null, loginId)).withSelfRel());
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).deleteBook(bookId, loginId)).withRel("delete-book"));
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).searchBooks(null, null, loginId)).withRel("search-books"));
        }
        return ResponseEntity.ok(resource);
    }

    @DeleteMapping(value = "/book/{bookId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ReturnTypeResponse>> deleteBook(@PathVariable Long bookId, @RequestHeader("Login-ID") Long loginId) {
        ReturnType result = lmsDatabase.deleteBook(loginId, bookId);
        ReturnTypeResponse returnTypeResponse = new ReturnTypeResponse();
        returnTypeResponse.setReturnVal(result);
        EntityModel<ReturnTypeResponse> resource = EntityModel.of(returnTypeResponse);
        if (lmsDatabase.checkLoginIdIsLibrarian(loginId)) {
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).deleteBook(bookId, loginId)).withSelfRel());
            resource.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LMSEndPoints.class).searchBooks(null, null, loginId)).withRel("search-books"));
        }
        return ResponseEntity.ok(resource);
    }
}