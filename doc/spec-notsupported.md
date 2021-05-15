## [Oiyokan] Unsupported features in Oiyokan v1.14

## $search, $apply, $expand, custom query are NOT supported

The following features are not supported by Oiyokan v1.14.

- $search
- $apply
- $expand
- customQuery
- deltaToken

see: [github v1.14.20210510 OiyoBasicJdbcEntityCollectionBuilder.java](https://github.com/igapyon/oiyokan/blob/v1.14.20210510/src/main/java/jp/oiyokan/basic/OiyoBasicJdbcEntityCollectionBuilder.java#L104)

## Some $filter operations are NOT supported

The following ops are not supported by Oiyokan v1.14.

### $filter ops
- $filter => Alias
- $filter => Enumeration
- $filter => LambdaRef
- $filter => TypeLiteral

### $filter Kind
- BinaryOperatorKind.HAS
- BinaryOperatorKind.IN
- BinaryOperatorKind.MUL
- BinaryOperatorKind.DIV
- BinaryOperatorKind.MOD
- BinaryOperatorKind.ADD
- BinaryOperatorKind.SUB
- MethodKind.YEAR
- MethodKind.MONTH
- MethodKind.DAY
- MethodKind.HOUR
- MethodKind.MINUTE
- MethodKind.SECOND
- MethodKind.FRACTIONALSECONDS
- MethodKind.TOTALSECONDS
- MethodKind.DATE
- MethodKind.TIME
- MethodKind.TOTALOFFSETMINUTES
- MethodKind.MINDATETIME
- MethodKind.MAXDATETIME
- MethodKind.NOW
- MethodKind.ROUND
- MethodKind.FLOOR
- MethodKind.CEILING
- MethodKind.GEODISTANCE
- MethodKind.GEOLENGTH
- MethodKind.GEOINTERSECTS
- MethodKind.CAST
- MethodKind.ISOF
- UnaryOperatorKind.MINUS

see: [github v1.14.20210510 OiyoSqlQueryListExpr.java](https://github.com/igapyon/oiyokan/blob/v1.14.20210510/src/main/java/jp/oiyokan/basic/sql/OiyoSqlQueryListExpr.java#L82)

via: [diary](https://raw.githubusercontent.com/igapyon/diary/devel/2021/ig210512.src.md)
