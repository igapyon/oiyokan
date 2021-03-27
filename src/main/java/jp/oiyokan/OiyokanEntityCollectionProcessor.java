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

import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.core.uri.queryoption.CountOptionImpl;

import jp.oiyokan.basic.BasicJdbcEntityCollectionBuilder;

/**
 * Oiyokan による EntityCollectionProcessor 実装.
 * 
 * 実際のデータ取得処理を担当.
 */
public class OiyokanEntityCollectionProcessor implements EntityCollectionProcessor {
    /**
     * OData.
     */
    private OData odata;

    /**
     * サービスメタデータ.
     */
    private ServiceMetadata serviceMetadata;

    /**
     * 初期化タイミングにて ODataやサービスメタデータの情報を記憶.
     * 
     * @param odata           ODataインスタンス.
     * @param serviceMetadata サービスメタデータ.
     */
    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    /**
     * 要素コレクションを読み込み.
     * 
     * @param request        OData リクエスト.
     * @param response       OData レスポンス.
     * @param uriInfo        URI情報.
     * @param responseFormat レスポンスのフォーマット.
     * @throws SerializerException       直列化に失敗した場合.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, //
            UriInfo uriInfo, ContentType responseFormat) //
            throws ODataApplicationException, SerializerException {
        try {

            // URI情報からURIリソースの指定を取得.
            List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
            // URIリソースの最初のものを要素セット指定とみなす.
            // Note: パスのうち1番目の項目のみ処理.
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
            // 要素セットの指定からEDM要素セットを取得.
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

            OiyokanEntityCollectionBuilderInterface entityCollectionBuilder = getEntityCollectionBuilder(edmEntitySet);

            // 要素セットの指定をもとに要素コレクションを取得.
            // これがデータ本体に該当.
            EntityCollection eCollection = entityCollectionBuilder.build(edmEntitySet, uriInfo);

            // 指定のレスポンスフォーマットに合致する直列化を準備.
            ODataSerializer serializer = odata.createSerializer(responseFormat);

            // 要素セットから要素型のEDM情報を取得してコンテキストURLをビルド.
            EdmEntityType edmEntityType = edmEntitySet.getEntityType();
            ContextURL conUrl = ContextURL.with().entitySet(edmEntitySet).build();

            // 要素のIdを作成.
            final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();

            // 直列化の処理.
            EntityCollectionSerializerOptions.Builder builder = EntityCollectionSerializerOptions.with() //
                    .id(id).contextURL(conUrl);
            if (uriInfo.getCountOption() != null) {
                // $count あり.
                final CountOptionImpl copt = new CountOptionImpl();
                copt.setValue(true);
                builder.count(copt);
            }
            if (uriInfo.getSelectOption() != null) {
                // $select あり.
                builder.select(uriInfo.getSelectOption());
            }

            SerializerResult serResult = serializer.entityCollection( //
                    serviceMetadata, edmEntityType, eCollection, builder.build());

            // OData レスポンスを返却.
            response.setContent(serResult.getContent());
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
        } catch (ODataApplicationException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            // NullPointerException など想定しない例外の場合にここを通過させてスタックトレースを出力させる。
            // ODataRuntimeException についてもこちらを通過させてスタックトレース出力させる。
            ex.printStackTrace();
            throw ex;
        }
    }

    private static final OiyokanEntityCollectionBuilderInterface getEntityCollectionBuilder(EdmEntitySet edmEntitySet)
            throws ODataApplicationException {
        OiyokanCsdlEntitySet entitySet = null;
        OiyokanEdmProvider provider = new OiyokanEdmProvider();
        for (CsdlEntitySet look : provider.getEntityContainer().getEntitySets()) {
            if (edmEntitySet.getName().equals(look.getName())) {
                entitySet = (OiyokanCsdlEntitySet) look;
                break;
            }
        }

        switch (entitySet.getDatabaseType()) {
        case h2:
        case postgres:
        case MySQL:
        case MSSQL:
        case ORACLE:
        default:
            return new BasicJdbcEntityCollectionBuilder();
        case BigQuery:
            // TODO FIXME BigQuery用の実装が必要.
            // [M999] NOT IMPLEMENTED: Generic NOT implemented message.
            System.err.println(OiyokanMessages.M999);
            throw new ODataApplicationException(OiyokanMessages.M999, 500, Locale.ENGLISH);
        }
    }
}
