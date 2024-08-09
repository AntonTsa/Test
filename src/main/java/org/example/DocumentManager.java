package org.example;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;
import java.util.*;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */

public final class DocumentManager {

    private final Map<String, Document> storedDocuments = new HashMap<>();
    @Getter
    private int index = 1;


    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        if (document.getId() == null) {
            document.setId(generateId());
            document.setCreated(Instant.now());
        } else {
            document = updateExistingDocument(document);
        }

        storedDocuments.put(document.getId(), document);
        return document;
    }

    private Document updateExistingDocument(Document document) {
        Document existingDocument = storedDocuments.get(document.getId());
        if (existingDocument != null) {
            existingDocument.setTitle(document.getTitle());
            existingDocument.setAuthor(document.getAuthor());
            existingDocument.setContent(document.getContent());
            return existingDocument;
        } else {
            document.setCreated(Instant.now());
            return document;
        }
    }

    private String generateId() {
        while (storedDocuments.containsKey(String.valueOf(index))) {
            index++;
        }
        return String.valueOf(index);
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }
        return storedDocuments.values().stream()
                .filter(document -> matchesSearchCriteria(document, request))
                .toList();
    }

    private boolean matchesSearchCriteria(Document document, SearchRequest request) {
        return matchesCriteria(request.getTitlePrefixes(), document.getTitle())
                && matchesCriteria(request.getAuthorIds(), document.getAuthor().getId())
                && matchesCriteria(request.getContainsContents(), document.getContent())
                && matchesDateRange(document.getCreated(), request.getCreatedFrom(), request.getCreatedTo());
    }

    private boolean matchesCriteria(List<String> criteria, String field) {
        return field != null && (criteria == null || criteria.isEmpty() || criteria.contains(field));
    }

    private boolean matchesDateRange(Instant created, Instant from, Instant to) {
        if (created == null) {
            return false;
        }
        return (from == null || created.isAfter(from)) && (to == null || created.isBefore(to));
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Document id cannot be null or empty");
        }
        return Optional.ofNullable(storedDocuments.get(id));
    }

    public Map<String, Document> getStoredDocuments() {
        return new HashMap<>(this.storedDocuments);
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
