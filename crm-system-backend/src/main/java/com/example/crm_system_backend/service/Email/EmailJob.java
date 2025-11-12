package com.example.crm_system_backend.service.Email;

import com.example.crm_system_backend.controller.ReportController;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

@Component
public class EmailJob extends QuartzJobBean {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailProperties mailProperties;

    @Autowired
    private ReportController reportController;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();

        String subject = jobDataMap.getString("subject");
        String body = jobDataMap.getString("body");
        String recipientEmail = jobDataMap.getString("email");

        System.out.println("Attempting to send mail to: " + recipientEmail + " with subject: " + subject);

        try {
            sendMail(mailProperties.getUsername(), recipientEmail, subject, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMail(String fromEmail, String toEmail, String subject, String body) throws IOException {

        YearMonth previousMonth = YearMonth.now(ZoneId.systemDefault()).minusMonths(1);
        LocalDate localStartDate = previousMonth.atDay(12);
        Date startDate = Date.from(localStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LocalDate localEndDate = previousMonth.atEndOfMonth();
        Date endDate = Date.from(localEndDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusNanos(1).toInstant());

        String reportName = previousMonth.getMonth().name() + "-" + previousMonth.getYear() + " Monthly Report.xlsx";

        ResponseEntity<byte[]> monthlyReport = reportController.getTemplate(startDate, endDate);

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper messageHelper = new MimeMessageHelper(
                    message,
                    true,   // Enables multipart (for attachments)
                    StandardCharsets.UTF_8.toString());
            messageHelper.setSubject(subject);
            messageHelper.setText("Please find attached " + reportName + " !!", true);
            messageHelper.setFrom(fromEmail);
            messageHelper.setTo(toEmail);
            if (monthlyReport.getBody() != null) {
                ByteArrayResource resource = new ByteArrayResource(monthlyReport.getBody());
                messageHelper.addAttachment(reportName, resource);
                System.out.println("Report attached successfully: " + reportName);
            } else {
                System.err.println("Error: Monthly report data was empty or null.");
                messageHelper.setText("Dear recipient,\n\nCould not generate the monthly report for " + previousMonth.toString() + ".", false);
            }
            mailSender.send(message);

        } catch(MessagingException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
