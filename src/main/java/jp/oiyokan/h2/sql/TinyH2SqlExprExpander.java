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
package jp.oiyokan.h2.sql;

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

/**
 * SQL文を構築するための簡易クラスの、Expression を SQLに変換する処理.
 */
public class TinyH2SqlExprExpander {
    /**
     * SQL構築のデータ構造.
     */
    private TinySqlBuildInfo sqlInfo = null;

    /**
     * コンストラクタ.
     * 
     * @param sqlInfo SQL構築のデータ構造.
     */
    public TinyH2SqlExprExpander(TinySqlBuildInfo sqlInfo) {
        this.sqlInfo = sqlInfo;
    }

    /**
     * フィルタを展開。WHEREになる。
     * 
     * @param filterExpression フィルタ表現.
     */
    public void expand(Expression filterExpression) {
        if (filterExpression instanceof AliasImpl) {
            throw new IllegalArgumentException("NOT SUPPORTED:Expression:AliasImpl");
        } else if (filterExpression instanceof BinaryImpl) {
            expandBinary((BinaryImpl) filterExpression);
            return;
        } else if (filterExpression instanceof EnumerationImpl) {
            throw new IllegalArgumentException("NOT SUPPORTED:Expression:EnumerationImpl");
        } else if (filterExpression instanceof LambdaRefImpl) {
            throw new IllegalArgumentException("NOT SUPPORTED:Expression:LambdaRefImpl");
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
            throw new IllegalArgumentException("NOT SUPPORTED:Expression:TypeLiteralImpl");
        } else if (filterExpression instanceof UnaryImpl) {
            UnaryImpl impl = (UnaryImpl) filterExpression;
            expandUnary(impl);
            return;
        }

        final String message = "Unexpected Case: Unsupported expression:" + filterExpression.getClass().getName() + ","
                + filterExpression.toString() + "]";
        System.err.println(message);
        throw new IllegalArgumentException(message);
    }

    ///////////////////////////////////////////////////////////////
    // 内部の実際処理.

    private void expandBinary(BinaryImpl impl) {
        BinaryOperatorKind opKind = impl.getOperator();
        if (opKind == BinaryOperatorKind.HAS) {
            // HAS
            throw new IllegalArgumentException("NOT SUPPORTED:BinaryOperatorKind:" + opKind);
        } else if (opKind == BinaryOperatorKind.IN) {
            // IN
            throw new IllegalArgumentException("NOT SUPPORTED:BinaryOperatorKind:" + opKind);
        } else if (opKind == BinaryOperatorKind.MUL) {
            // MUL
            throw new IllegalArgumentException("NOT SUPPORTED:BinaryOperatorKind:" + opKind);
        } else if (opKind == BinaryOperatorKind.DIV) {
            // DIV
            throw new IllegalArgumentException("NOT SUPPORTED:BinaryOperatorKind:" + opKind);
        } else if (opKind == BinaryOperatorKind.MOD) {
            // MOD
            throw new IllegalArgumentException("NOT SUPPORTED:BinaryOperatorKind:" + opKind);
        } else if (opKind == BinaryOperatorKind.ADD) {
            // ADD
            throw new IllegalArgumentException("NOT SUPPORTED:BinaryOperatorKind:" + opKind);
        } else if (opKind == BinaryOperatorKind.SUB) {
            // SUB
            throw new IllegalArgumentException("NOT SUPPORTED:BinaryOperatorKind:" + opKind);
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
            expand(impl.getLeftOperand());
            sqlInfo.getSqlBuilder().append(" = ");
            expand(impl.getRightOperand());
            sqlInfo.getSqlBuilder().append(")");
            return;
        } else if (opKind == BinaryOperatorKind.NE) {
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

        final String message = "Unexpected Case: Unsupported binary operator:" + opKind + "," + impl.toString() + "]";
        System.err.println(message);
        throw new IllegalArgumentException(message);
    }

    private void expandLiteral(LiteralImpl impl) {
        String value = impl.toString();
        if (value.startsWith("'") && value.endsWith("'")) {
            // 文字列リテラルについては前後のクオートを除去して記憶.
            value = value.substring(1, value.length() - 1);
            // 文字列リテラルとしてパラメータ化クエリで扱う.
            sqlInfo.getSqlBuilder().append("?");
            sqlInfo.getSqlParamList().add(value);
            return;
        }

        // パラメータクエリ化は断念.
        // 単に value をそのままSQL文に追加。
        sqlInfo.getSqlBuilder().append(value);
    }

    private void expandMember(MemberImpl impl) {
        // そのままSQLのメンバーとする。
        sqlInfo.getSqlBuilder().append(impl.toString());
    }

    private void expandMethod(MethodImpl impl) {
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
        // TODO 未実装

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

        // TOTALSECONDS

        // DATE

        // TIME

        // TOTALOFFSETMINUTES

        // MINDATETIME

        // MAXDATETIME

        // NOW

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

        // GEOLENGTH

        // GEOINTERSECTS

        // CAST

        // ISOF

        // SUBSTRINGOF
        if (impl.getMethod() == MethodKind.SUBSTRINGOF) {
            sqlInfo.getSqlBuilder().append("(POSITION(");
            expand(impl.getParameters().get(0));
            sqlInfo.getSqlBuilder().append(",");
            expand(impl.getParameters().get(1));
            sqlInfo.getSqlBuilder().append(") > 0)");
            return;
        }

        final String message = "Unexpected Case: NOT SUPPORTED MethodKind:" + impl.getMethod() + "," + impl.toString()
                + "]";
        System.err.println(message);
        throw new IllegalArgumentException(message);
    }

    private void expandUnary(UnaryImpl impl) {
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

        final String message = "Unexpected Case: Unsupported UnaryOperatorKind:" + impl.getOperator() + ","
                + impl.toString() + "]";
        System.err.println(message);
        throw new IllegalArgumentException(message);
    }
}
