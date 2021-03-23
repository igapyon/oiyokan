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

import jp.oiyokan.settings.OiyokanNamingUtil;

/**
 * SQL文を構築するための簡易クラスの、Expression を SQLに変換する処理.
 */
public class BasicSqlExprExpander {
    private static final boolean IS_DEBUG_EXPAND_LITERAL = false;

    /**
     * SQL構築のデータ構造.
     */
    private BasicSqlBuildInfo sqlInfo = null;

    /**
     * コンストラクタ.
     * 
     * @param sqlInfo SQL構築のデータ構造.
     */
    public BasicSqlExprExpander(BasicSqlBuildInfo sqlInfo) {
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
            throw new ODataApplicationException("NOT SUPPORTED: Filter Expression: AliasImpl", 500, Locale.ENGLISH);
        } else if (filterExpression instanceof BinaryImpl) {
            expandBinary((BinaryImpl) filterExpression);
            return;
        } else if (filterExpression instanceof EnumerationImpl) {
            throw new ODataApplicationException("NOT SUPPORTED: Filter Expression: EnumerationImpl", 500,
                    Locale.ENGLISH);
        } else if (filterExpression instanceof LambdaRefImpl) {
            throw new ODataApplicationException("NOT SUPPORTED: Filter Expression: LambdaRefImpl", 500, Locale.ENGLISH);
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
            throw new ODataApplicationException("NOT SUPPORTED: Filter Expression: TypeLiteralImpl", 500,
                    Locale.ENGLISH);
        } else if (filterExpression instanceof UnaryImpl) {
            UnaryImpl impl = (UnaryImpl) filterExpression;
            expandUnary(impl);
            return;
        }

