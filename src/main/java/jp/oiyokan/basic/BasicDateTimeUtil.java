package jp.oiyokan.basic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class BasicDateTimeUtil {
    // TODO ゾーン指定付きを追加
    private static final String[] PATTERNS = new String[] { "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd" };

    public static ZonedDateTime parseStringDateTime(String inputDateString) {
        for (String pattern : PATTERNS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                java.util.Date look = sdf.parse(inputDateString);
                return date2ZonedDateTime(look);
            } catch (ParseException e) {
            }
        }

        for (String pattern : PATTERNS) {
            try {
                String modifiedInputDateString = inputDateString;
                if (modifiedInputDateString.length() > 10 && modifiedInputDateString.charAt(10) == 'T') {
                    modifiedInputDateString = inputDateString.substring(0, 10) + " " + inputDateString.substring(11);
                }
                if (modifiedInputDateString.endsWith("Z")) {
                    modifiedInputDateString = modifiedInputDateString.substring(0,
                            modifiedInputDateString.length() - 1);
                }

                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                java.util.Date look = sdf.parse(modifiedInputDateString);
                return date2ZonedDateTime(look);
            } catch (ParseException e) {
            }
        }

        throw new IllegalArgumentException("パースできない[" + inputDateString + "]");
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
