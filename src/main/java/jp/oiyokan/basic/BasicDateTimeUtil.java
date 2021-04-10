package jp.oiyokan.basic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class BasicDateTimeUtil {
    public static ZonedDateTime parseStringDateTime(String inputDateString) {
        try {
            // 2021-04-10 19:12:49.082587
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            java.util.Date look = sdf.parse(inputDateString);
            return date2ZonedDateTime(look);
        } catch (ParseException e) {
            // 1970-01-01T10:12:49Z
            if (inputDateString.length() > 10 && inputDateString.charAt(10) == 'T') {
                String wrk = inputDateString.substring(0, 10) + " " + inputDateString.substring(11);
                if (wrk.endsWith("Z")) {
                    wrk = wrk.substring(0, wrk.length() - 1);
                }
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    java.util.Date look = sdf.parse(wrk);
                    return date2ZonedDateTime(look);
                } catch (ParseException e2) {
                    e2.printStackTrace();
                    throw new IllegalArgumentException(e2);
                }
            }

            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    public static ZonedDateTime date2ZonedDateTime(java.util.Date arg) {
        Instant instant = arg.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return ZonedDateTime.ofInstant(instant, zone);
    }

    public static java.util.Date zonedDateTime2Date(ZonedDateTime arg) {
        Instant instant = arg.toInstant();
        return java.util.Date.from(instant);
    }
}
