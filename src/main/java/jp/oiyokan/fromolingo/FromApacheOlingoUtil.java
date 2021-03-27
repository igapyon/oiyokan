/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package jp.oiyokan.fromolingo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.server.api.ODataApplicationException;

/**
 * Apache Olingo から複写したソースコード。
 */
public class FromApacheOlingoUtil {
    private static final Pattern PATTERN_EdmDateTimeOffset = Pattern
            .compile("(-?\\p{Digit}{4,})-(\\p{Digit}{2})-(\\p{Digit}{2})"
                    + "T(\\p{Digit}{2}):(\\p{Digit}{2})(?::(\\p{Digit}{2})(\\.(\\p{Digit}{0,12}?)0*)?)?"
                    + "(Z|([-+]\\p{Digit}{2}:\\p{Digit}{2}))?");

    /**
     * 与えられた日時をもとに日時に変換。
     * 
     * @param value 日時を表す文字列.
     * @return 日時.
     */
    public static ZonedDateTime parseZonedDateTime(final String value) {
        ZonedDateTime zdt;
        try {
            // ISO-8601 conform pattern
            zdt = ZonedDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            // for backward compatibility - allow patterns that don't specify a time zone
            final Matcher matcher = PATTERN_EdmDateTimeOffset.matcher(value);
            if (matcher.matches() && matcher.group(9) == null) {
                zdt = ZonedDateTime.parse(value + "Z");
            } else {
                throw ex;
            }
        }
        return zdt;
    }

    /**
     * 与えられた日付文字列を共に日時を取得。
     * 
     * @param value 日付文字列。
     * @return 日時。
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static ZonedDateTime parseDateString(final String value) throws ODataApplicationException {
        LocalDate date;
        try {
            date = LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            System.err.println("UNEXPECTED: The literal '" + value + "' has illegal content.: " + ex.toString());
            throw new ODataApplicationException("UNEXPECTED: The literal '" + value + "' has illegal content.", 500,
                    Locale.ENGLISH);
        }
        // inappropriate types, which need to be supported for backward compatibility
        return LocalDateTime.of(date, LocalTime.MIDNIGHT).atZone(ZoneId.systemDefault());
    }
}