        System.err.println("UNEXPECTED: Expression が処理できない:" + filterExpression.toString() + ": "
                + filterExpression.getClass().getName() + "," + filterExpression.toString() + "]");
        throw new ODataApplicationException("UNEXPECTED: 処理できない Expression: " + filterExpression.toString(), 500,
                Locale.ENGLISH);
    }

    ///////////////////////////////////////////////////////////////
    // 内部の実際処理.

    private void expandBinary(BinaryImpl impl) throws ODataApplicationException {
        BinaryOperatorKind opKind = impl.getOperator();
        if (opKind == BinaryOperatorKind.HAS) {
            // HAS
            throw new ODataApplicationException("NOT SUPPORTED: BinaryOperatorKind:" + opKind, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.IN) {
            // IN
            throw new ODataApplicationException("NOT SUPPORTED: BinaryOperatorKind:" + opKind, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.MUL) {
            // MUL
            throw new ODataApplicationException("NOT SUPPORTED: BinaryOperatorKind:" + opKind, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.DIV) {
            // DIV
            throw new ODataApplicationException("NOT SUPPORTED: BinaryOperatorKind:" + opKind, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.MOD) {
            // MOD
            throw new ODataApplicationException("NOT SUPPORTED: BinaryOperatorKind:" + opKind, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.ADD) {
            // ADD
            throw new ODataApplicationException("NOT SUPPORTED: BinaryOperatorKind:" + opKind, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.SUB) {
            // SUB
            throw new ODataApplicationException("NOT SUPPORTED: BinaryOperatorKind:" + opKind, 500, Locale.ENGLISH);
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
            // TODO FIXME おそらく EQ と同様な特殊処理が必要.

            // NE
            sqlInfo.getSqlBuilder().append("(");
            expand(impl.getLeftOperand());
            sqlInfo.getSqlBuilder().append(" <> ");
            expand(impl.getRightOperand());
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

        System.err.println("UNEXPECTED: Unsupported binary operator:" + opKind + "," + impl.toString());
        throw new ODataApplicationException("UNEXPECTED: Unsupported binary operator:" + opKind + "," + impl.toString(),
                500, Locale.ENGLISH);
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
        if (EdmSByte.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmSByte: " + impl.getText());
            Byte look = Byte.valueOf(impl.getText());
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
            // そのまま連結.
            // 2.0などをDecimalにするデメリットが読みきれず。そのまま連結でDB処理に判断をO委ねる。
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
            // そのまま連結.
            sqlInfo.getSqlBuilder().append(impl.getText());
            return;
        }
        if (EdmDouble.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmDouble: " + impl.getText());
            // そのまま連結.
            sqlInfo.getSqlBuilder().append(impl.getText());
            return;
        }
        if (EdmDate.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmDate: " + impl.getText());
            ZonedDateTime zdt = FromOlingoUtil.parseDateString(impl.getText());
            sqlInfo.getSqlBuilder().append("?");
            Timestamp tstamp = Timestamp.from(zdt.toInstant());
            sqlInfo.getSqlParamList().add(tstamp);
            return;
        }
        if (EdmDateTimeOffset.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmDateTimeOffset: " + impl.getText());
            ZonedDateTime zdt = FromOlingoUtil.parseZonedDateTime(impl.getText());
            sqlInfo.getSqlBuilder().append("?");
            Timestamp tstamp = Timestamp.from(zdt.toInstant());
            sqlInfo.getSqlParamList().add(tstamp);
            return;
        }
        if (EdmTimeOfDay.getInstance() == impl.getType()) {
            if (IS_DEBUG_EXPAND_LITERAL)
                System.err.println("TRACE: EdmTimeOfDay: " + impl.getText());
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

        System.err.println("NOT SUPPORTED: LiteralImpl: Type:" + impl.getType().getName());
        System.err.println("NOT SUPPORTED: LiteralImpl: " + impl.toString());
        System.err.println("NOT SUPPORTED: LiteralImpl: " + impl.getClass().getTypeName());
        throw new ODataApplicationException("NOT SUPPORTED: LiteralImpl: " + impl.getClass().getTypeName(), 500,
                Locale.ENGLISH);
    }

    private void expandMember(MemberImpl impl) throws ODataApplicationException {
        // そのままSQLのメンバーとせず、項目名エスケープを除去.
        sqlInfo.getSqlBuilder().append(BasicSqlBuilder.escapeKakkoFieldName(sqlInfo.getSettingsDatabase(),
                OiyokanNamingUtil.entity2Db(BasicSqlBuilder.unescapeKakkoFieldName(impl.toString()))));
    }

    private void expandMethod(MethodImpl impl) throws ODataApplicationException {
        // CONTAINS
        if (impl.getMethod() == MethodKind.CONTAINS) {
            // h2 database の POSITION は 1 オリジンで発見せずが0 なので 1 を減らしています。
            sqlInfo.getSqlBuilder().append("(POSITION(");
            expand(impl.getParameters().get(1));
            sqlInfo.getSqlBuilder().append(",");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(") > 0)");
            return;
        }

        // STARTSWITH
        if (impl.getMethod() == MethodKind.STARTSWITH) {
            // h2 database の POSITION は 1 オリジンで発見せずが0 なので 1 を減らしています。
            sqlInfo.getSqlBuilder().append("(POSITION(");
            expand(impl.getParameters().get(1));
            sqlInfo.getSqlBuilder().append(",");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(") = 1)");
            return;
        }

        // ENDSWITH
        if (impl.getMethod() == MethodKind.ENDSWITH) {
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.ENDSWITH", 500, Locale.ENGLISH);
        }

        // LENGTH
        if (impl.getMethod() == MethodKind.LENGTH) {
            sqlInfo.getSqlBuilder().append("(LENGTH(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append("))");
            return;
        }
        // $top=20&$filter=(length(Description) gt 3)

        // INDEXOF
        if (impl.getMethod() == MethodKind.INDEXOF) {
            // h2 database の POSITION は 1 オリジンで発見せずが0 なので 1 を減らしています。
            sqlInfo.getSqlBuilder().append("(POSITION(");
            expand(impl.getParameters().get(1));
            sqlInfo.getSqlBuilder().append(",");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(") - 1)");
            return;
        }

        // SUBSTRING
        if (impl.getMethod() == MethodKind.SUBSTRING) {
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
            sqlInfo.getSqlBuilder().append("TRIM(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // TODO 未テスト.

        // CONCAT
        if (impl.getMethod() == MethodKind.CONCAT) {
            sqlInfo.getSqlBuilder().append("CONCAT(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(",");
            expand(impl.getParameters().get(1));
            sqlInfo.getSqlBuilder().append(")");
            return;
        }
        // TODO 未テスト.

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
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.FRACTIONALSECONDS", 500, Locale.ENGLISH);
        }

        // TOTALSECONDS
        if (impl.getMethod() == MethodKind.TOTALSECONDS) {
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.TOTALSECONDS", 500, Locale.ENGLISH);
        }

        // DATE
        if (impl.getMethod() == MethodKind.DATE) {
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.DATE", 500, Locale.ENGLISH);
        }

        // TIME
        if (impl.getMethod() == MethodKind.TIME) {
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.TIME", 500, Locale.ENGLISH);
        }

        // TOTALOFFSETMINUTES
        if (impl.getMethod() == MethodKind.TOTALOFFSETMINUTES) {
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.TOTALOFFSETMINUTES", 500, Locale.ENGLISH);
        }

        // MINDATETIME
        if (impl.getMethod() == MethodKind.MINDATETIME) {
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.MINDATETIME", 500, Locale.ENGLISH);
        }

        // MAXDATETIME
        if (impl.getMethod() == MethodKind.MAXDATETIME) {
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.MAXDATETIME", 500, Locale.ENGLISH);
        }

        // NOW
        if (impl.getMethod() == MethodKind.NOW) {
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.NOW", 500, Locale.ENGLISH);
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
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.GEODISTANCE", 500, Locale.ENGLISH);
        }

        // GEOLENGTH
        if (impl.getMethod() == MethodKind.GEOLENGTH) {
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.GEOLENGTH", 500, Locale.ENGLISH);
        }

        // GEOINTERSECTS
        if (impl.getMethod() == MethodKind.GEOINTERSECTS) {
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.GEOINTERSECTS", 500, Locale.ENGLISH);
        }

        // CAST
        if (impl.getMethod() == MethodKind.CAST) {
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.CAST", 500, Locale.ENGLISH);
        }

        // ISOF
        if (impl.getMethod() == MethodKind.ISOF) {
            throw new ODataApplicationException("NOT SUPPORTED: MethodKind.ISOF", 500, Locale.ENGLISH);
        }

        // SUBSTRINGOF
        if (impl.getMethod() == MethodKind.SUBSTRINGOF) {
            sqlInfo.getSqlBuilder().append("(POSITION(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(",");
            expand(impl.getParameters().get(1));
            sqlInfo.getSqlBuilder().append(") > 0)");
            return;
        }

        System.err.println("UNEXPECTED : NOT SUPPORTED MethodKind:" + impl.getMethod() + "," + impl.toString());
        throw new ODataApplicationException(
                "UNEXPECTED : NOT SUPPORTED MethodKind:" + impl.getMethod() + "," + impl.toString(), 500,
                Locale.ENGLISH);
    }

    private void expandUnary(UnaryImpl impl) throws ODataApplicationException {
        if (impl.getOperator() == UnaryOperatorKind.NOT) {
            sqlInfo.getSqlBuilder().append("(NOT (");
            expand(impl.getOperand());
            sqlInfo.getSqlBuilder().append("))");
            return;
        } else if (impl.getOperator() == UnaryOperatorKind.MINUS) {
            sqlInfo.getSqlBuilder().append("(-(");
            expand(impl.getOperand());
            sqlInfo.getSqlBuilder().append("))");
            return;
        }

        System.err.println("UNEXPECTED: Unsupported UnaryOperatorKind:" + impl.getOperator() + "," + impl.toString());
        throw new ODataApplicationException(
                "UNEXPECTED: Unsupported UnaryOperatorKind:" + impl.getOperator() + "," + impl.toString(), 500,
                Locale.ENGLISH);
    }
}
