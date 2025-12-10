package com.example.crm_system_backend.service.serviceImpl;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.constants.ReportConstant;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.entity.downloadReport;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.exception.LeadException;
import com.example.crm_system_backend.exception.ReportException;
import com.example.crm_system_backend.helper.ReportExcelHelper;
import com.example.crm_system_backend.repository.DownloadReportHistoryRepo;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private ReportService reportService;

    @Mock
    private ReportExcelHelper helper;

    @Mock
    private CellStyle mockCellStyle;

    @Mock
    private DownloadReportHistoryRepo historyRepo;

    @Captor
    private ArgumentCaptor<downloadReport> downloadReportCaptor;

    private Workbook mockWorkbook;
    private XSSFSheet mockSummarySheet;
    private XSSFSheet mockUser1Sheet;
    private XSSFSheet mockUser2Sheet;
    private ReportService spyReportService;

    private Set<Lead> mockLeadList;
    private Date startDate;
    private Date endDate;
    private String expectedStartDate;
    private String expectedEndDate;

    private final String TEST_EMAIL = "test.user@example.com";
    private final String TEST_NAME = "Test User";
    private final String EXPECTED_DATE_RANGE = "2025-01-01 To 2025-01-31";
    private final String EXPECTED_DOWNLOAD_TIME = "2025-02-15 10:30:00";

    // Instant used for the fixed clock setup
    private final Instant fixedInstant = Instant.parse("2025-02-15T10:30:00Z");
    private final ZoneId systemZone = ZoneId.of("Europe/London"); // Use a fixed time zone for consistency

    // A mock data signature written by ListToExcelStream
    private static final byte[] MOCK_EXCEL_CONTENT = "MOCK_EXCEL_DATA".getBytes();

    @BeforeEach
    void setUp() {
        // Setup dates for predictable filename generation
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2025, Calendar.JANUARY, 1, 0, 0, 0);
        startDate = calendar.getTime();

        calendar.set(2025, Calendar.JANUARY, 31, 0, 0, 0);
        endDate = calendar.getTime();

        expectedStartDate = "2025-01-01";
        expectedEndDate = "2025-01-31";

        // Setup leads
        mockLeadList = new HashSet<>(Collections.singletonList(
                new Lead(1L, startDate, new User(10L, "Test", "test@test.com"))
        ));

        // for saveInDb
        ReportService actualService = new ReportService(helper, historyRepo);

        this.reportService = Mockito.spy(actualService);
        startDate = Date.from(LocalDate.of(2025, 1, 1).atStartOfDay(systemZone).toInstant());
        endDate = Date.from(LocalDate.of(2025, 1, 31).atStartOfDay(systemZone).toInstant());

        when(helper.getName(TEST_EMAIL)).thenReturn(TEST_NAME);
    }


    // ----- listToExcelStream -----

    @Test
    void listToExcelStream_Success() throws Exception {

        Date startDate = new Date(System.currentTimeMillis() - 86400000);
        Date endDate = new Date(System.currentTimeMillis());

        User user1 = new User(1L, "John", "john@example.com");
        User user2 = new User(2L, "Jane", "jane@example.com");

        Lead lead1 = new Lead(101L, startDate, user1);
        Lead lead2 = new Lead(102L, startDate, user1);
        Lead lead3 = new Lead(103L, endDate, user2);

        Set<Lead> leads = new HashSet<>(Arrays.asList(lead1, lead2, lead3));
        OutputStream outputStream = new ByteArrayOutputStream();

        when(helper.headStyle(any())).thenReturn(mockCellStyle);
        when(helper.headerStyle(any())).thenReturn(mockCellStyle);
        when(helper.dataStyle(any())).thenReturn(mockCellStyle);

        try (MockedConstruction<XSSFWorkbook> mockedWorkbook = mockConstruction(XSSFWorkbook.class, (mock, context) -> {

            mockWorkbook = mock;

            mockSummarySheet = mock(XSSFSheet.class); // Use XSSFSheet
            mockUser1Sheet = mock(XSSFSheet.class);   // Use XSSFSheet
            mockUser2Sheet = mock(XSSFSheet.class);   // Use XSSFSheet

            when(mockWorkbook.createSheet("Summary Report")).thenReturn(mockSummarySheet);
            when(mockWorkbook.createSheet("John_john@example.com")).thenReturn(mockUser1Sheet);
            when(mockWorkbook.createSheet("Jane_jane@example.com")).thenReturn(mockUser2Sheet);

        })) {

            spyReportService = spy(reportService);

            doNothing().when(spyReportService).SummaryReport(any(), any(), any(), any(), any(), any(), any(), any());
            doNothing().when(spyReportService).perUserReport(any(), any(), any(), any(), any(), anyInt(), any());

            spyReportService.ListToExcelStream(leads, startDate, endDate, outputStream);

            verify(spyReportService, times(1)).SummaryReport(
                    eq(mockCellStyle), eq(mockCellStyle), eq(mockCellStyle), eq(mockSummarySheet),
                    eq(ReportConstant.summaryReport_headers), any(), eq(startDate), eq(endDate));

            verify(spyReportService, times(2)).perUserReport(
                    eq(mockCellStyle), eq(mockCellStyle), eq(mockCellStyle), any(Sheet.class),
                    eq(ReportConstant.perUserReport_headers), eq(0), anyList());

            int summaryHeaderCount = ReportConstant.summaryReport_headers.length;
            int perUserHeaderCount = ReportConstant.perUserReport_headers.length;

            verify(mockSummarySheet, times(summaryHeaderCount)).autoSizeColumn(anyInt(), eq(true));
            verify(mockUser1Sheet, times(perUserHeaderCount)).autoSizeColumn(anyInt(), eq(true));
            verify(mockUser2Sheet, times(perUserHeaderCount)).autoSizeColumn(anyInt(), eq(true));

            verify(mockWorkbook, times(1)).write(outputStream);
            verify(mockWorkbook, times(1)).close();
        }
    }

    @Test
    void listToExcelStream_Fail_NullLeads_ThrowsReportException() throws Exception {
        Date startDate = new Date();
        Date endDate = new Date();
        OutputStream outputStream = new ByteArrayOutputStream();

        try (MockedConstruction<XSSFWorkbook> mockedWorkbook = mockConstruction(XSSFWorkbook.class)) {
            // Spy the ReportService
            spyReportService = spy(reportService);

            ReportException thrown = assertThrows(ReportException.class, () -> {
                spyReportService.ListToExcelStream(null, startDate, endDate, outputStream);
            });

            // Verification
            assertEquals(ErrorCode.EMPTY_LEAD_LIST, thrown.getErrorCode());

            // Verify no Excel creation/writing happened after the check
            verify(mockedWorkbook.constructed().get(0), never()).write(any());
        }
    }

    @Test
    void listToExcelStream_Fail_EmptyLeadsSet_ThrowsLeadException() throws Exception {
        // --- 1. Setup Input Data ---
        Date startDate = new Date();
        Date endDate = new Date();
        OutputStream outputStream = new ByteArrayOutputStream();
        Set<Lead> emptyLeads = Collections.emptySet();

        // --- 2. Mock Style Creation (must happen before workbook creation) ---
        when(helper.headStyle(any())).thenReturn(mockCellStyle);
        when(helper.headerStyle(any())).thenReturn(mockCellStyle);
        when(helper.dataStyle(any())).thenReturn(mockCellStyle);


        // --- 3. Mock POI and Internal Method Calls ---
        try (MockedConstruction<XSSFWorkbook> mockedWorkbook = mockConstruction(XSSFWorkbook.class, (mock, context) -> {
            mockWorkbook = mock;
            when(mock.createSheet(anyString())).thenReturn((XSSFSheet) mockSummarySheet);
        })) {
            // Spy the ReportService
            spyReportService = spy(reportService);

            // --- 4. Execute & Verify Exception ---
            LeadException thrown = assertThrows(LeadException.class, () -> {
                spyReportService.ListToExcelStream(emptyLeads, startDate, endDate, outputStream);
            });

            // --- 5. Verification ---
            assertEquals(ErrorCode.LEAD_NOT_FOUND, thrown.getErrorCode());

            // Verify that the workbook was created but writing was prevented
            verify(mockWorkbook, never()).write(any());
        }
    }

    @Test
    void listToExcelStream_Fail_SummaryReportThrowsException() throws Exception {
        Date startDate = new Date();
        Date endDate = new Date();
        OutputStream outputStream = new ByteArrayOutputStream();
        Set<Lead> leads = new HashSet<>(Arrays.asList(new Lead(101L, startDate, new User(1L, "UserA", "userA@example.com"))));

        when(helper.headStyle(any())).thenReturn(mockCellStyle);
        when(helper.headerStyle(any())).thenReturn(mockCellStyle);
        when(helper.dataStyle(any())).thenReturn(mockCellStyle);

        try (MockedConstruction<XSSFWorkbook> mockedWorkbook = mockConstruction(XSSFWorkbook.class, (mock, context) -> {
            mockWorkbook = mock;
            when(mock.createSheet(anyString())).thenReturn((XSSFSheet) mockSummarySheet);
        })) {
            spyReportService = spy(reportService);

            doThrow(new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION))
                    .when(spyReportService).SummaryReport(any(), any(), any(), any(), any(), any(), any(), any());

            ExcelException thrown = assertThrows(ExcelException.class, () -> {
                spyReportService.ListToExcelStream(leads, startDate, endDate, outputStream);
            });

            assertEquals(ErrorCode.FILE_PROCESSING_EXCEPTION, thrown.getErrorCode());

            verify(mockWorkbook, never()).write(any());
        }
    }

    @Test
    void listToExcelStream_Fail_PerUserReportThrowsException() throws Exception {

        Date startDate = new Date();
        Date endDate = new Date();
        OutputStream outputStream = new ByteArrayOutputStream();
        User user1 = new User(1L, "John", "john@example.com");
        Set<Lead> leads = new HashSet<>(Arrays.asList(new Lead(101L, startDate, user1)));

        when(helper.headStyle(any())).thenReturn(mockCellStyle);
        when(helper.headerStyle(any())).thenReturn(mockCellStyle);
        when(helper.dataStyle(any())).thenReturn(mockCellStyle);

        try (MockedConstruction<XSSFWorkbook> mockedWorkbook = mockConstruction(XSSFWorkbook.class, (mock, context) -> {
            mockWorkbook = mock;
            mockSummarySheet = mock(XSSFSheet.class);
            mockUser1Sheet = mock(XSSFSheet.class);
            when(mock.createSheet("Summary Report")).thenReturn(mockSummarySheet);
            when(mock.createSheet("John_john@example.com")).thenReturn(mockUser1Sheet);
        })) {
            spyReportService = spy(reportService);

            doNothing().when(spyReportService).SummaryReport(any(), any(), any(), any(), any(), any(), any(), any());

            doThrow(new ExcelException(ErrorCode.FILE_PROCESSING_EXCEPTION))
                    .when(spyReportService).perUserReport(any(), any(), any(), any(), any(), anyInt(), any());

            ExcelException thrown = assertThrows(ExcelException.class, () -> {
                spyReportService.ListToExcelStream(leads, startDate, endDate, outputStream);
            });

            assertEquals(ErrorCode.FILE_PROCESSING_EXCEPTION, thrown.getErrorCode());

            verify(mockWorkbook, never()).write(any());
        }
    }


    // ----- excelToZipConverter -----

    @Test
    void excelToZipConverter_Success() throws Exception {

        doAnswer(invocation -> {
            OutputStream os = invocation.getArgument(3);
            os.write(MOCK_EXCEL_CONTENT);
            return null;
        }).when(reportService).ListToExcelStream(any(), any(), any(), any());

        ResponseEntity<StreamingResponseBody> responseEntity =
                reportService.excelToZipConverter(mockLeadList, startDate, endDate);

        assertEquals(200, responseEntity.getStatusCodeValue());
        assertTrue(responseEntity.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE));
        assertTrue(responseEntity.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION));

        assertEquals(MediaType.parseMediaType("application/zip"), responseEntity.getHeaders().getContentType());

        String expectedZipFileName = "COVORO Report-" + expectedStartDate + " To " + expectedEndDate + ".zip";
        String expectedContentDisposition = "form-data; name=\"attachment\"; filename=\"" + expectedZipFileName + "\"";
        assertEquals(expectedContentDisposition, responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));

        StreamingResponseBody responseBody = responseEntity.getBody();
        assertNotNull(responseBody);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        responseBody.writeTo(baos);

        verify(reportService, times(1)).ListToExcelStream(eq(mockLeadList), eq(startDate), eq(endDate), any(OutputStream.class));

        try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
             ZipInputStream zis = new ZipInputStream(bais)) {

            ZipEntry entry = zis.getNextEntry();
            assertNotNull(entry, "ZIP file must contain an entry.");

            String expectedExcelFileName = "COVORO Report-" + expectedStartDate + " To " + expectedEndDate + ".xlsx";
            assertEquals(expectedExcelFileName, entry.getName(), "ZIP entry name must match expected Excel filename.");

            byte[] buffer = new byte[MOCK_EXCEL_CONTENT.length];
            int bytesRead = zis.read(buffer, 0, buffer.length);

            assertEquals(MOCK_EXCEL_CONTENT.length, bytesRead, "The size of content read must match mock data size.");
            assertArrayEquals(MOCK_EXCEL_CONTENT, buffer, "The extracted content must match the mock Excel data.");
        }
    }

    @Test
    void excelToZipConverter_Fail_EmptyLeadList_ReturnsNoContent() throws IOException {

        Set<Lead> emptyLeadList = Collections.emptySet();

        ResponseEntity<StreamingResponseBody> responseEntity =
                reportService.excelToZipConverter(emptyLeadList, startDate, endDate);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode(), "Should return 204 No Content for empty list.");
        assertNull(responseEntity.getBody(), "Response body should be null.");
        verify(reportService, never()).ListToExcelStream(any(), any(), any(), any());
    }

    @Test
    void excelToZipConverter_Fail_ListToExcelStreamThrowsIOException_ThrowsExcelException() throws Exception {

        doThrow(new IOException("Simulated file write error"))
                .when(reportService).ListToExcelStream(any(), any(), any(), any());

        ResponseEntity<StreamingResponseBody> responseEntity =
                reportService.excelToZipConverter(mockLeadList, startDate, endDate);

        StreamingResponseBody responseBody = responseEntity.getBody();
        assertNotNull(responseBody);

        ExcelException thrown = assertThrows(ExcelException.class, () -> {
            responseBody.writeTo(new ByteArrayOutputStream());
        });

        assertEquals(ErrorCode.ERROR_IN_FILE_DOWNLOAD, thrown.getErrorCode());
        verify(reportService, times(1)).ListToExcelStream(any(), any(), any(), any());
    }

    // saveInDb

    @Test
    void saveInDb_Success() {

        when(helper.getName(TEST_EMAIL)).thenReturn(TEST_NAME);
        reportService.saveInDb(startDate, endDate, TEST_EMAIL);
        verify(historyRepo, times(1)).save(downloadReportCaptor.capture());
        downloadReport capturedReport = downloadReportCaptor.getValue();
        assertEquals(EXPECTED_DATE_RANGE, capturedReport.getDateRange());

        assertEquals(TEST_EMAIL, capturedReport.getEmail());

        assertEquals(TEST_NAME, capturedReport.getUserName());

        assertEquals("Success", capturedReport.getStatus());

        assertTrue(capturedReport.getDownloadedAt().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "DownloadedAt should be in 'yyyy-MM-dd HH:mm:ss' format.");

        int year = Integer.parseInt(capturedReport.getDownloadedAt().substring(0, 4));
        assertTrue(year >= 2025, "DownloadedAt year should be current or future.");
    }

    @Test
    void saveInDb_DateConversionEdgeCase_DifferentTimeZone() {

        reportService.saveInDb(startDate, endDate, TEST_EMAIL);

        verify(historyRepo, times(1)).save(downloadReportCaptor.capture());
        downloadReport capturedReport = downloadReportCaptor.getValue();

        assertEquals(EXPECTED_DATE_RANGE, capturedReport.getDateRange());
    }

    @Test
    void saveInDb_HelperThrowsException_StillSavesWithNullName() {

        when(helper.getName(TEST_EMAIL)).thenThrow(new RuntimeException("Simulated helper error"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            reportService.saveInDb(startDate, endDate, TEST_EMAIL);
        });

        assertEquals("Simulated helper error", thrown.getMessage());

        verify(historyRepo, never()).save(any(downloadReport.class));
    }
}
