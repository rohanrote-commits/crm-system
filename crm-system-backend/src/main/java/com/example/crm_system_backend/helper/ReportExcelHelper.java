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

    public List<Lead> getLeadList(List<Lead> leads, Date start, Date end) {
        List<Lead> leadList = new ArrayList<>();
        for(Lead lead : leads) {
            Date createdAt = lead.getCreatedAt();
            if((createdAt.after(start) && createdAt.before(end))) {
                leadList.add(lead);
            }
        }
        return leadList;
    }

    public List<Lead> getLeadList(Date start, Date end) {
        List<Lead> leadList = new ArrayList<>();
        for(Lead lead : leadRepo.findAll()) {
            Date createdAt = lead.getCreatedAt();
            if((createdAt.after(start) && createdAt.before(end))) {
                leadList.add(lead);
            }
        }
        return leadList;
    }

}
