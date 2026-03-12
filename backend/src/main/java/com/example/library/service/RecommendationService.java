package com.example.library.service;

import com.example.library.dto.BookDtos;
import com.example.library.model.Book;
import com.example.library.model.LoanStatus;
import com.example.library.model.Rating;
import com.example.library.model.RecommendationProfile;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.RatingRepository;
import com.example.library.repository.RecommendationProfileRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {
    private final RecommendationProfileRepository profileRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final CurrentUserService currentUserService;
    private final RatingRepository ratingRepository;
    private final LoanRepository loanRepository;

    public RecommendationService(RecommendationProfileRepository profileRepository,
                                 BookRepository bookRepository,
                                 BookService bookService,
                                 CurrentUserService currentUserService,
                                 RatingRepository ratingRepository,
                                 LoanRepository loanRepository) {
        this.profileRepository = profileRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.currentUserService = currentUserService;
        this.ratingRepository = ratingRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional(readOnly = true)
    public Page<BookDtos.RecommendationResponse> getRecommendations(Long userId, int page, int size, String source) {
        currentUserService.requireSameUserOrAdmin(userId);

        RecommendationProfile profile = profileRepository.findByUserId(userId).orElse(null);
        Set<String> userGenres = parseCsv(profile == null ? null : profile.getPreferredGenresCsv());
        Set<String> userAuthors = parseCsv(profile == null ? null : profile.getFavoriteAuthorsCsv());

        Set<String> systemGenres = new LinkedHashSet<>();
        Set<String> systemAuthors = new LinkedHashSet<>();
        for (Rating rating : ratingRepository.findByUserIdAndScoreGreaterThanEqual(userId, 4)) {
            Long bookId = rating.getBook().getId();
            boolean returned = loanRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.RETURNED);
            if (!returned) {
                continue;
            }

            systemAuthors.add(normalize(rating.getBook().getAuthor()));
            parseCsv(rating.getBook().getGenresCsv()).forEach(systemGenres::add);
        }

        Set<String> requestedSources = parseRequestedSources(source);
        Specification<Book> candidateSpec = buildCandidateSpecification(
                requestedSources.contains("USER") ? userGenres : Set.of(),
                requestedSources.contains("USER") ? userAuthors : Set.of(),
                requestedSources.contains("SYSTEM") ? systemGenres : Set.of(),
                requestedSources.contains("SYSTEM") ? systemAuthors : Set.of()
        );
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> booksPage = bookRepository.findAll(candidateSpec, pageable);

        List<BookDtos.RecommendationResponse> content = booksPage.getContent().stream()
                .map(book -> toRecommendationResponse(book, userGenres, userAuthors, systemGenres, systemAuthors, requestedSources))
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(content, pageable, booksPage.getTotalElements());
    }

    private BookDtos.RecommendationResponse toRecommendationResponse(Book book,
                                                                     Set<String> userGenres,
                                                                     Set<String> userAuthors,
                                                                     Set<String> systemGenres,
                                                                     Set<String> systemAuthors,
                                                                     Set<String> requestedSources) {
        List<String> tags = new ArrayList<>();
        boolean userMatch = matchesBook(book, userGenres, userAuthors);
        boolean systemMatch = matchesBook(book, systemGenres, systemAuthors);

        if (userMatch) {
            tags.add("USER");
        }
        if (systemMatch) {
            tags.add("SYSTEM");
        }

        if (tags.isEmpty()) {
            return null;
        }
        if (requestedSources.size() == 1 && !tags.containsAll(requestedSources)) {
            return null;
        }

        return new BookDtos.RecommendationResponse(bookService.getById(book.getId()), tags);
    }

    private Set<String> parseRequestedSources(String source) {
        if ("system".equalsIgnoreCase(source)) {
            return Set.of("SYSTEM");
        }
        if ("user".equalsIgnoreCase(source)) {
            return Set.of("USER");
        }
        return Set.of("USER", "SYSTEM");
    }

    private Specification<Book> buildCandidateSpecification(Set<String> userGenres,
                                                            Set<String> userAuthors,
                                                            Set<String> systemGenres,
                                                            Set<String> systemAuthors) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            addSourcePredicates(predicates, root, cb, userGenres, userAuthors);
            addSourcePredicates(predicates, root, cb, systemGenres, systemAuthors);

            if (predicates.isEmpty()) {
                return cb.disjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private void addSourcePredicates(List<Predicate> predicates,
                                     jakarta.persistence.criteria.Root<Book> root,
                                     jakarta.persistence.criteria.CriteriaBuilder cb,
                                     Set<String> genres,
                                     Set<String> authors) {
        genres.stream()
                .filter(value -> !value.isBlank())
                .forEach(genre -> predicates.add(cb.like(cb.lower(cb.coalesce(root.get("genresCsv"), "")), "%" + genre + "%")));
        authors.stream()
                .filter(value -> !value.isBlank())
                .forEach(author -> predicates.add(cb.like(cb.lower(cb.coalesce(root.get("author"), "")), "%" + author + "%")));
    }

    private boolean matchesBook(Book book, Set<String> genres, Set<String> authors) {
        if (genres.isEmpty() && authors.isEmpty()) {
            return false;
        }
        String author = normalize(book.getAuthor());
        Set<String> bookGenres = parseCsv(book.getGenresCsv());

        boolean authorMatch = !authors.isEmpty() && authors.stream().anyMatch(a -> !a.isBlank() && author.contains(a));
        boolean genreMatch = !genres.isEmpty() && genres.stream().anyMatch(g -> bookGenres.stream().anyMatch(bg -> bg.contains(g)));
        return authorMatch || genreMatch;
    }

    private Set<String> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        Set<String> result = new LinkedHashSet<>();
        Arrays.stream(csv.split(","))
                .map(this::normalize)
                .filter(s -> !s.isBlank())
                .forEach(result::add);
        return result;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
