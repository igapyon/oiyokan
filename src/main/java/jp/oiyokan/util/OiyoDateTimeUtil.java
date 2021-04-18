package jp.oiyokan.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.oiyokan.OiyokanMessages;

/**
 * Basic DateTime util for Oiyokan.
 */
public class OiyoDateTimeUtil {
    private static final Log log = LogFactory.getLog(OiyoDateTimeUtil.class);

    private static final DateTimeFormatter[] OFPATTERNS_DATETIME = new DateTimeFormatter[] { //
            DateTimeFormatter.ISO_DATE_TIME, //
            DateTimeFormatter.ISO_INSTANT, //
            DateTimeFormatter.ISO_LOCAL_DATE_TIME, //
            DateTimeFormatter.ISO_LOCAL_TIME, //
            DateTimeFormatter.ISO_OFFSET_DATE_TIME, //
            DateTimeFormatter.ISO_OFFSET_TIME, //
            DateTimeFormatter.ISO_TIME, //
            DateTimeFormatter.ISO_ZONED_DATE_TIME, //
            DateTimeFormatter.RFC_1123_DATE_TIME, //
    };

    private static final DateTimeFormatter[] OFPATTERNS_DATE = new DateTimeFormatter[] { //
            DateTimeFormatter.ISO_DATE, //
            DateTimeFormatter.ISO_INSTANT, //
            DateTimeFormatter.ISO_LOCAL_DATE, //
            DateTimeFormatter.ISO_OFFSET_DATE, //
            DateTimeFormatter.ISO_ORDINAL_DATE, //
    };
    private static final String[] CLASSICPATTERNS = new String[] { //
            "yyyy-MM-dd'T'HH:mm:ss.SSS", //
    };

    /**
     * Parse datetime string.
     * 
     * @param inputDateString input datetime string.
     * @return Parsed DateTime.
     */
    public static ZonedDateTime parseStringDateTime(String inputDateString) {
        String modifiedInputDateString = inputDateString;

        // T のある状態に更新.
        if (modifiedInputDateString.length() > 10 && modifiedInputDateString.charAt(10) == ' ') {
            // T を埋め込み.
            modifiedInputDateString = inputDateString.substring(0, 10) + "T" + inputDateString.substring(11);
        }

        // パターン引き当て.
        for (DateTimeFormatter ofpattern : OFPATTERNS_DATETIME) {
            try {
                return ZonedDateTime.parse(modifiedInputDateString, ofpattern);
            } catch (DateTimeParseException e) {
            }
        }

        for (DateTimeFormatter pattern : OFPATTERNS_DATE) {
            try {
                // 日付は、UTCで00:00:00 の値を戻す.
                final LocalDate ld = LocalDate.parse(modifiedInputDateString, pattern);
                return ZonedDateTime.of(ld.getYear(), ld.getMonth().getValue(), ld.getDayOfMonth(), 0, 0, 0, 0,
                        ZoneId.of("UTC"));
            } catch (DateTimeParseException e) {
            }
        }

        try {
            // デフォルト挙動もだめ押しでトライ.
            return ZonedDateTime.parse(modifiedInputDateString);
        } catch (DateTimeParseException e) {
        }

        try {
            // 旧式のパースにも挑戦
            for (String pattern : CLASSICPATTERNS) {
                // 最後の手段として、旧式APIでのパース.
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                java.util.Date look = sdf.parse(modifiedInputDateString);
                return date2ZonedDateTime(look);
            }
        } catch (ParseException e) {
        }

        // [IY7161] Error: Fail to parse DateTime string.
        System.err.println(OiyokanMessages.IY7161 + ": " + inputDateString);
        throw new IllegalArgumentException(OiyokanMessages.IY7161 + ": " + inputDateString);
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
