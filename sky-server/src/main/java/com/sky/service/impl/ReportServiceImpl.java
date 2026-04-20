package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    OrderDetailMapper orderDetailMapper;
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        log.info("统计营业额数据：{}到{}", begin, end);
        //创建日期集合
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.equals( end)){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Double count = orderMapper.getDateStatistics(beginTime, endTime, Orders.COMPLETED);
            if (count == null){
                count = 0.0;
            }
            turnoverList.add(count);
        }

        String dateListString = StringUtils.join(dateList, ",");
        String turnoverListString = StringUtils.join(turnoverList, ",");

        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(dateListString)
                .turnoverList(turnoverListString)
                .build();
        return turnoverReportVO;
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.equals( end)){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer newUserCount = userMapper.getNewUserCount(begin, end) ;
            Integer totalUserCount = userMapper.getTotalCount(begin);
            newUserList.add(newUserCount);
            totalUserList.add(totalUserCount);
        }
        String dateListString = StringUtils.join(dateList, ",");
        String newUserListString = StringUtils.join(newUserList, ",");
        String totalUserListString = StringUtils.join(totalUserList, ",");
        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(dateListString)
                .newUserList(newUserListString)
                .totalUserList(totalUserListString)
                .build();
        return userReportVO;
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end){
        List<LocalDate> dateList = new ArrayList<>();
        while(!begin.equals(end)){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer totalValidOrderCount = 0;
        Double orderCompletionRate = 0.0;
        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = orderMapper.getOrderCount(beginTime, endTime);
            Integer validOrderCount = orderMapper.getValidOrderCount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
            totalOrderCount += orderCount;
            totalValidOrderCount += validOrderCount;
        }
        orderCompletionRate = (double) totalValidOrderCount / totalOrderCount;
        String dateListString = StringUtils.join(dateList, ",");
        String orderCountListString = StringUtils.join(orderCountList, ",");
        String validOrderCountListString = StringUtils.join(validOrderCountList, ",");
        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(dateListString)
                .orderCountList(orderCountListString)
                .validOrderCountList(validOrderCountListString)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
        return orderReportVO;
    }

    /**
     * 销量排名
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end){


        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List< GoodsSalesDTO> salesTop10 = orderDetailMapper.getSalesTop10(beginTime, endTime, Orders.COMPLETED);
        List<String> names = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();
        for (GoodsSalesDTO salesTop : salesTop10){
            names.add(salesTop.getName());
            numbers.add(salesTop.getNumber());
        }
        String nameList = StringUtils.join(names, ",");
        String numberList = StringUtils.join(numbers, ",");
        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
        return salesTop10ReportVO;

    }
}
