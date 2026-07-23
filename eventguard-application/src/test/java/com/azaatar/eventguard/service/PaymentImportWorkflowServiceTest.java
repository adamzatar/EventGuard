package com.azaatar.eventguard.service;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import com.azaatar.eventguard.domain.RejectionStatus;
import com.azaatar.eventguard.ingestion.PaymentFileReader;
import com.azaatar.eventguard.parsing.PaymentParser;
import com.azaatar.eventguard.persistence.PaymentImportAttempt;
import com.azaatar.eventguard.persistence.PaymentImportRepository;
import com.azaatar.eventguard.persistence.PersistenceException;
import com.azaatar.eventguard.pojo.ParseStatus;
import com.azaatar.eventguard.pojo.PaymentParseResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentImportWorkflowServiceTest {

    private static final long SAVED_IMPORT_ID = 123L;

    @Test
    public void givenNullImportServiceWhenCreatedThenThrowsNullPointerException() {
        assertThrows(
                NullPointerException.class,
                () -> new PaymentImportWorkflowService(
                        null,
                        new PaymentProcessingService(),
                        new CapturingPaymentImportRepository()
                )
        );
    }

    @Test
    public void givenNullProcessingServiceWhenCreatedThenThrowsNullPointerException() {
        assertThrows(
                NullPointerException.class,
                () -> new PaymentImportWorkflowService(
                        createImportService(createSuccessfulParseResult(List.of(createPaymentRecord("PAY-001")))),
                        null,
                        new CapturingPaymentImportRepository()
                )
        );
    }

    @Test
    public void givenNullPaymentImportRepositoryWhenCreatedThenThrowsNullPointerException() {
        assertThrows(
                NullPointerException.class,
                () -> new PaymentImportWorkflowService(
                        createImportService(createSuccessfulParseResult(List.of(createPaymentRecord("PAY-001")))),
                        new PaymentProcessingService(),
                        null
                )
        );
    }

    @Test
    public void givenNullSourcePathWhenImportPaymentsThenThrowsNullPointerException() {
        // Arrange
        PaymentImportWorkflowService workflowService = createWorkflowService(
                createSuccessfulParseResult(List.of(createPaymentRecord("PAY-001"))),
                new CapturingPaymentImportRepository()
        );

        // Act and Assert
        assertThrows(
                NullPointerException.class,
                () -> workflowService.importPayments(null)
        );
    }

    @Test
    public void givenSuccessfulParseWhenImportPaymentsThenProcessesAndSavesAttempt() throws IOException {
        // Arrange
        CapturingPaymentImportRepository repository = new CapturingPaymentImportRepository();
        PaymentParseResult parseResult = createSuccessfulParseResult(List.of(
                createPaymentRecord("PAY-001"),
                createPaymentRecord("PAY-001")
        ));
        PaymentImportWorkflowService workflowService = createWorkflowService(parseResult, repository);

        // Act
        long importId = workflowService.importPayments(Path.of("input", "payments.csv"));

        // Assert
        PaymentImportAttempt savedAttempt = repository.getSavedAttempt();

        assertEquals(SAVED_IMPORT_ID, importId);
        assertNotNull(savedAttempt);
        assertEquals("payments.csv", savedAttempt.getSourceName());
        assertNotNull(savedAttempt.getImportedAt());
        assertEquals(ParseStatus.SUCCESS, savedAttempt.getParseStatus());
        assertEquals("Document parsed successfully.", savedAttempt.getDescription());
        assertEquals(2, savedAttempt.getRecords().size());
        assertEquals(RejectionStatus.NONE, savedAttempt.getRecords().get(0).getRejectionStatus());
        assertEquals(RejectionStatus.DUPLICATE_PAYMENT_ID, savedAttempt.getRecords().get(1).getRejectionStatus());
    }

    @Test
    public void givenPartialSuccessParseWhenImportPaymentsThenProcessesAndSavesAttempt() throws IOException {
        // Arrange
        CapturingPaymentImportRepository repository = new CapturingPaymentImportRepository();
        PaymentParseResult parseResult = createPartialSuccessParseResult(List.of(
                createPaymentRecord("PAY-001"),
                createPaymentRecord("PAY-001"),
                createMissingAmountPaymentRecord("PAY-002")
        ));
        PaymentImportWorkflowService workflowService = createWorkflowService(parseResult, repository);

        // Act
        long importId = workflowService.importPayments(Path.of("input", "payments.csv"));

        // Assert
        PaymentImportAttempt savedAttempt = repository.getSavedAttempt();

        assertEquals(SAVED_IMPORT_ID, importId);
        assertNotNull(savedAttempt);
        assertEquals("payments.csv", savedAttempt.getSourceName());
        assertEquals(ParseStatus.PARTIAL_SUCCESS, savedAttempt.getParseStatus());
        assertEquals("Document parsed with rejected rows.", savedAttempt.getDescription());
        assertEquals(3, savedAttempt.getRecords().size());
        assertEquals(RejectionStatus.NONE, savedAttempt.getRecords().get(0).getRejectionStatus());
        assertEquals(RejectionStatus.DUPLICATE_PAYMENT_ID, savedAttempt.getRecords().get(1).getRejectionStatus());
        assertEquals(RejectionStatus.MISSING_AMOUNT, savedAttempt.getRecords().get(2).getRejectionStatus());
    }

    @Test
    public void givenFailedParseWhenImportPaymentsThenSkipsProcessingAndSavesEmptyAttempt() throws IOException {
        // Arrange
        CapturingPaymentImportRepository repository = new CapturingPaymentImportRepository();
        PaymentParseResult parseResult = createFailedParseResult();
        PaymentImportWorkflowService workflowService = createWorkflowService(parseResult, repository);

        // Act
        long importId = workflowService.importPayments(Path.of("input", "bad-payments.csv"));

        // Assert
        PaymentImportAttempt savedAttempt = repository.getSavedAttempt();

        assertEquals(SAVED_IMPORT_ID, importId);
        assertNotNull(savedAttempt);
        assertEquals("bad-payments.csv", savedAttempt.getSourceName());
        assertEquals(ParseStatus.FAILURE, savedAttempt.getParseStatus());
        assertEquals("Document is blank.", savedAttempt.getDescription());
        assertEquals(0, savedAttempt.getRecords().size());
    }

    @Test
    public void givenReaderThrowsIOExceptionWhenImportPaymentsThenIOExceptionPropagates() {
        // Arrange
        PaymentImportService importService = new PaymentImportService(
                new FailingPaymentFileReader(),
                new StubPaymentParser(createSuccessfulParseResult(List.of(createPaymentRecord("PAY-001"))))
        );

        PaymentImportWorkflowService workflowService = new PaymentImportWorkflowService(
                importService,
                new PaymentProcessingService(),
                new CapturingPaymentImportRepository()
        );

        // Act and Assert
        assertThrows(
                IOException.class,
                () -> workflowService.importPayments(Path.of("input", "payments.csv"))
        );
    }

    @Test
    public void givenRepositoryThrowsPersistenceExceptionWhenImportPaymentsThenPersistenceExceptionPropagates() {
        // Arrange
        PaymentImportWorkflowService workflowService = createWorkflowService(
                createSuccessfulParseResult(List.of(createPaymentRecord("PAY-001"))),
                new FailingPaymentImportRepository()
        );

        // Act and Assert
        assertThrows(
                PersistenceException.class,
                () -> workflowService.importPayments(Path.of("input", "payments.csv"))
        );
    }

    private PaymentImportWorkflowService createWorkflowService(
            PaymentParseResult parseResult,
            PaymentImportRepository repository
    ) {
        return new PaymentImportWorkflowService(
                createImportService(parseResult),
                new PaymentProcessingService(),
                repository
        );
    }

    private PaymentImportService createImportService(PaymentParseResult parseResult) {
        return new PaymentImportService(
                new StubPaymentFileReader("payment document"),
                new StubPaymentParser(parseResult)
        );
    }

    private PaymentParseResult createSuccessfulParseResult(List<PaymentRecord> records) {
        return new PaymentParseResult(
                ParseStatus.SUCCESS,
                "Document parsed successfully.",
                records
        );
    }

    private PaymentParseResult createPartialSuccessParseResult(List<PaymentRecord> records) {
        return new PaymentParseResult(
                ParseStatus.PARTIAL_SUCCESS,
                "Document parsed with rejected rows.",
                records
        );
    }

    private PaymentParseResult createFailedParseResult() {
        return new PaymentParseResult(
                ParseStatus.FAILURE,
                "Document is blank.",
                List.of()
        );
    }

    private PaymentRecord createPaymentRecord(String paymentId) {
        return new PaymentRecord(
                paymentId,
                "ACC-001",
                "Adam Zaatar",
                "adam@example.com",
                new BigDecimal("100.00"),
                "JOD",
                PaymentStatus.PENDING
        );
    }

    private PaymentRecord createMissingAmountPaymentRecord(String paymentId) {
        PaymentRecord record = new PaymentRecord(
                paymentId,
                "ACC-001",
                "Adam Zaatar",
                "adam@example.com",
                null,
                "JOD",
                PaymentStatus.PENDING
        );

        record.setRejectionStatus(RejectionStatus.MISSING_AMOUNT);

        return record;
    }

    private static class StubPaymentFileReader implements PaymentFileReader {

        private final String document;

        private StubPaymentFileReader(String document) {
            this.document = document;
        }

        @Override
        public String read(Path sourcePath) {
            return document;
        }
    }

    private static class FailingPaymentFileReader implements PaymentFileReader {

        @Override
        public String read(Path sourcePath) throws IOException {
            throw new IOException("Failed to read file");
        }
    }

    private static class StubPaymentParser implements PaymentParser {

        private final PaymentParseResult parseResult;

        private StubPaymentParser(PaymentParseResult parseResult) {
            this.parseResult = parseResult;
        }

        @Override
        public PaymentParseResult parse(String document) {
            return parseResult;
        }
    }

    private static class CapturingPaymentImportRepository implements PaymentImportRepository {

        private PaymentImportAttempt savedAttempt;

        @Override
        public long save(PaymentImportAttempt attempt) {
            this.savedAttempt = attempt;
            return SAVED_IMPORT_ID;
        }

        private PaymentImportAttempt getSavedAttempt() {
            return savedAttempt;
        }
    }

    private static class FailingPaymentImportRepository implements PaymentImportRepository {

        @Override
        public long save(PaymentImportAttempt attempt) {
            throw new PersistenceException("Failed to save import attempt");
        }
    }
}