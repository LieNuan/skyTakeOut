package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;

public interface ReportService {
    //统计指定时间区间营业额内的数据
    TurnoverReportVO getTurnoverStatistics(LocalDate beginDate, LocalDate endDate);

    UserReportVO getUserStatistics(LocalDate beginDate, LocalDate endDate);

    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    //统计指定时间区间销量排名前10的商品
    SalesTop10ReportVO getSalesTop10(LocalDate beginDate, LocalDate endDate);

    //导出最近30天的运营数据报表
    void exportBusinessData(HttpServletResponse response);
}
