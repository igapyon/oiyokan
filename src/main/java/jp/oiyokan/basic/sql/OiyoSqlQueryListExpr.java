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

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.common.OiyoSqlInfo;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsProperty;

/**
 * SQL文を構築するための簡易クラスの、Expression を SQLに変換する処理.
 */
public class OiyoSqlQueryListExpr {
    private static final Log log = LogFactory.getLog(OiyoSqlQueryListExpr.class);

    private static final boolean IS_DEBUG_EXPAND_LITERAL = false;

    /**
     * Oiyokan Info.
     */
    private OiyoInfo oiyoInfo;

    /**
     * SQL構築のデータ構造.
     */
    private OiyoSqlInfo sqlInfo = null;

    /**
     * コンストラクタ.
     * 
     * @param sqlInfo SQL構築のデータ構造.
     */
    public OiyoSqlQueryListExpr(OiyoInfo oiyoInfo, OiyoSqlInfo sqlInfo) {
        this.oiyoInfo = oiyoInfo;
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
            log.error(OiyokanMessages.IY4101);
            throw new ODataApplicationException(OiyokanMessages.IY4101, 500, Locale.ENGLISH);
        } else if (filterExpression instanceof BinaryImpl) {
            expandBinary((BinaryImpl) filterExpression);
            return;
        } else if (filterExpression instanceof EnumerationImpl) {
            // [M102] NOT SUPPORTED: Filter Expression: EnumerationImpl
            log.error(OiyokanMessages.IY4102);
            throw new ODataApplicationException(OiyokanMessages.IY4102, 500, Locale.ENGLISH);
        } else if (filterExpression instanceof LambdaRefImpl) {
            // [M103] NOT SUPPORTED: Filter Expression: LambdaRefImpl
            log.error(OiyokanMessages.IY4103);
            throw new ODataApplicationException(OiyokanMessages.IY4103, 500, Locale.ENGLISH);
        } else if (filterExpression instanceof LiteralImpl) {
            expandLiteral((LiteralImpl) filterExpression, null);
            return;
        } else if (filterExpression instanceof MemberImpl) {
            expandMember((MemberImpl) filterExpression);
            return;
        } else if (filterExpression instanceof MethodImpl) {
            expandMethod((MethodImpl) filterExpression);
            return;
        } else if (filterExpression instanceof TypeLiteralImpl) {
            // [M104] NOT SUPPORTED: Filter Expression: TypeLiteralImpl"
            log.error(OiyokanMessages.IY4104);
            throw new ODataApplicationException(OiyokanMessages.IY4104, 500, Locale.ENGLISH);
        } else if (filterExpression instanceof UnaryImpl) {
            UnaryImpl impl = (UnaryImpl) filterExpression;
            expandUnary(impl);
            return;
        }

