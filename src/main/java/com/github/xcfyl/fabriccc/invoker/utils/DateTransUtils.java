package com.github.xcfyl.fabriccc.invoker.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 该工具类负责不同Date类型之间的相互转换
 *
 * @author 西城风雨楼
 */
public class DateTransUtils {
    /**
     * 将util包下的date对象转换为LocalDate对象
     *
     * @param date util包下的date对象
     * @return 返回LocalDate对象
     */
    public static LocalDate utilDateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 将一个字符串，按照某种格式转换为Date对象
     *
     * @param format 转换的格式
     * @param date   date对象
     * @return 返回Date对象
     */
    public static Date stringToDate(String format, String date) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.parse(date);
    }

    /**
     * 将LocalDate对象转换为util包下的Date对象
     *
     * @param date LocalDate对象
     * @return 返回util包下的Date对象
     */
    public static Date localDateToUtilDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将Date转LocalDateTime
     *
     * @param date 传入日期
     * @return 返回LocalDateTime形式
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {

        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {

        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
