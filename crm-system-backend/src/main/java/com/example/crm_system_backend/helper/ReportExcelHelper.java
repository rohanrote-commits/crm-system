package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.constants.ErrorCode;
import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.entity.downloadReport;
import com.example.crm_system_backend.exception.ErrorRecordException;
import com.example.crm_system_backend.exception.ReportException;
import com.example.crm_system_backend.repository.DownloadReportHistoryRepo;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
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
        List<Lead> leadList = new ArrayList<>();
        for(Lead lead : leadRepo.findAll()) {
            Date createdAt = lead.getCreatedAt();
            if(createdAt == null) {    // for test case
                continue;
            }
            if(createdAt.compareTo(start) >= 0 && createdAt.compareTo(end) < 0) {
                leadList.add(lead);
            }
        }
        LOGGER.log(Level.FINE, "helper :: ReportExcelHelper :: getLeadList :: Successfully fetched all Leads registered from "  + start + " to " + end);
        return leadList;
    }


    /**
     * Required to get the set of leads according to the role of the user (Master Admin, Admin and Basic)
     * @param start leads registered from start date = start
     * @param end leads registered till end date = end
     * @return Set of leads to be written in Report Template in all sheets
     */
    public Set<Lead> getLeads(Date start, Date end) {

        Date date = new Date();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate startDate = date.toInstant().atZone(zoneId).toLocalDate();
        LocalDate endDate = end.toInstant().atZone(zoneId).toLocalDate();

        LOGGER.log(Level.FINE, "Getting appropriate leads list ");

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

        LOGGER.log(Level.FINE, "Successfully received a list of all leads registered from " + startDate + " to " + endDate);
        return finalLeads;
    }


    /**
     * Filters the downloaded Report's records according to logged-in user's role
     * @param id logged-in user id
     * @param role logged-in user role
     * @param email logged-in user email
     * @return filtered Set of downloaded report's record according to user's role
     */
    public Set<downloadReport> getFilteredDownloadHistory(Long id, String role, String email) {

        Set<User> users = new HashSet<>();
        Set<Long> adminIds = new HashSet<>();
        Set<String> emailList = new HashSet<>();
        Set<downloadReport> filteredRecords = new HashSet<>();
        Set<downloadReport> finalFilteredRecords = new HashSet<>();

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
                emailList.add(user.getEmail());
            }

            for(downloadReport record : historyRepo.findAll()) {
                for(String one_email : emailList) {
                    if(record.getEmail().equals(one_email)) {
                        filteredRecords.add(record);
                    }
                }
            }
            finalFilteredRecords.addAll(filteredRecords);

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
                emailList.add(user.getEmail());
            }

            for(downloadReport record : historyRepo.findAll()) {
                for(String one_email : emailList) {
                    if(record.getEmail().equals(one_email)) {
                        filteredRecords.add(record);
                    }
                }
            }
            finalFilteredRecords.addAll(filteredRecords);

        } else if(role.equalsIgnoreCase("USER")) {
            for(downloadReport record : historyRepo.findAll()) {
                if(record.getEmail().equals(email)) {
                    filteredRecords.add(record);
                }
            }
            finalFilteredRecords.addAll(filteredRecords);
        }
        if(!finalFilteredRecords.isEmpty()) {
            LOGGER.log(Level.FINE, "Successfully generated list of leads according to logged-in user's role");
            return finalFilteredRecords;

        } else {
            LOGGER.log(Level.FINE, "helper :: ReportExcelHelper :: Could not generate list of leads according to logged-in user's role, no leads registered");
            throw new ReportException(ErrorCode.EMPTY_LEAD_LIST);
        }
    }


    /**
     * Required to print name of user in Report Download History data-table on UI side
     * @param email email is unique property here
     * @return a String containing name of user
     */
    public String getName(String email) {
        String name = null;
        for(User user : userRepo.findAll()) {
            if(user.getEmail().equals(email)) {
                String first_name = userRepo.findUserFirstNameByEmail(email);
                String last_name = userRepo.findUserLastNameByEmail(email);
                name = first_name + " " + last_name;
            }
        }
        if(name == null) {
            LOGGER.log(Level.FINE, "helper :: ReportExcelHelper :: getName :: Email not found");
        }
        LOGGER.log(Level.FINE, "helper :: ReportExcelHelper :: getName :: Successfully fetched User name using email");
        return name;
    }
}
