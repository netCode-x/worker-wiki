package com.codebit.api.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SITE_START_DATE = "2026-01-01";

    public static String calculateBuildDays() {
        LocalDate startDate = LocalDate.parse(SITE_START_DATE);
        long days = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        return days + "天";
    }

    public static String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "未知";

        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (days == 0) {
            long hours = ChronoUnit.HOURS.between(dateTime, now);
            if (hours == 0) {
                long minutes = ChronoUnit.MINUTES.between(dateTime, now);
                return minutes + "分钟前";
            }
            return hours + "小时前";
        } else if (days < 7) {
            return days + "天前";
        } else {
            return dateTime.format(DATE_FORMATTER);
        }
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_FORMATTER);
    }

    public static String formatFootprintDate(Integer year, Integer month) {
        if (year == null) return "";
        if (month != null && month >= 1 && month <= 12) {
            return String.format("%d年%d月", year, month);
        }
        return String.format("%d年", year);
    }
}