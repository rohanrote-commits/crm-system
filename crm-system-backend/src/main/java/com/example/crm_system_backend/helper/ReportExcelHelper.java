package com.example.crm_system_backend.helper;

import com.example.crm_system_backend.entity.Lead;
import com.example.crm_system_backend.entity.User;
import com.example.crm_system_backend.repository.ILeadRepository;
import com.example.crm_system_backend.repository.IUserRepo;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class ReportExcelHelper {

    @Autowired
    ILeadRepository leadRepo;

    @Autowired
    IUserRepo userRepo;

    public static final Logger LOGGER = Logger.getLogger(ReportExcelHelper.class.getName());

    /**
     * Contains head style for workbook
     * @param workbook
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

        LOGGER.log(Level.INFO, "headStyle created");
        return headStyle;
    }


    /**
     * Contains header style for workbook
     * @param workbook
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

        LOGGER.log(Level.INFO, "headerStyle created");
        return headerStyle;
    }


    /**
     * Contains data style for workbook
     * @param workbook
     * @return cell style for data cells for all sheets present in workbook
     */
    public CellStyle dataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setWrapText(true);

        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        LOGGER.log(Level.INFO, "dataStyle created");
        return dataStyle;
    }


    /**
     * @param start
     * @param end
     * @return list of all leads registered in time period between start date and end date
     */
    @Transactional
    public List<Lead> getLeadList(Date start, Date end) {
        List<Lead> leadList = new ArrayList<>();
        for (Lead lead : leadRepo.findAll()) {
            Date createdAt = lead.getCreatedAt();
            if ((createdAt.after(start) && createdAt.before(end))) {
                leadList.add(lead);
            }
        }
        LOGGER.log(Level.INFO, "Received all Leads registered from "  + start + " to " + end);
        return leadList;
    }

    /**
     * Required to print name of user in Report Download History data-table on UI side
     * @param email
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
        return name;
    }

}
