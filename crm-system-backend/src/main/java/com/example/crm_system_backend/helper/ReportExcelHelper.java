package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.dto.downloadReportDTO;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.entity.downloadReport;
import com.example.crm_system_backend.exception.ReportException;
import com.example.crm_system_backend.repository.DownloadReportHistoryRepo;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.example.crm_system_backend.constants.Roles.ADMIN;
import static com.example.crm_system_backend.constants.Roles.MASTER_ADMIN;

@Component
public class ReportExcelHelper {

    @Autowired
    ILeadRepository leadRepo;

    @Autowired
    IUserRepo userRepo;

    @Autowired
    DownloadReportHistoryRepo historyRepo;

    public static final Logger LOGGER = Logger.getLogger(ReportExcelHelper.class.getName());


    /**
     * Contains head style for workbook
     * @param workbook for template creation
     * @return cell style for head cells for all sheets present in workbook
     */
    // Styles
    public CellStyle headStyle(Workbook workbook) {
        Font headFont = workbook.createFont();
        headFont.setBold(true);
        headFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle headStyle = workbook.createCellStyle();
        headStyle.setFont(headFont);
        headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headStyle.setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
        headStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headStyle.setAlignment(HorizontalAlignment.LEFT);

        headStyle.setBorderTop(BorderStyle.THIN);
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);

