package com.example.crm_system_backend.service.Email;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.exception.ExcelException;
import com.example.crm_system_backend.exception.ReportException;
import com.example.crm_system_backend.service.Report.ReportService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.example.crm_system_backend.constants.ReportConstant.noDataText;

@Slf4j
@Component
public class AutoEmailScheduler {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ReportService reportService;


    private static final Logger LOGGER = Logger.getLogger(AutoEmailScheduler.class.getName());
    private static final String SENDER_EMAIL = "akanksha.senad@perennialsys.com";
    private static final String RECIPIENT_EMAIL = "akankshasenad21@gmail.com";


    /**
     * Schedules the email to be sent automatically.
     * Cron Expression format:
     * <seconds> <minutes> <hours> <day-of-month> <month> <day-of-week> <year> (year is optional)
     */
    @Scheduled(cron = "0 0 9 1 * *")
    public void scheduleMonthlyReportEmail() {

        LOGGER.log(Level.INFO,"Scheduling monthly report");

        try {
            sendMonthlyReport();
        } catch (Exception e) {
            log.error("Error sending monthly report Email", e);
            LOGGER.log(Level.WARNING, "Service :: Email :: AutoEmailScheduler :: scheduleMonthlyReportEmail", e);
            throw new ReportException(ErrorCode.FAILED_TO_SCHEDULE_EMAIL);
        }
    }


    /**
     * Calculates suitable start date and end date and compose email with or without attachment.
     * Attachment is provided only if any records are present in previous month.
     * @throws MessagingException
     */
    private void sendMonthlyReport() throws MessagingException {

        LOGGER.log(Level.INFO, "Sending monthly report Email");

        YearMonth previousMonth = YearMonth.now(ZoneId.systemDefault()).minusMonths(1);

        // Calculate Start and End Dates for the previous month
        LocalDate localStartDate = previousMonth.atDay(1);
        Date startDate = Date.from(localStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LocalDate localEndDate = previousMonth.atEndOfMonth();
        Date endDate = Date.from(localEndDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusNanos(1).toInstant());

        String reportName = previousMonth.getMonth().name() + "-" + previousMonth.getYear() + " Monthly Report";


        // --- Generate Report ---
        // The ResponseEntity contains the StreamingResponseBody which holds the logic for ZIP generation.
        ResponseEntity<StreamingResponseBody> monthlyReportResponse;
        try {

            Set<Lead> leadList = reportService.getLeads(startDate, endDate);
            if (leadList.isEmpty()) {
                // --- Send Email WITHOUT Attachment ---
                MimeMessage message = mailSender.createMimeMessage();
                // Use 'false' for single-part message (no attachment)
                MimeMessageHelper messageHelper = new MimeMessageHelper(
                        message,
                        false,
                        StandardCharsets.UTF_8.toString());

                messageHelper.setSubject("NO DATA: " + reportName);
                messageHelper.setFrom(AutoEmailScheduler.SENDER_EMAIL);
                messageHelper.setTo(AutoEmailScheduler.RECIPIENT_EMAIL);

                messageHelper.setText(noDataText, false);
                mailSender.send(message);
                LOGGER.log(Level.INFO, "Mail sent successfully without attachment");

            } else {
                monthlyReportResponse = reportService.excelToZipConverter(leadList, startDate, endDate);

                StreamingResponseBody streamingBody = monthlyReportResponse.getBody();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                try {
                    // This call executes the entire ZIP creation and Excel writing process,
                    // capturing the final zipped data into 'baos'.
                    if(streamingBody != null) {
                        streamingBody.writeTo(baos);
                    } else {
                        LOGGER.log(Level.WARNING, "Streaming Body Null - Service :: Email :: AutoEmailScheduler :: sendMonthlyReport() ");
                    }
                } catch (IOException e) {
                    log.error("Failed capturing the final zipped data");
                    LOGGER.log(Level.WARNING, "Service :: Email :: AutoEmailScheduler :: SendMonthlyReport ", e);
                    throw new ReportException(ErrorCode.ERROR_IN_ZIP_FILE_CREATION);
                }

                // Convert the collected bytes into a resource for the email attachment
                ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());

                // --- 3. Send Email WITH Attachment ---
                MimeMessage message = mailSender.createMimeMessage();
                // Use 'true' for multipart (attachments)
                MimeMessageHelper messageHelper = new MimeMessageHelper(
                        message,
                        true,
                        StandardCharsets.UTF_8.toString());

                messageHelper.setSubject(reportName);
                messageHelper.setFrom(AutoEmailScheduler.SENDER_EMAIL);
                messageHelper.setTo(AutoEmailScheduler.RECIPIENT_EMAIL);

                // The controller generates a ZIP file, so use the .zip extension
                messageHelper.addAttachment(reportName + ".zip", resource);
                messageHelper.setText("Please find attached the **" + reportName + ".zip** file containing the monthly CRM report. ", true);

                mailSender.send(message);
                LOGGER.log(Level.INFO, "Mail sent successfully with attachment");
            }
            LOGGER.log(Level.INFO, "Received Template ZIP file");

        } catch (ExcelException e) {
            log.error("Failed to get Template ZIP file");
            LOGGER.log(Level.WARNING, "Service :: Email :: AutoEmailScheduler :: SendMonthlyReport ", e);
            throw new ReportException(ErrorCode.ERROR_IN_ZIP_FILE_CREATION);
        }
    }
}