        // [M105] UNEXPECTED: Fail to process Expression
        log.fatal(OiyokanMessages.IY4151 + ": " + filterExpression.toString() + ": "
                + filterExpression.getClass().getName() + "," + "]");
        throw new ODataApplicationException(OiyokanMessages.IY4151 + ": " + filterExpression.toString(), 500,
                Locale.ENGLISH);
    }

    ///////////////////////////////////////////////////////////////
    // 内部の実際処理.

    private void expandBinary(BinaryImpl impl) throws ODataApplicationException {
        BinaryOperatorKind opKind = impl.getOperator();
        if (opKind == BinaryOperatorKind.HAS) {
            // [M124] NOT SUPPORTED: BinaryOperatorKind.HAS
            log.error(OiyokanMessages.IY4118 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.IY4118, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.IN) {
            // [M125] NOT SUPPORTED: BinaryOperatorKind.IN
            log.error(OiyokanMessages.IY4119 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.IY4119, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.MUL) {
            // [M126] NOT SUPPORTED: BinaryOperatorKind.MUL
            log.error(OiyokanMessages.IY4120 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.IY4120, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.DIV) {
            // [M127] NOT SUPPORTED: BinaryOperatorKind.DIV
            log.error(OiyokanMessages.IY4121 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.IY4121, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.MOD) {
            // [M128] NOT SUPPORTED: BinaryOperatorKind.MOD
            log.error(OiyokanMessages.IY4122 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.IY4122, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.ADD) {
            // [M129] NOT SUPPORTED: BinaryOperatorKind.ADD
            log.error(OiyokanMessages.IY4123 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.IY4123, 500, Locale.ENGLISH);
        } else if (opKind == BinaryOperatorKind.SUB) {
            // [M130] NOT SUPPORTED: BinaryOperatorKind.SUB
            log.error(OiyokanMessages.IY4124 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.IY4124, 500, Locale.ENGLISH);
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
            // IS NULL対応(右辺がNULL)
            if (impl.getRightOperand() instanceof LiteralImpl //
                    && null == ((LiteralImpl) impl.getRightOperand()).getType()) {
                expand(impl.getLeftOperand());
                // 特殊処理 : 右辺が Literal かつ nullの場合は IS NULL 展開する。こうしないと h2 database は NULL検索できない.
                // また、リテラルに null が指定されている場合に、LiteralImpl の getType() 自体が null で渡ってくる。
                sqlInfo.getSqlBuilder().append(" IS NULL)");
                return;
            }
            // IS NULL対応(左辺がNULL)
            if (impl.getLeftOperand() instanceof LiteralImpl //
                    && null == ((LiteralImpl) impl.getLeftOperand()).getType()) {
                expand(impl.getRightOperand());
                // 特殊処理 : 左辺が Literal かつ nullの場合は IS NULL 展開する。こうしないと h2 database は NULL検索できない.
                // また、リテラルに null が指定されている場合に、LiteralImpl の getType() 自体が null で渡ってくる。
                sqlInfo.getSqlBuilder().append(" IS NULL)");
                return;
            }

            // 左辺が MemberImpl で右辺が LiteralImpl
            if (impl.getLeftOperand() instanceof MemberImpl //
                    && impl.getRightOperand() instanceof LiteralImpl) {
                final MemberImpl member = (MemberImpl) impl.getLeftOperand();
                final LiteralImpl literal = (LiteralImpl) impl.getRightOperand();
                OiyoSettingsProperty property = expandMember(member);
                sqlInfo.getSqlBuilder().append(" = ");
                expandLiteral(literal, property);
                sqlInfo.getSqlBuilder().append(")");
                return;
            }

            expand(impl.getLeftOperand());
            sqlInfo.getSqlBuilder().append(" = ");
            expand(impl.getRightOperand());
            sqlInfo.getSqlBuilder().append(")");
            return;
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
        log.fatal(OiyokanMessages.IY4152 + ": " + opKind + "," + impl.toString());
        throw new ODataApplicationException(OiyokanMessages.IY4152 + ": " + opKind, 500, Locale.ENGLISH);
    }

    /**
     * リテラルを展開。必要に応じてパラメータ化。
     * 
     * @param impl リテラル
     * @throws ODataApplicationException Odataアプリ例外が発生した場合.
     */
    private void expandLiteral(LiteralImpl impl, OiyoSettingsProperty property) throws ODataApplicationException {
        if (null == impl.getType()) {
            // リテラルに null が指定されている場合に、LiteralImpl の getType() 自体が null で渡ってくる。
            if (IS_DEBUG_EXPAND_LITERAL)
                log.debug("TRACE: null: (" + impl.getText() + ")");
            sqlInfo.getSqlBuilder().append("null");
            return;
        }

        if (property == null) {
            OiyoCommonJdbcUtil.expandLiteralOrBindParameter(sqlInfo,
                    impl.getType().getFullQualifiedName().getFullQualifiedNameAsString(), null, impl.getText());
        } else {
            System.err.println("TRACE: TODO: member情報付き展開:" + property.getName());
            OiyoCommonJdbcUtil.expandLiteralOrBindParameter(sqlInfo,
                    impl.getType().getFullQualifiedName().getFullQualifiedNameAsString(), property, impl.getText());
        }
    }

    private OiyoSettingsProperty expandMember(MemberImpl impl) throws ODataApplicationException {
        final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, sqlInfo.getEntitySetName());

        // そのままSQLのメンバーとせず、項目名エスケープを除去.
        final String unescapedName = OiyoCommonJdbcUtil.unescapeKakkoFieldName(impl.toString());
        final OiyoSettingsProperty property = OiyoInfoUtil.getOiyoEntityProperty(oiyoInfo, entitySet.getName(),
                unescapedName);
        sqlInfo.getSqlBuilder().append(OiyoCommonJdbcUtil.escapeKakkoFieldName(sqlInfo, property.getDbName()));

        return property;
    }

    /**
     * method を expand.
     * 
     * @param impl MethodImpl
     * @throws ODataApplicationException ODataアプリ例外が発生.
     */
    private void expandMethod(MethodImpl impl) throws ODataApplicationException {
        final OiyokanConstants.DatabaseType databaseType = OiyoInfoUtil
                .getOiyoDatabaseTypeByEntitySetName(sqlInfo.getOiyoInfo(), sqlInfo.getEntitySetName());

        // CONTAINS
        if (impl.getMethod() == MethodKind.CONTAINS) {
            // h2 database の POSITION/INSTR は 1 オリジンで発見せずが0 なので 1 を減らしています。
            switch (databaseType) {
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
            switch (databaseType) {
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
            switch (databaseType) {
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
            switch (databaseType) {
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
            switch (databaseType) {
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
            switch (databaseType) {
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
            switch (databaseType) {
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
            switch (databaseType) {
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
            log.error(OiyokanMessages.IY4105);
            throw new ODataApplicationException(OiyokanMessages.IY4105, 500, Locale.ENGLISH);
        }

        // TOTALSECONDS
        if (impl.getMethod() == MethodKind.TOTALSECONDS) {
            // [M109] NOT SUPPORTED: MethodKind.TOTALSECONDS
            log.error(OiyokanMessages.IY4106);
            throw new ODataApplicationException(OiyokanMessages.IY4106, 500, Locale.ENGLISH);
        }

        // DATE
        if (impl.getMethod() == MethodKind.DATE) {
            // [M110] NOT SUPPORTED: MethodKind.DATE
            log.error(OiyokanMessages.IY4107);
            throw new ODataApplicationException(OiyokanMessages.IY4107, 500, Locale.ENGLISH);
        }

        // TIME
        if (impl.getMethod() == MethodKind.TIME) {
            // [M111] NOT SUPPORTED: MethodKind.TIME
            log.error(OiyokanMessages.IY4108);
            throw new ODataApplicationException(OiyokanMessages.IY4108, 500, Locale.ENGLISH);
        }

        // TOTALOFFSETMINUTES
        if (impl.getMethod() == MethodKind.TOTALOFFSETMINUTES) {
            // [M112] NOT SUPPORTED: MethodKind.TOTALOFFSETMINUTES
            log.error(OiyokanMessages.IY4109);
            throw new ODataApplicationException(OiyokanMessages.IY4109, 500, Locale.ENGLISH);
        }

        // MINDATETIME
        if (impl.getMethod() == MethodKind.MINDATETIME) {
            // [M113] NOT SUPPORTED: MethodKind.MINDATETIME
            log.error(OiyokanMessages.IY4110);
            throw new ODataApplicationException(OiyokanMessages.IY4110, 500, Locale.ENGLISH);
        }

        // MAXDATETIME
        if (impl.getMethod() == MethodKind.MAXDATETIME) {
            // [M114] NOT SUPPORTED: MethodKind.MAXDATETIME
            log.error(OiyokanMessages.IY4111);
            throw new ODataApplicationException(OiyokanMessages.IY4111, 500, Locale.ENGLISH);
        }

        // NOW
        if (impl.getMethod() == MethodKind.NOW) {
            // [M115] NOT SUPPORTED: MethodKind.NOW
            log.error(OiyokanMessages.IY4112);
            throw new ODataApplicationException(OiyokanMessages.IY4112, 500, Locale.ENGLISH);
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
            log.error(OiyokanMessages.IY4113);
            throw new ODataApplicationException(OiyokanMessages.IY4113, 500, Locale.ENGLISH);
        }

        // GEOLENGTH
        if (impl.getMethod() == MethodKind.GEOLENGTH) {
            // [M117] NOT SUPPORTED: MethodKind.GEOLENGTH
            log.error(OiyokanMessages.IY4114);
            throw new ODataApplicationException(OiyokanMessages.IY4114, 500, Locale.ENGLISH);
        }

        // GEOINTERSECTS
        if (impl.getMethod() == MethodKind.GEOINTERSECTS) {
            // [M118] NOT SUPPORTED: MethodKind.GEOINTERSECTS
            log.error(OiyokanMessages.IY4115);
            throw new ODataApplicationException(OiyokanMessages.IY4115, 500, Locale.ENGLISH);
        }

        // CAST
        if (impl.getMethod() == MethodKind.CAST) {
            // [M119] NOT SUPPORTED: MethodKind.CAST
            log.error(OiyokanMessages.IY4116);
            throw new ODataApplicationException(OiyokanMessages.IY4116, 500, Locale.ENGLISH);
        }

        // ISOF
        if (impl.getMethod() == MethodKind.ISOF) {
            // [M120] NOT SUPPORTED: MethodKind.ISOF
            log.error(OiyokanMessages.IY4117);
            throw new ODataApplicationException(OiyokanMessages.IY4117, 500, Locale.ENGLISH);
        }

        // SUBSTRINGOF
        if (impl.getMethod() == MethodKind.SUBSTRINGOF) {
            switch (databaseType) {
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
        log.fatal(OiyokanMessages.IY4153 + ": " + impl.getMethod() + "," + impl.toString());
        throw new ODataApplicationException(OiyokanMessages.IY4153 + ": " + impl.getMethod() + "," + impl.toString(),
                500, Locale.ENGLISH);
    }

    private void expandUnary(UnaryImpl impl) throws ODataApplicationException {
        if (impl.getOperator() == UnaryOperatorKind.NOT) {
            sqlInfo.getSqlBuilder().append("(NOT (");
            expand(impl.getOperand());
            sqlInfo.getSqlBuilder().append("))");
            return;
        } else if (impl.getOperator() == UnaryOperatorKind.MINUS) {
            // [M131] NOT SUPPORTED: UnaryOperatorKind.MINUS
            log.error(OiyokanMessages.IY4125 + ": " + impl.toString());
            throw new ODataApplicationException(OiyokanMessages.IY4125, 500, Locale.ENGLISH);
        }

        // [M122] UNEXPECTED: Unsupported UnaryOperatorKind
        log.fatal(OiyokanMessages.IY4154 + ": " + impl.getOperator() + "," + impl.toString());
        throw new ODataApplicationException(OiyokanMessages.IY4154 + ": " + impl.getOperator() + "," + impl.toString(),
                500, Locale.ENGLISH);
    }
}
