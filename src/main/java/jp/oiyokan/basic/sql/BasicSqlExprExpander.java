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
package jp.oiyokan.basic.sql;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Locale;

import org.apache.olingo.commons.core.edm.primitivetype.EdmBoolean;
import org.apache.olingo.commons.core.edm.primitivetype.EdmByte;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDate;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDecimal;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDouble;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt16;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt32;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt64;
import org.apache.olingo.commons.core.edm.primitivetype.EdmSByte;
import org.apache.olingo.commons.core.edm.primitivetype.EdmSingle;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmTimeOfDay;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;
import org.apache.olingo.server.core.uri.queryoption.expression.AliasImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.BinaryImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.EnumerationImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.LambdaRefImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.LiteralImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.MemberImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.MethodImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.TypeLiteralImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.UnaryImpl;

import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.basic.BasicJdbcUtil;
import jp.oiyokan.fromolingo.FromApacheOlingoUtil;
import jp.oiyokan.settings.OiyokanNamingUtil;

/**
 * SQL文を構築するための簡易クラスの、Expression を SQLに変換する処理.
 */
public class BasicSqlExprExpander {
    private static final boolean IS_DEBUG_EXPAND_LITERAL = false;

    /**
     * SQL構築のデータ構造.
     */
    private BasicSqlInfo sqlInfo = null;

    /**
     * コンストラクタ.
     * 
     * @param sqlInfo SQL構築のデータ構造.
     */
    public BasicSqlExprExpander(BasicSqlInfo sqlInfo) {
        this.sqlInfo = sqlInfo;
    }

