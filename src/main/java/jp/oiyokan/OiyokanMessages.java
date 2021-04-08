/*
 * Copyright 2021 Toshiki Iga
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.oiyokan;

/**
 * Messages of Oiyokan.
 */
public class OiyokanMessages {
    public static final String M001 = "[M001] ERROR: Can't decode specified decodec url";
    public static final String M002 = "[M002] UNEXPECTED: Illegal data type in database settings";
    public static final String M003 = "[M003] UNEXPECTED: Fail to load JDBC driver. Check JDBC Driver classname or JDBC Driver is on classpath.";
    public static final String M004 = "[M004] UNEXPECTED: Database error in setup internal database.";
    public static final String M005 = "[M005] ERROR: Fail to connect database. Please wait minutes and retry again.: データベースの接続に失敗しました。しばらく待って再度トライしてください。しばらく経っても改善しない場合はIT部門などに連絡してください。";
    public static final String M006 = "[M006] NOT SUPPORTED: CSDL: JDBC Type";
    public static final String M007 = "[M007] UNEXPECTED: fail to read from CLOB";
    public static final String M008 = "[M008] UNEXPECTED: fail to read from binary";
    public static final String M009 = "[M009] UNEXPECTED: missing impl";
    public static final String M010 = "[M010] NOT SUPPORTED: Parameter Type";
    public static final String M011 = "[M011] NOT SUPPORTED: URI: $apply";
    public static final String M012 = "[M012] NOT SUPPORTED: URI: customQuery";
    public static final String M013 = "[M013] NOT SUPPORTED: URI: deltaToken";
    public static final String M014 = "[M014] NOT SUPPORTED: URI: $expand";
    public static final String M015 = "[M015] UNEXPECTED: An error occurred in SQL that counts the number of search results.";
    public static final String M016 = "[M016] Fail to execute count SQL";
    public static final String M017 = "[M017] Fail to execute SQL";
    public static final String M018 = "[M018] UNEXPECTED: Fail to create ID EntitySet name";
    public static final String M019 = "[M019] UNEXPECTED: Fail to get database meta";
    public static final String M020 = "[M020] NOT SUPPORTED: Database type";
    public static final String M021 = "[M021] NOT SUPPORTED: JDBC Type";
    public static final String M022 = "[M022] UNEXPECTED: Fail to load setting SQL file";
    public static final String M023 = "[M023] UNEXPECTED: Fail to load Oiyokan naming settings";
    public static final String M024 = "[M024] UNEXPECTED: Fail to load Oiyokan settings";
    public static final String M025 = "[M025] UNEXPECTED: Database settings NOT found";
    public static final String M026 = "[M026] UNEXPECTED: Database settings NOT found";
    public static final String M027 = "[M027] UNEXPECTED: Fail to create local table: Oiyokan";
    public static final String M028 = "[M028] UNEXPECTED: Fail to check local table exists: Oiyokan";
    public static final String M029 = "[M029] UNEXPECTED: Fail to execute SQL for local internal table";
    public static final String M030 = "[M030] UNEXPECTED: Fail to execute SQL for local internal table(2)";
    public static final String M031 = "[M031] UNEXPECTED: Fail to execute Dabaase";
    public static final String M032 = "[M032] NOT SUPPORTED: URI: $search";
    public static final String M033 = "[M033] NOT SUPPORTED: unknown UUID object given";
    public static final String M034 = "[M034] ERROR: An unknown field name was specified. The field names are case sensitive. Make sure the Oiyo field name matches the target field name.";
    public static final String M035 = "[M035] SQL timeout at count";
    public static final String M036 = "[M036] SQL timeout at execute";

    ///////////////////
    // Expression

    public static final String M101 = "[M101] NOT SUPPORTED: Filter Expression: AliasImpl";
    public static final String M102 = "[M102] NOT SUPPORTED: Filter Expression: EnumerationImpl";
    public static final String M103 = "[M103] NOT SUPPORTED: Filter Expression: LambdaRefImpl";
    public static final String M104 = "[M104] NOT SUPPORTED: Filter Expression: TypeLiteralImpl";
    public static final String M105 = "[M105] UNEXPECTED: Fail to process Expression";
    public static final String M106 = "[M106] UNEXPECTED: Unsupported binary operator";
    public static final String M107 = "[M107] NOT SUPPORTED: LiteralImpl";
    public static final String M108 = "[M108] NOT SUPPORTED: MethodKind.FRACTIONALSECONDS";
    public static final String M109 = "[M109] NOT SUPPORTED: MethodKind.TOTALSECONDS";
    public static final String M110 = "[M110] NOT SUPPORTED: MethodKind.DATE";
    public static final String M111 = "[M111] NOT SUPPORTED: MethodKind.TIME";
    public static final String M112 = "[M112] NOT SUPPORTED: MethodKind.TOTALOFFSETMINUTES";
    public static final String M113 = "[M113] NOT SUPPORTED: MethodKind.MINDATETIME";
    public static final String M114 = "[M114] NOT SUPPORTED: MethodKind.MAXDATETIME";
    public static final String M115 = "[M115] NOT SUPPORTED: MethodKind.NOW";
    public static final String M116 = "[M116] NOT SUPPORTED: MethodKind.GEODISTANCE";
    public static final String M117 = "[M117] NOT SUPPORTED: MethodKind.GEOLENGTH";
    public static final String M118 = "[M118] NOT SUPPORTED: MethodKind.GEOINTERSECTS";
    public static final String M119 = "[M119] NOT SUPPORTED: MethodKind.CAST";
    public static final String M120 = "[M120] NOT SUPPORTED: MethodKind.ISOF";
    public static final String M121 = "[M121] UNEXPECTED: NOT SUPPORTED MethodKind";
    public static final String M122 = "[M122] UNEXPECTED: Unsupported UnaryOperatorKind";
    // public static final String M123 = "[M123] NOT SUPPORTED:
    // MethodKind.ENDSWITH";
    public static final String M124 = "[M124] NOT SUPPORTED: BinaryOperatorKind.HAS";
    public static final String M125 = "[M125] NOT SUPPORTED: BinaryOperatorKind.IN";
    public static final String M126 = "[M126] NOT SUPPORTED: BinaryOperatorKind.MUL";
    public static final String M127 = "[M127] NOT SUPPORTED: BinaryOperatorKind.DIV";
    public static final String M128 = "[M128] NOT SUPPORTED: BinaryOperatorKind.MOD";
    public static final String M129 = "[M129] NOT SUPPORTED: BinaryOperatorKind.ADD";
    public static final String M130 = "[M130] NOT SUPPORTED: BinaryOperatorKind.SUB";
    public static final String M131 = "[M131] NOT SUPPORTED: UnaryOperatorKind.MINUS";

    /**
     * 手早く未実装マークをつけるためのテンポラリなメッセージ.
     */
    public static final String M999 = "[M999] NOT IMPLEMENTED: Generic NOT implemented message.";
}
