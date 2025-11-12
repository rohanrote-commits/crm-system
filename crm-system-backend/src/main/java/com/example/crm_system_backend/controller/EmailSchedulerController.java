package com.example.crm_system_backend.controller;

import com.example.crm_system_backend.dto.EmailRequest;
import com.example.crm_system_backend.dto.EmailResponse;
import com.example.crm_system_backend.service.Email.EmailJob;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@RestController
public class EmailSchedulerController {

    @Autowired
    private Scheduler scheduler;

    @PostMapping("/schedule/email")
    public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest emailRequest) {
        try {
            ZonedDateTime dateTime = ZonedDateTime.now();
            JobDetail jobDetail = buildJobDetail(emailRequest);
            Trigger trigger = buildTrigger(jobDetail, dateTime);

            scheduler.scheduleJob(jobDetail, trigger);

            EmailResponse emailResponse = new EmailResponse(true,
                    jobDetail.getKey().getName(), jobDetail.getKey().getGroup(),
                    "Email scheduled successfully!");

            return ResponseEntity.ok(emailResponse);

        } catch(SchedulerException se) {
            log.error("Error while scheduling email", se);
            EmailResponse emailResponse = new EmailResponse(false, "Error while scheduling email.Please try again later!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emailResponse);
        }
    }

    private JobDetail buildJobDetail(EmailRequest scheduleEmailRequest) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("email", scheduleEmailRequest.getEmail());
        jobDataMap.put("subject", scheduleEmailRequest.getSubject());
        jobDataMap.put("body", scheduleEmailRequest.getBody());

        // Tells the builder which specific class of code needs to be executed when the job runs
        return JobBuilder.newJob(EmailJob.class)
                // This assigns a unique name and a group to the job.
                // The Scheduler uses this identity to uniquely locate and manage this specific task.
                // It gives the job a completely random, unique ID and
                // places it into the general "email-jobs" category or group.
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                // This adds a simple, human-readable label to the JobDetail.
                .withDescription("Send email job")
                // This attaches the JobDataMap to the JobDetail.
                .usingJobData(jobDataMap)
                // a crucial configuration setting
                // It instructs the Quartz Scheduler to save the JobDetail permanently,
                // even if there is currently no Trigger(schedule) associated with it.
                .storeDurably()
                // It tells the JobBuilder to complete the assembly and return the finished, fully configured JobDetail object.
                // This object is now ready to be passed to the Scheduler.
                // complete the assembly :
                // 1. validates that all necessary information is provided
                // 2. collects all the configurations defined in the preceding steps
                // 3. constructs the final $\text{JobDetail}$ object
                // 4. returns the fully assembled JobDetail object, which is now ready to be passed to the Scheduler
                .build();
    }

//    private Trigger immediateTrigger(JobDetail jobDetail, ZonedDateTime dateTime) {
//        return TriggerBuilder.newTrigger()
//                .forJob(jobDetail)
//                .withIdentity(jobDetail.getKey().getName(), "Immediate Trigger")
//                .withDescription("send trigger immediately")
//                .startNow()
//                .withSchedule(SimpleScheduleBuilder.simpleSchedule())
//                .build();
//    }

    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt) {

        // 0 seconds, 0 minutes, 9 hours, 1st of every month, *: all months, ?: any day
        String monthlyCronExpression = "0 51 18 12 * ?";

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "monthly-email-triggers")
                .withDescription("send email trigger")
                .startNow()
                // withMisfireHandlingInstructionFireAndProceed() - tells the Scheduler: "If you missed this schedule time,
                // run the job immediately once you are back online, and then continue with the original schedule."
                .withSchedule(CronScheduleBuilder.cronSchedule(monthlyCronExpression)
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }
}