    /**
     * フィルタを展開。WHEREになる。
     * 
     * @param filterExpression フィルタ表現.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public void expand(Expression filterExpression) throws ODataApplicationException {
        if (filterExpression instanceof AliasImpl) {
            // [M101] NOT SUPPORTED: Filter Expression: AliasImpl
            System.err.println(OiyokanMessages.M101);
            throw new ODataApplicationException(OiyokanMessages.M101, 500, Locale.ENGLISH);
        } else if (filterExpression instanceof BinaryImpl) {
            expandBinary((BinaryImpl) filterExpression);
            return;
        } else if (filterExpression instanceof EnumerationImpl) {
            // [M102] NOT SUPPORTED: Filter Expression: EnumerationImpl
            System.err.println(OiyokanMessages.M102);
            throw new ODataApplicationException(OiyokanMessages.M102, 500, Locale.ENGLISH);
        } else if (filterExpression instanceof LambdaRefImpl) {
            // [M103] NOT SUPPORTED: Filter Expression: LambdaRefImpl
            System.err.println(OiyokanMessages.M103);
            throw new ODataApplicationException(OiyokanMessages.M103, 500, Locale.ENGLISH);
        } else if (filterExpression instanceof LiteralImpl) {
            expandLiteral((LiteralImpl) filterExpression);
            return;
        } else if (filterExpression instanceof MemberImpl) {
            expandMember((MemberImpl) filterExpression);
            return;
        } else if (filterExpression instanceof MethodImpl) {
            expandMethod((MethodImpl) filterExpression);
            return;
        } else if (filterExpression instanceof TypeLiteralImpl) {
            // [M104] NOT SUPPORTED: Filter Expression: TypeLiteralImpl"
            System.err.println(OiyokanMessages.M104);
            throw new ODataApplicationException(OiyokanMessages.M104, 500, Locale.ENGLISH);
        } else if (filterExpression instanceof UnaryImpl) {
            UnaryImpl impl = (UnaryImpl) filterExpression;
            expandUnary(impl);
            return;
        }

        // [M105] UNEXPECTED: Fail to process Expression
        System.err.println(OiyokanMessages.M105 + ": " + filterExpression.toString() + ": "
                + filterExpression.getClass().getName() + "," + "]");
        throw new ODataApplicationException(OiyokanMessages.M105 + ": " + filterExpression.toString(), 500,
                Locale.ENGLISH);
    }

    ///////////////////////////////////////////////////////////////
    // 内部の実際処理.

    private void expandBinary(BinaryImpl impl) throws ODataApplicationException {
        BinaryOperatorKind opKind = impl.getOperator();
        if (opKind == BinaryOperatorKind.HAS) {
            // [M124] NOT SUPPORTED: BinaryOperatorKind.HAS
            System.err.println(OiyokanMessages.M124 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.M124, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.IN) {
            // [M125] NOT SUPPORTED: BinaryOperatorKind.IN
            System.err.println(OiyokanMessages.M125 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.M125, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.MUL) {
            // [M126] NOT SUPPORTED: BinaryOperatorKind.MUL
            System.err.println(OiyokanMessages.M126 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.M126, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.DIV) {
            // [M127] NOT SUPPORTED: BinaryOperatorKind.DIV
            System.err.println(OiyokanMessages.M127 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.M127, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.MOD) {
            // [M128] NOT SUPPORTED: BinaryOperatorKind.MOD
            System.err.println(OiyokanMessages.M128 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.M128, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.ADD) {
            // [M129] NOT SUPPORTED: BinaryOperatorKind.ADD
            System.err.println(OiyokanMessages.M129 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.M129, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.SUB) {
            // [M130] NOT SUPPORTED: BinaryOperatorKind.SUB
            System.err.println(OiyokanMessages.M130 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.M130, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.GT) {
            // GT
            sqlInfo.getSqlBuilder().append("(");
            expand(impl.getLeftOperand());
            sqlInfo.getSqlBuilder().append(" > ");
            expand(impl.getRightOperand());
            sqlInfo.getSqlBuilder().append(")");
            return;
        } else if (opKind == BinaryOperatorKind.GE) {
            // GE
            sqlInfo.getSqlBuilder().append("(");
            expand(impl.getLeftOperand());
            sqlInfo.getSqlBuilder().append(" >= ");
            expand(impl.getRightOperand());
            sqlInfo.getSqlBuilder().append(")");
            return;
        } else if (opKind == BinaryOperatorKind.LT) {
            // LT
            sqlInfo.getSqlBuilder().append("(");
            expand(impl.getLeftOperand());
            sqlInfo.getSqlBuilder().append(" < ");
            expand(impl.getRightOperand());
            sqlInfo.getSqlBuilder().append(")");
            return;
        } else if (opKind == BinaryOperatorKind.LE) {
            // LE
            sqlInfo.getSqlBuilder().append("(");
            expand(impl.getLeftOperand());
            sqlInfo.getSqlBuilder().append(" <= ");
            expand(impl.getRightOperand());
            sqlInfo.getSqlBuilder().append(")");
            return;
        } else if (opKind == BinaryOperatorKind.EQ) {
            // EQ
            sqlInfo.getSqlBuilder().append("(");
            if (impl.getRightOperand() instanceof LiteralImpl //
                    && null == ((LiteralImpl) impl.getRightOperand()).getType()) {
                expand(impl.getLeftOperand());
                // 特殊処理 : 右辺が Literal かつ nullの場合は IS NULL 展開する。こうしないと h2 database は NULL検索できない.
                // また、リテラルに null が指定されている場合に、LiteralImpl の getType() 自体が null で渡ってくる。
                sqlInfo.getSqlBuilder().append(" IS NULL)");
                return;
            } else if (impl.getLeftOperand() instanceof LiteralImpl //
                    && null == ((LiteralImpl) impl.getLeftOperand()).getType()) {
                expand(impl.getRightOperand());
                // 特殊処理 : 左辺が Literal かつ nullの場合は IS NULL 展開する。こうしないと h2 database は NULL検索できない.
                // また、リテラルに null が指定されている場合に、LiteralImpl の getType() 自体が null で渡ってくる。
                sqlInfo.getSqlBuilder().append(" IS NULL)");
                return;
            } else {
                expand(impl.getLeftOperand());
                sqlInfo.getSqlBuilder().append(" = ");
                expand(impl.getRightOperand());
                sqlInfo.getSqlBuilder().append(")");
                return;
            }
        } else if (opKind == BinaryOperatorKind.NE) {
            // NE
            sqlInfo.getSqlBuilder().append("(");
            if (impl.getRightOperand() instanceof LiteralImpl //
                    && null == ((LiteralImpl) impl.getRightOperand()).getType()) {
                expand(impl.getLeftOperand());
                // 特殊処理 : 右辺が Literal かつ nullの場合は IS NOT NULL 展開する。こうしないと h2 database は
                // NULL検索できない.
                // また、リテラルに null が指定されている場合に、LiteralImpl の getType() 自体が null で渡ってくる。
                sqlInfo.getSqlBuilder().append(" IS NOT NULL");
                // なお、 「null ne 項目」のように左辺に NULL を記述する IS NOT NULL は Olingoにて指定不可。
            } else {
                expand(impl.getLeftOperand());
                sqlInfo.getSqlBuilder().append(" <> ");
                expand(impl.getRightOperand());
            }
            sqlInfo.getSqlBuilder().append(")");
            return;
        } else if (opKind == BinaryOperatorKind.AND) {
            // AND
            sqlInfo.getSqlBuilder().append("(");
            expand(impl.getLeftOperand());
            sqlInfo.getSqlBuilder().append(" AND ");
            expand(impl.getRightOperand());
            sqlInfo.getSqlBuilder().append(")");
            return;
        } else if (opKind == BinaryOperatorKind.OR) {
            // OR
            sqlInfo.getSqlBuilder().append("(");
            expand(impl.getLeftOperand());
            sqlInfo.getSqlBuilder().append(" OR ");
            expand(impl.getRightOperand());
            sqlInfo.getSqlBuilder().append(")");
            return;
        }

        // [M106] UNEXPECTED: Unsupported binary operator
        System.err.println(OiyokanMessages.M106 + ": " + opKind + "," + impl.toString());
        throw new ODataApplicationException(OiyokanMessages.M106 + ": " + opKind, 500, Locale.ENGLISH);
    }

    /**
     * リテラルを展開。必要に応じてパラメータ化。
     * 
     * @param impl リテラル
     * @throws ODataApplicationException Odataアプリ例外が発生した場合.
     */
    private void expandLiteral(LiteralImpl impl) throws ODataApplicationException {
        if (null == impl.getType()) {
            // リテラルに null が指定されている場合に、LiteralImpl の getType() 自体が null で渡ってくる。
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: null: (" + impl.getText() + ")");
            sqlInfo.getSqlBuilder().append("null");
            return;
        }
        // TODO FIXME この記述を BasicJdbcUtil.buildLiteralOrPlaceholderで共通化すること。
        if (EdmSByte.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmSByte: " + impl.getText());
            Byte look = Byte.valueOf(impl.getText());
            sqlInfo.getSqlBuilder().append("?");
            sqlInfo.getSqlParamList().add(look);
            return;
        }
        if (EdmByte.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmByte: " + impl.getText());
            // 符号なしByteはJavaには該当する型がないので Shortで代用.
            Short look = Short.valueOf(impl.getText());
            sqlInfo.getSqlBuilder().append("?");
            sqlInfo.getSqlParamList().add(look);
            return;
        }
        if (EdmInt16.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmInt16: " + impl.getText());
            Short look = Short.valueOf(impl.getText());
            sqlInfo.getSqlBuilder().append("?");
            sqlInfo.getSqlParamList().add(look);
            return;
        }
        if (EdmInt32.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmInt32: " + impl.getText());
            Integer look = Integer.valueOf(impl.getText());
            sqlInfo.getSqlBuilder().append("?");
            sqlInfo.getSqlParamList().add(look);
            return;
        }
        if (EdmInt64.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmInt64: " + impl.getText());
            Long look = Long.valueOf(impl.getText());
            sqlInfo.getSqlBuilder().append("?");
            sqlInfo.getSqlParamList().add(look);
            return;
        }
        if (EdmDecimal.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmDecimal: " + impl.getText());
            // 小数点付きの数値はパラメータとしては処理せずにそのまま文字列として連結.
            sqlInfo.getSqlBuilder().append(impl.getText());
            return;
        }
        if (EdmBoolean.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmBoolean: " + impl.getText());
            sqlInfo.getSqlBuilder().append("?");
            sqlInfo.getSqlParamList().add(Boolean.valueOf("true".equalsIgnoreCase(impl.getText())));
            return;
        }
        if (EdmSingle.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmSingle: " + impl.getText());
            // 小数点付きの数値はパラメータとしては処理せずにそのまま文字列として連結.
            sqlInfo.getSqlBuilder().append(impl.getText());
            return;
        }
        if (EdmDouble.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmDouble: " + impl.getText());
            // 小数点付きの数値はパラメータとしては処理せずにそのまま文字列として連結.
            sqlInfo.getSqlBuilder().append(impl.getText());
            return;
        }
        if (EdmDate.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmDate: " + impl.getText());
            ZonedDateTime zdt = FromApacheOlingoUtil.parseDateString(impl.getText());
            sqlInfo.getSqlBuilder().append("?");
            Timestamp tstamp = Timestamp.from(zdt.toInstant());
            sqlInfo.getSqlParamList().add(tstamp);
            return;
        }
        if (EdmDateTimeOffset.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmDateTimeOffset: " + impl.getText());
            ZonedDateTime zdt = FromApacheOlingoUtil.parseZonedDateTime(impl.getText());
            sqlInfo.getSqlBuilder().append("?");
            Timestamp tstamp = Timestamp.from(zdt.toInstant());
            sqlInfo.getSqlParamList().add(tstamp);
            return;
        }
        if (EdmTimeOfDay.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmTimeOfDay: " + impl.getText());
            // TODO FIXME NOT IMPLEMENTED
        }
        if (EdmString.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmString: " + impl.getText());
            String value = impl.getText();
            if (value.startsWith("'") && value.endsWith("'")) {
                // 文字列リテラルについては前後のクオートを除去して記憶.
                value = value.substring(1, value.length() - 1);
            }
            // 文字列リテラルとしてパラメータ化クエリで扱う.
            sqlInfo.getSqlBuilder().append("?");
            sqlInfo.getSqlParamList().add(value);
            return;
        }

        // [M107] NOT SUPPORTED: LiteralImpl
        System.err.println(OiyokanMessages.M107 + ": " + impl.getType());
        throw new ODataApplicationException(OiyokanMessages.M107 + ": " + impl.getType(), 500, Locale.ENGLISH);
    }

