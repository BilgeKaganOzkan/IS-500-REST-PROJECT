package com.is550.lmsrestclient.client;

import com.is550.lmsrestclient.variables.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LMSClient {
    private Long loginId = null;
    private List<Long> bookIdList = new ArrayList<>();
    private Long selectedBookId = null;
    private Long selectedStudentId = null;
    private String lastRef = "login";
    private int backLogin = 0;

    public void login(RestTemplate restTemplate, LoginRequest loginRequest) {
        String url = "http://localhost:9090/login";
        loginId = null;
        bookIdList = new ArrayList<>();
        selectedBookId = null;
        selectedStudentId = null;
        lastRef = "login";
        backLogin = 1;
        List<Link> linksList = new ArrayList<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<EntityModel<UserLoginInfos>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<EntityModel<UserLoginInfos>>() {}
        );

        EntityModel<UserLoginInfos> userInfo = response.getBody();
        System.out.println("\nWelcome Start Menu");

        loginId = userInfo.getContent().getUserId();

        linksList = userInfo.getLinks().stream().collect(Collectors.toList());

        printAndSelectLinks(restTemplate, linksList);
    }

    private void printAndSelectLinks(RestTemplate restTemplate, List<Link> linksList) {
        try {
            if (backLogin != 0) {
                System.out.println("You can follow bolew operations:\n");
                AtomicInteger counter = new AtomicInteger(1);
                linksList.forEach(link -> {
                    if ("self".equals(link.getRel().value())) {
                        System.out.println(counter.get() + "-Self : " + link.getHref());
                    } else if ("get-book".equals(link.getRel().value())) {
                        System.out.println(counter.get() + "-Search Books By ID : " + link.getHref());
                    } else if ("search-books".equals(link.getRel().value())) {
                        System.out.println(counter.get() + "-Search Books By Name and/or Author : " + link.getHref());
                    } else if ("borrowing-info".equals(link.getRel().value())) {
                        System.out.println(counter.get() + "-Show User Informations : " + link.getHref());
                    } else if ("borrow-book".equals(link.getRel().value())) {
                        if (bookIdList.size() >= counter.get() - 1) {
                            System.out.println(counter.get() + "-Add Borrow Book : " + link.getHref() + " Book ID : " + bookIdList.get(counter.get()) + " Student ID" + selectedStudentId);
                        } else {
                            System.out.println(counter.get() + "-Add Borrow Book : " + link.getHref());
                        }
                    } else if ("return-book".equals(link.getRel().value())) {
                        if (bookIdList.size() >= counter.get() - 1) {
                            System.out.println(counter.get() + "-Add Return Book : " + link.getHref() + " Book ID : " + bookIdList.get(counter.get()) + " Student ID" + selectedStudentId);
                        } else {
                            System.out.println(counter.get() + "-Add Return Book : " + link.getHref());
                        }
                    } else if ("add-book".equals(link.getRel().value())) {
                        System.out.println(counter.get() + "-Add Book : " + link.getHref());
                    } else if ("delete-book".equals(link.getRel().value())) {
                        System.out.println(counter.get() + "-Delete Book : " + link.getHref());
                    }
                    counter.set(counter.get() + 1);
                });

                System.out.println(counter.get() + "-Back to Login Menu");
                System.out.println(counter.get() + 1 + "-Quit");
                counter.set(counter.get() + 2);

                System.out.println("\nEnter selection:");
                Scanner scanner = new Scanner(System.in);
                int selection = scanner.nextInt();
                scanner.nextLine();

                if (selection == counter.get() - 1) {
                    System.exit(0);
                }

                if (selection != counter.get() - 2) {
                    if (selection < counter.get()) {
                        if (bookIdList.size() >= counter.get() - 3) {
                            selectedBookId = bookIdList.get(selection - 1);
                        }
                        followLinks(restTemplate, linksList.get(selection - 1));
                    } else {
                        System.out.println("Wrong selection!");
                    }
                } else {
                    backLogin = 0;
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }
    }
    private void followLinks(RestTemplate restTemplate, Link link) {
        String selection = link.getRel().value();
        List<Link> linksList = new ArrayList<>();

        if ("self".equals(link.getRel().value())) {
            selection = lastRef;
        }

        if("login".equals(selection)){
            Scanner scanner = new Scanner(System.in);

            System.out.println("Enter email:");
            String email = scanner.nextLine();
            System.out.println("Enter password:");
            String password = scanner.nextLine();

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail(email);
            loginRequest.setPassword(password);

            login(restTemplate, loginRequest);
        }else if ("get-book".equals(selection)) {
            String regexPattern = "/book/(\\d+)";
            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(link.getHref());
            ResponseEntity<EntityModel<Book>> response;
            String urlTemplate;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Login-ID", String.valueOf(loginId));
            HttpEntity<?> entity = new HttpEntity<>(headers);

            if (matcher.find()) {
                urlTemplate = link.getHref();
            } else {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter book ID:");
                String bookId = scanner.nextLine();

                urlTemplate = link.getHref().replace("{bookId}", bookId);
            }

            response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<EntityModel<Book>>() {}
            );

            System.out.println("Book ID: " + response.getBody().getContent().getId());
            System.out.println("Book Name: " + response.getBody().getContent().getName());
            System.out.println("Book Author: " + response.getBody().getContent().getAuthor());
            System.out.println("Book Type: " + response.getBody().getContent().getType());
            System.out.println("Book Location: " + response.getBody().getContent().getLocation());
            System.out.println("Book Availability: " + response.getBody().getContent().getAvailable() + "\n");

            linksList = response.getBody().getLinks().stream().collect(Collectors.toList());
        }else if ("search-books".equals(selection)) {
            String urlTemplate;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Login-ID", String.valueOf(loginId));
            HttpEntity<?> entity = new HttpEntity<>(headers);

            if (!link.getHref().contains("{name}") || !link.getHref().contains("{author}")) {
                urlTemplate = link.getHref();
            } else {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter book name:");
                String bookName = scanner.nextLine();
                if (Objects.equals(bookName, "")){
                    bookName = "null";
                }
                System.out.println("Enter book author:");
                String bookAuthor = scanner.nextLine();
                if (Objects.equals(bookAuthor, "")){
                    bookAuthor = "null";
                }

                urlTemplate = link.getHref();

                if (urlTemplate.contains("{name}")) {
                    urlTemplate = urlTemplate.replace("{name}", bookName);
                }
                if (urlTemplate.contains("{author}")) {
                    urlTemplate = urlTemplate.replace("{author}", bookAuthor);
                }
            }

            ResponseEntity<CollectionModel<EntityModel<Book>>> response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<CollectionModel<EntityModel<Book>>>() {}
            );

            CollectionModel<EntityModel<Book>> collectionModel = response.getBody();
            if (collectionModel != null) {
                for (EntityModel<Book> responseBook : collectionModel.getContent()) {
                    System.out.println("Book ID: " + responseBook.getContent().getId());
                    System.out.println("Book Name: " + responseBook.getContent().getName());
                    System.out.println("Book Author: " + responseBook.getContent().getAuthor());
                    System.out.println("Book Type: " + responseBook.getContent().getType());
                    System.out.println("Book Location: " + responseBook.getContent().getLocation());
                    System.out.println("Book Availability: " + responseBook.getContent().getAvailable() + "\n");

                    List<Link> modelLinkList = responseBook.getLinks().stream().collect(Collectors.toList());
                    linksList.addAll(modelLinkList);
                }
            }

            Set<Link> linksSet = new LinkedHashSet<>(linksList);
            linksList = new ArrayList<>(linksSet);
        }else if ("borrowing-info".equals(selection)) {
            bookIdList = new ArrayList<>();
            selectedStudentId = null;
            String urlTemplate;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Login-ID", String.valueOf(loginId));
            HttpEntity<?> entity = new HttpEntity<>(headers);

            if (!link.getHref().contains("{studentId}")) {
                urlTemplate = link.getHref();
                int lastIndex = urlTemplate.lastIndexOf('/');

                if (lastIndex != -1) {
                    String lastPart = urlTemplate.substring(lastIndex + 1);
                    selectedStudentId = (long) Integer.parseInt(lastPart);
                }
            } else {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter student id:");
                String studentId = scanner.nextLine();

                urlTemplate = link.getHref().replace("{studentId}", studentId);

                selectedStudentId = (long) Integer.parseInt(studentId);
            }

            ResponseEntity<CollectionModel<EntityModel<UserBorrowingInfos>>> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<CollectionModel<EntityModel<UserBorrowingInfos>>>() {}
            );

            CollectionModel<EntityModel<UserBorrowingInfos>> collectionModel = response.getBody();
            if (collectionModel != null) {
                for (EntityModel<UserBorrowingInfos> borrowinrInfo : collectionModel.getContent()) {
                    System.out.println("Book ID: " + borrowinrInfo.getContent().getBook().getId());
                    System.out.println("Book Name: " + borrowinrInfo.getContent().getBook().getName());
                    System.out.println("Book Author: " + borrowinrInfo.getContent().getBook().getAuthor());
                    System.out.println("Book Type: " + borrowinrInfo.getContent().getBook().getType());
                    System.out.println("Book's Current Location: " + borrowinrInfo.getContent().getBook().getLocation());
                    System.out.println("Book's Current Availability: " + borrowinrInfo.getContent().getBook().getAvailable());
                    System.out.println("Borrowing Time: " + borrowinrInfo.getContent().getBorrowingTime());
                    System.out.println("Due Date: " + borrowinrInfo.getContent().getDueDate());
                    System.out.println("Returning Time: " + borrowinrInfo.getContent().getReturningTime());
                    System.out.println("Paid: " + borrowinrInfo.getContent().getPaid());
                    System.out.println("Fine: " + borrowinrInfo.getContent().getFine() + "\n");

                    List<Link> modelLinkList = borrowinrInfo.getLinks().stream().collect(Collectors.toList());
                    for(int i=0; i<modelLinkList.size(); i++){
                        bookIdList.add(borrowinrInfo.getContent().getBook().getId());
                    }
                    linksList.addAll(modelLinkList);
                }
            }
        }else if ("borrow-book".equals(selection)) {
            bookIdList = new ArrayList<>();
            String urlTemplate = link.getHref();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Login-ID", String.valueOf(loginId));

            Scanner scanner = new Scanner(System.in);

            BorrowBook borrowBook = new BorrowBook();

            if(selectedStudentId == null)
            {
                System.out.println("Enter book ID:");
                long borrowBookID = scanner.nextLong();
                scanner.nextLine();
                System.out.println("Enter student ID:");
                long borrowBookStudentId = scanner.nextLong();
                scanner.nextLine();

                borrowBook.setStudentId(borrowBookStudentId);
                borrowBook.setBookId(borrowBookID);

                selectedStudentId = borrowBookStudentId;
                selectedBookId = borrowBookID;
            }else {
                borrowBook.setStudentId(selectedStudentId);
                borrowBook.setBookId(selectedBookId);
            }

            System.out.println("Enter borrowing time: (in yyyy-MM-dd:HH-mm-ss format)");
            String borrowingTime = scanner.nextLine();
            System.out.println("Enter due date: (in yyyy-MM-dd:HH-mm-ss format)");
            String dueDate = scanner.nextLine();

            try{
                borrowBook.setBorrowingTime(convertStringToXMLGregorianCalendar(borrowingTime));
                borrowBook.setDueDate(convertStringToXMLGregorianCalendar(dueDate));
            }catch (DatatypeConfigurationException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            AddBorrowBookRequest borrowBookRequest = new AddBorrowBookRequest();
            borrowBookRequest.setBorrowBook(borrowBook);
            borrowBookRequest.setLoginId(loginId);

            HttpEntity<AddBorrowBookRequest> entity = new HttpEntity<>(borrowBookRequest, headers);

            ResponseEntity<EntityModel<ReturnTypeResponse>> response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<EntityModel<ReturnTypeResponse>>() {}
            );
            System.out.println("\nReturn: " + response.getBody().getContent().getReturnVal());
            List<Link> modelLinkList = response.getBody().getLinks().stream().collect(Collectors.toList());
            linksList.addAll(modelLinkList);
        }else if ("return-book".equals(selection)) {
            bookIdList = new ArrayList<>();
            String urlTemplate = link.getHref();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Login-ID", String.valueOf(loginId));

            Scanner scanner = new Scanner(System.in);

            ReturnBook returnBook = new ReturnBook();

            if(selectedStudentId == null)
            {
                System.out.println("Enter book ID:");
                long borrowBookID = scanner.nextLong();
                scanner.nextLine();
                System.out.println("Enter student ID:");
                long borrowBookStudentId = scanner.nextLong();
                scanner.nextLine();

                returnBook.setStudentId(borrowBookStudentId);
                returnBook.setBookId(borrowBookID);

                selectedStudentId = borrowBookStudentId;
                selectedBookId = borrowBookID;
            }else {
                returnBook.setStudentId(selectedStudentId);
                returnBook.setBookId(selectedBookId);
            }

            System.out.println("Enter returning time: (in yyyy-MM-dd:HH-mm-ss format)");
            String returningTime = scanner.nextLine();

            try{
                returnBook.setReturningTime(convertStringToXMLGregorianCalendar(returningTime));
            }catch (DatatypeConfigurationException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            AddReturnBookRequest returnBookRequest = new AddReturnBookRequest();
            returnBookRequest.setReturnBook(returnBook);
            returnBookRequest.setLoginId(loginId);

            HttpEntity<AddReturnBookRequest> entity = new HttpEntity<>(returnBookRequest, headers);

            ResponseEntity<EntityModel<ReturnTypeResponse>> response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<EntityModel<ReturnTypeResponse>>() {}
            );
            System.out.println("Return: " + response.getBody().getContent().getReturnVal());
            List<Link> modelLinkList = response.getBody().getLinks().stream().collect(Collectors.toList());
            linksList.addAll(modelLinkList);
        }else if("add-book".equals(selection)){
            String urlTemplate = link.getHref();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Login-ID", String.valueOf(loginId));

            Scanner scanner = new Scanner(System.in);

            System.out.println("Enter book name:");
            String bookNameToAdd = scanner.nextLine();
            System.out.println("Enter book type:");
            String bookType = scanner.nextLine();
            System.out.println("Enter author name:");
            String bookAuthor = scanner.nextLine();
            System.out.println("Enter book location:");
            String bookLocation = scanner.nextLine();
            AddBook addBook = new AddBook();
            addBook.setName(bookNameToAdd);
            addBook.setType(convertStringToType(bookType));
            addBook.setAuthor(bookAuthor);
            addBook.setLocation(bookLocation);

            AddBookRequest addBookrequest = new AddBookRequest();
            addBookrequest.setAddBook(addBook);
            addBookrequest.setLoginId(loginId);

            HttpEntity<AddBookRequest> entity = new HttpEntity<>(addBookrequest, headers);

            ResponseEntity<EntityModel<ReturnTypeResponse>> response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<EntityModel<ReturnTypeResponse>>() {}
            );
            System.out.println("New added book ID: " + response.getBody().getContent().getReturnLongVal());
            List<Link> modelLinkList = response.getBody().getLinks().stream().collect(Collectors.toList());
            linksList.addAll(modelLinkList);
        }else if ("delete-book".equals(selection)) {
            bookIdList = new ArrayList<>();
            selectedStudentId = null;
            String urlTemplate;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Login-ID", String.valueOf(loginId));
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            if (!link.getHref().contains("{bookId}")) {
                urlTemplate = link.getHref();
            }else {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter book id:");
                String bookId = scanner.nextLine();

                urlTemplate = link.getHref().replace("{bookId}", bookId);
            }


            ResponseEntity<EntityModel<ReturnTypeResponse>> response = restTemplate.exchange(
                    urlTemplate,
                    HttpMethod.DELETE,
                    entity,
                    new ParameterizedTypeReference<EntityModel<ReturnTypeResponse>>() {}
            );
            System.out.println("Return: " + response.getBody().getContent().getReturnVal());
            List<Link> modelLinkList = response.getBody().getLinks().stream().collect(Collectors.toList());
            linksList.addAll(modelLinkList);
        }
        else {
            throw new RuntimeException("Invalid selection");
        }

        lastRef = selection;
        printAndSelectLinks(restTemplate, linksList);
}

    private static XMLGregorianCalendar convertStringToXMLGregorianCalendar(String dateString) throws ParseException, DatatypeConfigurationException {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").parse(dateString);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date));
        } catch (ParseException | DatatypeConfigurationException e) {
            throw e;
        }
    }

    public static BookType convertStringToType(String type){
        if (type.equals("biology")){
            return BookType.BIOLOGY;
        } else if (type.equals("mathematic")) {
            return BookType.MATHEMATICS;
        }
        else {
            return BookType.MATHEMATICS;
        }
    }
}
