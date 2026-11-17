package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate beginDate, LocalDate endDate) {
        List<LocalDate> dateList = getDateList(beginDate, endDate);
        List<Double> doubleList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            Double turnover = orderMapper.sumByMap(begin, end, Orders.COMPLETED);
            turnover = turnover == null ? 0.0 : turnover;
            doubleList.add(turnover);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(doubleList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate beginDate, LocalDate endDate) {
        List<LocalDate> dateList = getDateList(beginDate, endDate);
        //存放每天新增用户
        List<Integer> userList = new ArrayList<>();
        //总用户
        List<Integer> totalList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            userList.add(userMapper.sumUserByMap(begin,end));
            totalList.add(userMapper.sumUserByMap(null,end));
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(userList, ","))
                .totalUserList(StringUtils.join(totalList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate beginDate, LocalDate endDate) {
        List<LocalDate> dateList = getDateList(beginDate, endDate);
        //总订单数
        List<Integer> orderCountList = new ArrayList<>();
        //有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            //总订单
            orderCountList.add(orderMapper.countByMap(begin, end, null));
            //有效订单
            validOrderCountList.add(orderMapper.countByMap(begin, end, Orders.COMPLETED));
        }
        //订单总数
        int totalOrderCount = orderCountList.stream().mapToInt(Integer::intValue).sum();
        //有效订单总数
        int validOrderCount = validOrderCountList.stream().mapToInt(Integer::intValue).sum();
        //订单完成率
        double orderCompletionRate = totalOrderCount == 0 ? 0.0 : validOrderCount * 1.0 / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate beginDate, LocalDate endDate) {
        LocalDateTime begin = LocalDateTime.of(beginDate, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endDate, LocalTime.MAX);
        List<Map<String, Object>> salesTop10 = orderMapper.getSalesTop10(begin, end, Orders.COMPLETED);
        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        for (Map<String, Object> map : salesTop10) {
            nameList.add((String) map.get("name"));
            numberList.add(((Number) map.get("number")).intValue());
        }
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //统计最近30天：昨天往前推29天到昨天
        LocalDate beginDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().minusDays(1);

        InputStream in = getClass().getClassLoader()
                .getResourceAsStream("template/运营数据报表模板.xlsx");
        if (in == null) {
            throw new RuntimeException("未找到运营数据报表模板：template/运营数据报表模板.xlsx");
        }

        try (in; XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);

            //B2：统计时间区间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + beginDate + " 至 " + endDate);

            //概览数据：营业额 C4、订单完成率 E4、新增用户数 G4、有效订单 C5、平均客单价 E5
            BusinessDataVO overview = workspaceService.getBusinessData(
                    LocalDateTime.of(beginDate, LocalTime.MIN),
                    LocalDateTime.of(endDate, LocalTime.MAX));
            Row overviewRow1 = sheet.getRow(3);
            overviewRow1.getCell(2).setCellValue(overview.getTurnover());
            overviewRow1.getCell(4).setCellValue(overview.getOrderCompletionRate());
            overviewRow1.getCell(6).setCellValue(overview.getNewUsers());
            Row overviewRow2 = sheet.getRow(4);
            overviewRow2.getCell(2).setCellValue(overview.getValidOrderCount());
            overviewRow2.getCell(4).setCellValue(overview.getUnitPrice());

            //明细数据：从第8行开始，共30行，B~G列
            for (int i = 0; i < 30; i++) {
                LocalDate date = beginDate.plusDays(i);
                BusinessDataVO day = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                Row row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(day.getTurnover());
                row.getCell(3).setCellValue(day.getValidOrderCount());
                row.getCell(4).setCellValue(day.getOrderCompletionRate());
                row.getCell(5).setCellValue(day.getUnitPrice());
                row.getCell(6).setCellValue(day.getNewUsers());
            }

            //输出到响应流，浏览器直接下载
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("运营数据报表.xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            log.error("导出运营数据报表失败", e);
            throw new RuntimeException("导出运营数据报表失败", e);
        }
    }

    //生成 beginDate 到 endDate 之间的日期列表（包含首尾），beginDate 晚于 endDate 时返回空列表
    private List<LocalDate> getDateList(LocalDate beginDate, LocalDate endDate) {
        List<LocalDate> dateList = new ArrayList<>();
        for (LocalDate date = beginDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dateList.add(date);
        }
        return dateList;
    }
}
