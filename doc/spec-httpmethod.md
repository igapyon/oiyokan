## [Oiyokan] Supported HTTP method by Oiyokan v1.14

| HTTP method               | Corresponding SQL | OData specification to be compliant                |
| ------                    | ------            | ------                                             |
| GET                       | SELECT            | [Request](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_RequestingData) ([Individual](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_RequestingIndividualEntities), [Query](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionselect)) |
| POST                      | INSERT            | [Create](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_CreateanEntity) |
| PATCH                     | UPSERT            | [Update](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_UpdateanEntity) |
| PATCH (If-Match="*")      | UPDATE            | [Update with header If-Match](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_HeaderIfMatch) |
| PATCH (If-None-Match="*") | INSERT            | [Update with header If-None-Match](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_HeaderIfNoneMatch) |
| DELETE                    | DELETE            | [Delete](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_DeleteanEntity) |
| PUT (NOT supported)       | (NOT supported)   | [OData v4 spec](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_UpdateanEntity) |

- It is recommended to use PATCH with header `If-Match = "*"` for the purpose of UPDATE.

via: [diary](https://raw.githubusercontent.com/igapyon/diary/devel/2021/ig210513.src.md)

