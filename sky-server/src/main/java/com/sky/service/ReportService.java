package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {
    //统计指定时间区间营业额内的数据
    TurnoverReportVO getTurnoverStatistics(LocalDate beginDate, LocalDate endDate);

    UserReportVO getUserStatistics(LocalDate beginDate, LocalDate endDate);
}