    private void expandMember(MemberImpl impl) throws ODataApplicationException {
        // そのままSQLのメンバーとせず、項目名エスケープを除去.
        sqlInfo.getSqlBuilder().append(BasicJdbcUtil.escapeKakkoFieldName(sqlInfo,
                OiyokanNamingUtil.entity2Db(BasicJdbcUtil.unescapeKakkoFieldName(impl.toString()))));
    }

    /**
     * method を expand.
     * 
     * @param impl MethodImpl
     * @throws ODataApplicationException ODataアプリ例外が発生.
     */
    private void expandMethod(MethodImpl impl) throws ODataApplicationException {
        // CONTAINS
        if (impl.getMethod() == MethodKind.CONTAINS) {
            // h2 database の POSITION/INSTR は 1 オリジンで発見せずが0 なので 1 を減らしています。
            switch (sqlInfo.getEntitySet().getDatabaseType()) {
            default:
                sqlInfo.getSqlBuilder().append("(INSTR(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(") > 0)");
                return;
            case postgres:
                sqlInfo.getSqlBuilder().append("(STRPOS(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(") > 0)");
                return;
            case MSSQL2008:
                sqlInfo.getSqlBuilder().append("(CHARINDEX(");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(") > 0)");
                return;
            }
        }

        // STARTSWITH
        if (impl.getMethod() == MethodKind.STARTSWITH) {
            switch (sqlInfo.getEntitySet().getDatabaseType()) {
            default:
                // h2 database の POSITION/INSTR は 1 オリジンで発見せずが0 なので 1 を減らしています。
                sqlInfo.getSqlBuilder().append("(INSTR(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(") = 1)");
                return;
            case postgres:
                sqlInfo.getSqlBuilder().append("(STRPOS(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(") = 1)");
                return;
            case MSSQL2008:
                sqlInfo.getSqlBuilder().append("(CHARINDEX(");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(") = 1)");
                return;
            }
        }

        // ENDSWITH
        if (impl.getMethod() == MethodKind.ENDSWITH) {
            switch (sqlInfo.getEntitySet().getDatabaseType()) {
            case postgres:
            default:
                sqlInfo.getSqlBuilder().append("(RIGHT(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",LENGTH(");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(")) = ");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(")");
                return;
            case MSSQL2008:
                sqlInfo.getSqlBuilder().append("(RIGHT(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",LEN(");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(")) = ");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(")");
                return;
            case ORACLE:
                sqlInfo.getSqlBuilder().append("(SUBSTR(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",-LENGTH(");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(")) = ");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(")");
                return;
            }
        }

        // LENGTH
        if (impl.getMethod() == MethodKind.LENGTH) {
            switch (sqlInfo.getEntitySet().getDatabaseType()) {
            case postgres:
            default:
                sqlInfo.getSqlBuilder().append("(LENGTH(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append("))");
                return;
            case MSSQL2008:
                sqlInfo.getSqlBuilder().append("(LEN(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append("))");
                return;
            }
        }
        // $top=20&$filter=(length(Description) gt 3)

        // INDEXOF
        if (impl.getMethod() == MethodKind.INDEXOF) {
            switch (sqlInfo.getEntitySet().getDatabaseType()) {
            default:
                // h2 database の POSITION/INSTR は 1 オリジンで発見せずが0 なので 1 を減らしています。
                // postgresにINSTRがあるか確認
                sqlInfo.getSqlBuilder().append("(INSTR(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(") - 1)");
                return;
            case postgres:
                sqlInfo.getSqlBuilder().append("(STRPOS(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(") - 1)");
                return;
            case MSSQL2008:
                sqlInfo.getSqlBuilder().append("(CHARINDEX(");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(") - 1)");
                return;
            }
        }

        // SUBSTRING
        if (impl.getMethod() == MethodKind.SUBSTRING) {
            switch (sqlInfo.getEntitySet().getDatabaseType()) {
            default:
                sqlInfo.getSqlBuilder().append("(SUBSTRING(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(1));
                if (impl.getParameters().size() > 1) {
                    sqlInfo.getSqlBuilder().append(",");
                    expand(impl.getParameters().get(2));
                }
                sqlInfo.getSqlBuilder().append("))");
                return;
            case ORACLE:
                sqlInfo.getSqlBuilder().append("(SUBSTR(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(1));
                if (impl.getParameters().size() > 1) {
                    sqlInfo.getSqlBuilder().append(",");
                    expand(impl.getParameters().get(2));
                }
                sqlInfo.getSqlBuilder().append("))");
                return;
            }
        }
        // $top=20&$filter=(substring(Description,1,2) eq '増殖')

        // TOLOWER
        if (impl.getMethod() == MethodKind.TOLOWER) {
            sqlInfo.getSqlBuilder().append("LOWER(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // チェックのパターン.
        // $top=20&$filter=(substringof(%27poptablet5%27,tolower(Name)))

        // TOUPPER
        if (impl.getMethod() == MethodKind.TOUPPER) {
            sqlInfo.getSqlBuilder().append("UPPER(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // チェックのパターン.
        // $top=20&$filter=(substringof(%27POPTABLET5%27,toupper(Name)))

        // TRIM
        if (impl.getMethod() == MethodKind.TRIM) {
            switch (sqlInfo.getEntitySet().getDatabaseType()) {
            case postgres:
            default:
                sqlInfo.getSqlBuilder().append("TRIM(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(")");
                return;
            case MSSQL2008:
                // SQL Server 2008 には TRIM がない
                sqlInfo.getSqlBuilder().append("LTRIM(RTRIM(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append("))");
                return;
            }
        }

        // CONCAT
        if (impl.getMethod() == MethodKind.CONCAT) {
            switch (sqlInfo.getEntitySet().getDatabaseType()) {
            case postgres:
            default:
                sqlInfo.getSqlBuilder().append("CONCAT(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(")");
                return;
            case MSSQL2008:
                sqlInfo.getSqlBuilder().append("(CAST(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(" AS VARCHAR)");
                sqlInfo.getSqlBuilder().append(" + ");
                sqlInfo.getSqlBuilder().append("CAST(");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(" AS VARCHAR))");
                return;
            }
        }

        // YEAR
        if (impl.getMethod() == MethodKind.YEAR) {
            sqlInfo.getSqlBuilder().append("YEAR(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // TODO 未テスト.

        // MONTH
        if (impl.getMethod() == MethodKind.MONTH) {
            sqlInfo.getSqlBuilder().append("MONTH(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // TODO 未テスト.

        // DAY
        if (impl.getMethod() == MethodKind.DAY) {
            sqlInfo.getSqlBuilder().append("DAY_OF_MONTH(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // TODO 未テスト.

        // HOUR
        if (impl.getMethod() == MethodKind.HOUR) {
            sqlInfo.getSqlBuilder().append("HOUR(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // TODO 未テスト.

        // MINUTE
        if (impl.getMethod() == MethodKind.MINUTE) {
            sqlInfo.getSqlBuilder().append("MINUTE(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // TODO 未テスト.

        // SECOND
        if (impl.getMethod() == MethodKind.SECOND) {
            sqlInfo.getSqlBuilder().append("SECOND(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // TODO 未テスト.

        // FRACTIONALSECONDS
        if (impl.getMethod() == MethodKind.FRACTIONALSECONDS) {
            // [M108] NOT SUPPORTED: MethodKind.FRACTIONALSECONDS
            System.err.println(OiyokanMessages.M108);
            throw new ODataApplicationException(OiyokanMessages.M108, 500, Locale.ENGLISH);
        }

        // TOTALSECONDS
        if (impl.getMethod() == MethodKind.TOTALSECONDS) {
            // [M109] NOT SUPPORTED: MethodKind.TOTALSECONDS
            System.err.println(OiyokanMessages.M109);
            throw new ODataApplicationException(OiyokanMessages.M109, 500, Locale.ENGLISH);
        }

        // DATE
        if (impl.getMethod() == MethodKind.DATE) {
            // [M110] NOT SUPPORTED: MethodKind.DATE
            System.err.println(OiyokanMessages.M110);
            throw new ODataApplicationException(OiyokanMessages.M110, 500, Locale.ENGLISH);
        }

        // TIME
        if (impl.getMethod() == MethodKind.TIME) {
            // [M111] NOT SUPPORTED: MethodKind.TIME
            System.err.println(OiyokanMessages.M111);
            throw new ODataApplicationException(OiyokanMessages.M111, 500, Locale.ENGLISH);
        }

        // TOTALOFFSETMINUTES
        if (impl.getMethod() == MethodKind.TOTALOFFSETMINUTES) {
            // [M112] NOT SUPPORTED: MethodKind.TOTALOFFSETMINUTES
            System.err.println(OiyokanMessages.M112);
            throw new ODataApplicationException(OiyokanMessages.M112, 500, Locale.ENGLISH);
        }

        // MINDATETIME
        if (impl.getMethod() == MethodKind.MINDATETIME) {
            // [M113] NOT SUPPORTED: MethodKind.MINDATETIME
            System.err.println(OiyokanMessages.M113);
            throw new ODataApplicationException(OiyokanMessages.M113, 500, Locale.ENGLISH);
        }

        // MAXDATETIME
        if (impl.getMethod() == MethodKind.MAXDATETIME) {
            // [M114] NOT SUPPORTED: MethodKind.MAXDATETIME
            System.err.println(OiyokanMessages.M114);
            throw new ODataApplicationException(OiyokanMessages.M114, 500, Locale.ENGLISH);
        }

        // NOW
        if (impl.getMethod() == MethodKind.NOW) {
            // [M115] NOT SUPPORTED: MethodKind.NOW
            System.err.println(OiyokanMessages.M115);
            throw new ODataApplicationException(OiyokanMessages.M115, 500, Locale.ENGLISH);
        }

        // ROUND
        if (impl.getMethod() == MethodKind.ROUND) {
            sqlInfo.getSqlBuilder().append("ROUND(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // TODO 未テスト.

        // FLOOR
        if (impl.getMethod() == MethodKind.FLOOR) {
            sqlInfo.getSqlBuilder().append("FLOOR(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // TODO 未テスト.

        // CEILING
        if (impl.getMethod() == MethodKind.CEILING) {
            sqlInfo.getSqlBuilder().append("CEILING(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // TODO 未テスト.

        // GEODISTANCE
        if (impl.getMethod() == MethodKind.GEODISTANCE) {
            // [M116] NOT SUPPORTED: MethodKind.GEODISTANCE
            System.err.println(OiyokanMessages.M116);
            throw new ODataApplicationException(OiyokanMessages.M116, 500, Locale.ENGLISH);
        }

        // GEOLENGTH
        if (impl.getMethod() == MethodKind.GEOLENGTH) {
            // [M117] NOT SUPPORTED: MethodKind.GEOLENGTH
            System.err.println(OiyokanMessages.M117);
            throw new ODataApplicationException(OiyokanMessages.M117, 500, Locale.ENGLISH);
        }

        // GEOINTERSECTS
        if (impl.getMethod() == MethodKind.GEOINTERSECTS) {
            // [M118] NOT SUPPORTED: MethodKind.GEOINTERSECTS
            System.err.println(OiyokanMessages.M118);
            throw new ODataApplicationException(OiyokanMessages.M118, 500, Locale.ENGLISH);
        }

        // CAST
        if (impl.getMethod() == MethodKind.CAST) {
            // [M119] NOT SUPPORTED: MethodKind.CAST
            System.err.println(OiyokanMessages.M119);
            throw new ODataApplicationException(OiyokanMessages.M119, 500, Locale.ENGLISH);
        }

        // ISOF
        if (impl.getMethod() == MethodKind.ISOF) {
            // [M120] NOT SUPPORTED: MethodKind.ISOF
            System.err.println(OiyokanMessages.M120);
            throw new ODataApplicationException(OiyokanMessages.M120, 500, Locale.ENGLISH);
        }

        // SUBSTRINGOF
        if (impl.getMethod() == MethodKind.SUBSTRINGOF) {
            switch (sqlInfo.getEntitySet().getDatabaseType()) {
            default:
                sqlInfo.getSqlBuilder().append("(INSTR(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(") > 0)");
                return;
            case postgres:
                sqlInfo.getSqlBuilder().append("(STRPOS(");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(") > 0)");
                return;
            case MSSQL2008:
                sqlInfo.getSqlBuilder().append("(CHARINDEX(");
                expand(impl.getParameters().get(1));
                sqlInfo.getSqlBuilder().append(",");
                expand(impl.getParameters().get(0));
                sqlInfo.getSqlBuilder().append(") > 0)");
                return;
            }
        }

        // [M121] UNEXPECTED: NOT SUPPORTED MethodKind
        System.err.println(OiyokanMessages.M121 + ": " + impl.getMethod() + "," + impl.toString());
        throw new ODataApplicationException(OiyokanMessages.M121 + ": " + impl.getMethod() + "," + impl.toString(), 500,
                Locale.ENGLISH);
    }

    private void expandUnary(UnaryImpl impl) throws ODataApplicationException {
        if (impl.getOperator() == UnaryOperatorKind.NOT) {
            sqlInfo.getSqlBuilder().append("(NOT (");
            expand(impl.getOperand());
            sqlInfo.getSqlBuilder().append("))");
            return;
        } else if (impl.getOperator() == UnaryOperatorKind.MINUS) {
            // [M131] NOT SUPPORTED: UnaryOperatorKind.MINUS
            System.err.println(OiyokanMessages.M131 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.M131, 500, Locale.ENGLISH);
        }

        // [M122] UNEXPECTED: Unsupported UnaryOperatorKind
        System.err.println(OiyokanMessages.M122 + ": " + impl.getOperator() + "," + impl.toString());
        throw new ODataApplicationException(OiyokanMessages.M122 + ": " + impl.getOperator() + "," + impl.toString(),
                500, Locale.ENGLISH);
    }
}