        LOGGER.log(Level.FINE, "helper :: ReportExcelHelper :: headStyle :: headStyle created successfully");
        return headStyle;
    }


    /**
     * Contains header style for workbook
     * @param workbook for template creation
     * @return cell style for header cells for all sheets present in workbook
     */
    public CellStyle headerStyle(Workbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setWrapText(true);

        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        LOGGER.log(Level.FINE, "helper :: ReportExcelHelper :: headerStyle :: headerStyle created successfully ");
        return headerStyle;
    }


    /**
     * Contains data style for workbook
     * @param workbook for template creation
     * @return cell style for data cells for all sheets present in workbook
     */
    public CellStyle dataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);

        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        LOGGER.log(Level.FINE, "helper :: ReportExcelHelper :: dataStyle :: dataStyle created successfully");
        return dataStyle;
    }


    /**
     * @param start start date
     * @param end end date
     * @return list of all leads registered in time period between start date and end date
     */
    public List<Lead> getLeadList(Date start, Date end) {
        LOGGER.log(Level.INFO, "START: CLASS >> ReportExcelHelper >> METHOD >> getLeadList from " + start + " to " + end);
        List<Lead> leadList = new ArrayList<>();
        for(Lead lead : leadRepo.findAll()) {
            Date createdAt = lead.getCreatedAt();
            if(createdAt == null) {    // for test case
                continue;
            }

            // Reason for this condition:
            // Since the monthly report is scheduled to run on the 1st of every month, it generates the report for the complete previous
            // month—starting from the 1st day of the previous month and ending on its last day (28, 29, 30, or 31), i.e., up to the day
            // immediately before the current month begins.
            // So, if I add a lead on 1st December, and try to get November month leads, the leads added on 1st December will not be considered.
            if(createdAt.after(start) && createdAt.before(end)) {
                leadList.add(lead);
            }
        }
        LOGGER.log(Level.INFO, "END: CLASS >> ReportExcelHelper >> METHOD >> getLeadList from " + start + " to " + end);
        return leadList;
    }


    /**
     * Required to get the set of leads according to the role of the user (Master Admin, Admin and Basic)
     * @param start leads registered from start date = start
     * @param end leads registered till end date = end
     * @return Set of leads to be written in Report Template in all sheets
     */
    public Set<Lead> getLeads(Date start, Date end) {

        LOGGER.log(Level.INFO, "START: CLASS >> ReportExcelHelper >> METHOD >> getLeads from " + start + " to " + end);

        List<Lead> leadList = getLeadList(start, end);
        Set<Lead> finalLeads = new HashSet<>(leadList);
        Set<Lead> leadsToAdd = new HashSet<>();

        for (Lead lead : finalLeads) {
            if (lead.getUser().getRole().equals(MASTER_ADMIN) || lead.getUser().getRole().equals(ADMIN)) {
                Long id = lead.getUser().getId();
                List<User> userList = new ArrayList<>();

                for (User user : userRepo.findAll()) {
                    if (user.getRegisteredBy() == id) {
                        userList.add(user);
                    }
                }

                for (Lead lead1 : leadRepo.findAll()) {
                    for (User user : userList) {
                        if (lead1.getUser().getId().equals(user.getId())) {
                            leadsToAdd.add(lead1);
                        }
                    }
                }
            }
        }

        finalLeads.addAll(leadsToAdd);

        LOGGER.log(Level.INFO, "END: CLASS >> ReportExcelHelper >> METHOD >> getLeads from " + start + " to " + end);
        return finalLeads;
    }


    /**
     * Filters the downloaded Report's records according to logged-in user's role
     * @param id logged-in user id
     * @param role logged-in user role
     * @param email logged-in user email
     * @return filtered Set of downloaded report's record according to user's role
     */
    public Set<downloadReportDTO> getFilteredDownloadHistory(Long id, String role, String email) {

        LOGGER.log(Level.INFO, "START: CLASS >> ReportExcelHelper >> METHOD >> getFilteredDownloadHistory for " + email + " ( " + role + " )");

        Set<User> users = new HashSet<>();
        Set<Long> adminIds = new HashSet<>();
        Set<Long> userIdList = new HashSet<>();
        Set<downloadReport> filteredRecords = new HashSet<>();
        Set<downloadReportDTO> finalFilteredRecords = new HashSet<>();

        if(role.equalsIgnoreCase("MASTER_ADMIN")) {
            for(User user : userRepo.findAll()) {
                if(user.getId().equals(id)) {
                    users.add(user);
                }
                if(user.getRegisteredBy() == id) {
                    users.add(user);
                }
            }

            for(User user : users) {
                if(user.getRole().equals(ADMIN)) {
                    adminIds.add(user.getId());
                }
            }

            for(Long adminId : adminIds) {
                for(User user : userRepo.findAll()) {
                    if(user.getRegisteredBy() == adminId) {
                        users.add(user);
                    }
                }
            }

            for(User user : users) {
                userIdList.add(user.getId());
            }

            for(downloadReport record : historyRepo.findAll()) {
                for(Long user_id : userIdList) {
                    if(record.getUserId().equals(user_id)) {
                        filteredRecords.add(record);
                    }
                }
            }
            for(downloadReport rec : filteredRecords) {
                downloadReportDTO dto = classToDto(rec);
                finalFilteredRecords.add(dto);
            }

        } else if(role.equalsIgnoreCase("ADMIN")) {
            for(User user : userRepo.findAll()) {
                if(user.getId().equals(id)) {
                    users.add(user);
                }
                if(user.getRegisteredBy() == id) {
                    users.add(user);
                }
            }

            for(User user : users) {
                userIdList.add(user.getId());
            }

            for(downloadReport record : historyRepo.findAll()) {
                for(Long user_id : userIdList) {
                    if(record.getUserId().equals(user_id)) {
                        filteredRecords.add(record);
                    }
                }
            }
            for(downloadReport rec : filteredRecords) {
                downloadReportDTO dto = classToDto(rec);
                finalFilteredRecords.add(dto);
            }

        } else if(role.equalsIgnoreCase("USER") || role.equalsIgnoreCase("BASIC")) {
            for(downloadReport record : historyRepo.findAll()) {
                Long uid = record.getUserId();
                String user_email = getEmailByUserId(uid);
                if(user_email.equals(email)) {
                    filteredRecords.add(record);
                }
            }
            for(downloadReport rec : filteredRecords) {
                downloadReportDTO dto = classToDto(rec);
                finalFilteredRecords.add(dto);
            }
        }

        if(!finalFilteredRecords.isEmpty()) {
            LOGGER.log(Level.INFO, "INTERMEDIATE: CLASS >> ReportExcelHelper >> METHOD >> " +
                    "getFilteredDownloadHistory for " + email + " ( " + role + " )");
            LOGGER.log(Level.INFO, "END: CLASS >> ReportExcelHelper >> METHOD >> getFilteredDownloadHistory for "
                    + email + " ( " + role + " )");
            return finalFilteredRecords;

        } else {
            LOGGER.log(Level.WARNING, "WARNING: CLASS >> ReportExcelHelper >> METHOD >> " +
                    "getFilteredDownloadHistory for " + email + " ( " + role + " )");
            throw new ReportException(ErrorCode.EMPTY_LEAD_LIST);
        }

    }


    /**
     * Required to print name of user in Report Download History data-table on UI side
     * @param email email is unique property here
     * @return a String containing name of user
     */
    public String getName(String email) {

        LOGGER.log(Level.INFO, "START: CLASS >> ReportExcelHelper >> METHOD >> getName for " + email);

        String name = null;
        for(User user : userRepo.findAll()) {
            if(user.getEmail().equals(email)) {
                String first_name = userRepo.findUserFirstNameByEmail(email);
                String last_name = userRepo.findUserLastNameByEmail(email);
                name = first_name + " " + last_name;
            }
        }
        if(name == null) {
            LOGGER.log(Level.WARNING, "WARNING:  CLASS >> ReportExcelHelper >> METHOD >> getName for " + email + " >> Warning: Email not Found");
        }
        LOGGER.log(Level.INFO, "END: CLASS >> ReportExcelHelper >> METHOD >> getName for " + email);
        return name;
    }


    /**
     * This method fetch returns email by passing userId as input parameter (required in Report Controller)
     * @param userId userId
     * @return email
     */
    public String getEmailByUserId(long userId) {

        LOGGER.log(Level.INFO, "START: CLASS >> ReportExcelHelper >> METHOD >> getEmailByUserId with userId " + userId);
        String email = null;
        for(User user : userRepo.findAll()) {
            if(user.getId().equals(userId)) {
                email = user.getEmail();
            }
        }
        LOGGER.log(Level.INFO, "END: CLASS >> ReportExcelHelper >> METHOD >> getEmailByUserId with userId " + userId);
        return email;
    }

    // Method to convert class to DTO
    public downloadReportDTO classToDto(downloadReport record) {
        downloadReportDTO dto = new downloadReportDTO();
        if(record == null) {
            return null;
        } else {
            Long userid = record.getUserId();
            for(User user : userRepo.findAll()) {
                if(user.getId().equals(userid)) {
                    dto.setUserName(user.getFirstName() + " " + user.getLastName());
                    break;
                }
            }
            dto.setDownloadedAt(record.getDownloadedAt());
            dto.setDateRange(record.getDateRange());
            dto.setStatus(record.getStatus());
        }
        return dto;
    }
}
