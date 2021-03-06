# Oiyokan spec memo

## Supported target RDBMS

- h2 database (1.4)
- PostgreSQL (13)
- MySQL (8)
- SQL Server (2008)
- Oracle XE (18c)

## Supported OData Method

see `spec-httpmethod.md`.

## Supported OData system query options

- $select
- $count
- $filter
- $orderby
- $top
- $skip

see `spec-notsupported.md` for more details.

# Mapping memo at standard

## OData v4 JSON format

see:
https://docs.oasis-open.org/odata/odata-json-format/v4.01/odata-json-format-v4.01.html

- Primitive values : [RFC8259].
- Null は JSON literal の null.
- Edm.Boolean は JSON リテラルの true と false で表現.
- Edm.Byte, Edm.SByte, Edm.Int16, Edm.Int32, Edm.Int64, Edm.Single, Edm.Double, Edm.Decimal は JSON number で表現。-INF, INF, NaN の場合は　string で表現.
- Edm.String は JSON escape ルールに従う.
- Edm.Binary, Edm.Date, Edm.DateTimeOffset, Edm.Duration, Edm.Guid, Edm.TimeOfDay は [OData-ABNF] に従い JSON string.
- Edm.Int64, Edm.Decimal で IEEE754 を超える場合は、注釈の　value に文字列表現で記述.
- Geography : Not supported at Oiyokan.

http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part3-csdl/odata-v4.0-errata03-os-part3-csdl-complete.html#_Toc453752517

## [OData-ABNF]

- http://docs.oasis-open.org/odata/odata/v4.01/csprd03/abnf/odata-abnf-construction-rules.txt

## JSON at RFC

- https://tools.ietf.org/html/rfc8259

# 仕様メモ

- Sakila DB定義の create_date が Postgres版と MySQL 版とで型が違う.
    これにより、同一の Oiyo ファイルでアクセスすると "The types 'Edm.DateTimeOffset' and 'Edm.Date' are not compatible. が発生する。
