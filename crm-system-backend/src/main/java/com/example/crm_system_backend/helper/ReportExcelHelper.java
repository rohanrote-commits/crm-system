package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class ReportExcelHelper {

    @Autowired
    IUserRepo userRepo;

    @Autowired
    ILeadRepository leadRepo;

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

        return headStyle;
    }

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

        return headerStyle;
    }

    public CellStyle dataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);

        return dataStyle;
    }


    public Date convertLocalDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    // Get Lists
    public List<User> getUserList(Date start, Date end) {
        List<User> userList = new ArrayList<>();
        for(User user : userRepo.findAll()) {
            Date date = convertLocalDateTimeToDate(user.getRegisteredOn());
            if(date.after(start) && date.before(end)) {
                userList.add(user);
            }
        }
        return userList;
    }

    public List<Lead> getLeadList(String email) {

        // Get leads of specific user logic
        Optional<User> user = userRepo.getUserByEmail(email);
        if(user.isEmpty()) {
            System.err.println("User with email " + email + " not found");
            return null;
        }
        Optional<List<Lead>> listOfLeads = leadRepo.getLeadsByUser(user);
        if(listOfLeads.isEmpty()) {
            System.err.println("User with email " + email + " has not registered any leads");
            return null;
        }

        return listOfLeads.get();
    }

    public void createDropdownCell(Sheet sheet, int rowIndex, int cellIndex, Set<String> modules) {

        String[] moduleArray = modules.toArray(new String[0]);
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();

        if (String.join(",", moduleArray).length() > 255) {
            System.err.println("Values too long for direct list constraint!");
            return;
        }

        DataValidationConstraint constraint = dvHelper.createExplicitListConstraint(moduleArray);
        CellRangeAddressList addressList = new CellRangeAddressList(rowIndex, rowIndex, cellIndex, cellIndex);
        DataValidation validation = dvHelper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        sheet.addValidationData(validation);
    }

    public Set<String> getSetOfInterestedModules(Long leadId) {
        Optional<Lead> lead = leadRepo.getLeadsById(leadId);
        if(lead.isPresent()) {
            return lead.get().getInterestedModules();
        }
        return null;
    }

}
