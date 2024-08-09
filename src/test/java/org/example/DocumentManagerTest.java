package org.example;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentManagerTest {

    private final DocumentManager documentManager = new DocumentManager();

    private DocumentManager.Document createDocument(String id, String title, String content, DocumentManager.Author author) {
        return DocumentManager.Document.builder()
                .id(id)
                .title(title)
                .content(content)
                .author(author)
                .created(Instant.now())
                .build();
    }

    private DocumentManager.Author createAuthor(String name) {
        return DocumentManager.Author.builder()
                .id("1")
                .name(name)
                .build();
    }

    /**
     * GIVEN valid document with valid author
     * WHEN performing save method
     * THEN add document to map and return the document with unique id and non-null created field
     */
    @Test
    public void testSaveNewDocumentWithUniqueId() {
        DocumentManager.Author author = createAuthor("Author Name");
        DocumentManager.Document document = createDocument(null, "Title", "Content", author);

        DocumentManager.Document savedDocument = documentManager.save(document);

        assertNotNull(savedDocument.getId());
        assertEquals("1", savedDocument.getId());
        assertEquals("Title", savedDocument.getTitle());
        assertEquals("Content", savedDocument.getContent());
        assertEquals(author, savedDocument.getAuthor());
        assertNotNull(savedDocument.getCreated());

        assertTrue(documentManager.getStoredDocuments().containsKey(savedDocument.getId()));
        assertEquals(savedDocument, documentManager.getStoredDocuments().get(savedDocument.getId()));
    }

    /**
     * GIVEN null
     * WHEN performing save method
     * THEN throw {@link IllegalArgumentException} with message "Document cannot be null"
     */
    @Test
    public void testHandleNullDocumentInput() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> documentManager.save(null));

        assertEquals("Document cannot be null", exception.getMessage());
    }

    /**
     * GIVEN document with empty title and content fields
     * WHEN performing save method
     * THEN return document with empty title and content fields and non-null id and created fields
     */
    @Test
    public void testSaveDocumentWithEmptyFields() {
        DocumentManager.Document document = createDocument(null, "", "", null);

        DocumentManager.Document savedDocument = documentManager.save(document);

        assertNotNull(savedDocument.getId());
        assertEquals("", savedDocument.getTitle());
        assertEquals("", savedDocument.getContent());
        assertNotNull(savedDocument.getCreated());
    }


    /**
     * GIVEN two different documents
     * WHEN performing save method
     * THEN return documents with unique ids
     */
    @Test
    public void testIndexIncrementedCorrectly() {
        DocumentManager.Author author = createAuthor("Author Name");
        DocumentManager.Document document1 = createDocument(null, "Title1", "Content1", author);
        DocumentManager.Document document2 = createDocument(null, "Title2", "Content2", author);

        DocumentManager.Document savedDocument1 = documentManager.save(document1);
        DocumentManager.Document savedDocument2 = documentManager.save(document2);

        assertEquals("1", savedDocument1.getId());
        assertEquals("2", savedDocument2.getId());
    }

    /**
     * GIVEN document with existing in storage id
     * WHEN performing save method
     * THEN return document with updated fields
     */
    @Test
    public void testSaveDocumentWithExistingId() {
        DocumentManager.Author author = createAuthor("Author");
        DocumentManager.Document document = createDocument(null, "Title1", "Content1", null);

        DocumentManager.Document savedDocument = documentManager.save(document);
        DocumentManager.Document updatedDocument = createDocument("1", "Title2", "Content2", author);

        DocumentManager.Document savedUpdatedDocument = documentManager.save(updatedDocument);

        assertEquals("1", savedUpdatedDocument.getId());
        assertEquals("Title2", savedUpdatedDocument.getTitle());
        assertEquals("Content2", savedUpdatedDocument.getContent());
        assertEquals(author, savedUpdatedDocument.getAuthor());
        assertEquals(savedDocument.getCreated(), savedUpdatedDocument.getCreated());
    }

    /**
     * GIVEN valid search request
     * WHEN performing search method
     * THEN return list of corresponding documents
     */
    @Test
    public void testSearchWithNonNullFields() {
        DocumentManager.Author author = createAuthor("Author One");
        DocumentManager.Document document = createDocument("1", "Title One", "Content One", author);

        documentManager.save(document);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Title One"))
                .containsContents(List.of("Content One"))
                .authorIds(List.of("1"))
                .createdFrom(Instant.now().minusSeconds(3600))
                .createdTo(Instant.now().plusSeconds(3600))
                .build();

        List<DocumentManager.Document> result = documentManager.search(request);

        assertEquals(1, result.size());
        assertEquals("Title One", result.getFirst().getTitle());
    }

    /**
     * GIVEN valid search request with all fields equal to null
     * WHEN performing search method
     * THEN return list of all documents
     */
    @Test
    public void testSearchWithAllNullFields() {
        DocumentManager.Author author = createAuthor("Author One");
        DocumentManager.Document document = createDocument("1", "Title One", "Content One", author);

        documentManager.save(document);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder().build();

        List<DocumentManager.Document> result = documentManager.search(request);

        assertEquals(1, result.size());
    }

    /**
     * GIVEN null
     * WHEN performing search method
     * THEN return {@link IllegalArgumentException} with message "Request cannot be null"
     */
    @Test
    public void testSearchNullRequest() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> documentManager.search(null));
        assertEquals("Request cannot be null", exception.getMessage());
    }

    /**
     * GIVEN valid request with no match parameters
     * WHEN performing search method
     * THEN return empty results' list
     */
    @Test
    public void testSearchReturnsEmptyListIfNoDocumentsMatch() {
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Non Matching Title"))
                .containsContents(List.of("Non Matching Content"))
                .authorIds(List.of("Non Matching Author Id"))
                .createdFrom(Instant.now().minusSeconds(3600))
                .createdTo(Instant.now().minusSeconds(1800))
                .build();

        List<DocumentManager.Document> result = documentManager.search(request);

        assertTrue(result.isEmpty());
    }

    /**
     * GIVEN valid request with empty lists of params
     * WHEN performing search method
     * THEN return list of all documents in storage
     */
    @Test
    public void testSearchWithEmptyLists() {
        DocumentManager.Author author = createAuthor("Author One");
        DocumentManager.Document document = createDocument("1", "Title One", "Content One", author);

        documentManager.save(document);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(Collections.emptyList())
                .containsContents(Collections.emptyList())
                .authorIds(Collections.emptyList())
                .createdFrom(Instant.now().minusSeconds(3600))
                .createdTo(Instant.now().plusSeconds(3600))
                .build();

        List<DocumentManager.Document> result = documentManager.search(request);

        assertEquals(1, result.size());
    }

    /**
     * GIVEN valid request with invalid rage
     * WHEN performing search method
     * THEN return empty list of results
     */
    @Test
    public void testSearchCreatedFromAfterCreatedTo() {
        DocumentManager.Author author = createAuthor("Author One");
        DocumentManager.Document document = createDocument("1", "Title One", "Content One", author);

        documentManager.save(document);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Title One"))
                .containsContents(List.of("Content One"))
                .authorIds(List.of("1"))
                .createdFrom(Instant.now().plusSeconds(3600))
                .createdTo(Instant.now().minusSeconds(3600))
                .build();

        List<DocumentManager.Document> result = documentManager.search(request);

        assertTrue(result.isEmpty());
    }

    /**
     * GIVEN valid request with list of params with empty string
     * WHEN performing search method
     * THEN return valid list of results
     */
    @Test
    public void testSearchWithEmptyFields() {
        DocumentManager.Author author = createAuthor("Author One");
        DocumentManager.Document document = createDocument("1", "", "", author);

        documentManager.save(document);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of(""))
                .containsContents(List.of(""))
                .createdFrom(Instant.now().minusSeconds(3600))
                .createdTo(Instant.now().plusSeconds(3600))
                .build();

        List<DocumentManager.Document> result = documentManager.search(request);

        assertEquals(1, result.size());
        assertEquals("", result.getFirst().getTitle());
    }

    /**
     * GIVEN existing in storage id
     * WHEN performing findById method
     * THEN return {@link Optional} of correspondent document
     */
    @Test
    public void testFindByIdReturnsDocumentWhenIdExists() {
        DocumentManager.Author author = createAuthor("Author1");
        DocumentManager.Document document = createDocument("1", "Title1", "Content1", author);

        documentManager.save(document);

        Optional<DocumentManager.Document> result = documentManager.findById("1");

        assertTrue(result.isPresent());
        assertEquals("Title1", result.get().getTitle());
    }

    /**
     * GIVEN valid id and empty storage
     * WHEN performing findById method
     * THEN return empty {@link Optional}
     */
    @Test
    public void testFindByIdHandlesEmptyStoredDocuments() {
        Optional<DocumentManager.Document> result = documentManager.findById("1");

        assertTrue(result.isEmpty());
    }

    /**
     * GIVEN not existing in storage valid id
     * WHEN performing findById method
     * THEN return empty {@link Optional}
     */
    @Test
    public void testFindByIdReturnsOptionalEmptyWhenIdNotExists() {
        Optional<DocumentManager.Document> result = documentManager.findById("non_existent_id");

        assertTrue(result.isEmpty());
    }

    /**
     * GIVEN null
     * WHEN performing findById method
     * THEN throw {@link IllegalArgumentException} with message "Document id cannot be null or empty"
     */
    @Test
    public void testFindByIdThrowsExceptionWhenIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> documentManager.findById(null));

        assertEquals("Document id cannot be null or empty", exception.getMessage());
    }

    /**
     * GIVEN empty string as id
     * WHEN performing findById method
     * THEN throw {@link IllegalArgumentException} with message "Document id cannot be null or empty"
     */
    @Test
    public void testFindByIdHandlesEmptyId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> documentManager.findById(""));

        assertEquals("Document id cannot be null or empty", exception.getMessage());
    }
}