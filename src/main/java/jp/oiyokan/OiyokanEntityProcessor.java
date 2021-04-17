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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

import jp.oiyokan.basic.OiyoBasicJdbcEntityOneBuilder;
import jp.oiyokan.basic.OiyoBasicJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

/**
 * Oiyokan による Entity Processor
 */
public class OiyokanEntityProcessor implements EntityProcessor {
    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
        try {
            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = OiyokanCsdlEntityContainer.getOiyoInfoInstance();

            // 1. retrieve the Entity Type
            List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

            // Note: only in our example we can assume that the first segment is the
            // EntitySet
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

            // 2. retrieve the data from backend
            List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();

            // データベースに接続.
            final OiyokanCsdlEntitySet entitySet = OiyoBasicJdbcEntityOneBuilder.findEntitySet(edmEntitySet);
            if (entitySet == null) {
                // [M211] No such EntitySet found (createEntity)
                System.err.println(OiyokanMessages.M211);
                throw new ODataApplicationException(OiyokanMessages.M211, 500, Locale.ENGLISH);
            }

            final OiyoSettingsEntitySet oiyoEntitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo,
                    edmEntitySet.getName());
            if (oiyoEntitySet.getCanRead() != null && oiyoEntitySet.getCanRead() == false) {
                // [M052] WARN: No Read access by canRead==false.
                System.err.println(OiyokanMessages.M052 + ": Entity:" + edmEntitySet.getName());
                throw new ODataApplicationException(OiyokanMessages.M052 + ": Entity:" + edmEntitySet.getName(), //
                        OiyokanMessages.M052_CODE, Locale.ENGLISH);
            }

            Entity entity = null;
            try (Connection connTargetDb = OiyoBasicJdbcUtil.getConnection(entitySet.getSettingsDatabase(oiyoInfo))) {
                entity = new OiyoBasicJdbcEntityOneBuilder(oiyoInfo).readEntityData(connTargetDb, uriInfo, edmEntitySet,
                        keyPredicates);
            } catch (SQLException ex) {
                // [M210] Database exception occured (readEntity)
                System.err.println(OiyokanMessages.M210 + ": " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.M210, //
                        OiyokanMessages.M210_CODE, Locale.ENGLISH);
            }

            // 3. serialize
            EdmEntityType entityType = edmEntitySet.getEntityType();

            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
            // expand and select currently not supported
            EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

            ODataSerializer serializer = odata.createSerializer(responseFormat);
            SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, entity, options);
            InputStream entityStream = serializerResult.getContent();

            // 4. configure the response object
            response.setContent(entityStream);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

        } catch (RuntimeException ex) {
            System.err.println("OiyokanEntityProcessor#readEntity: exception: " + ex.toString());
            throw ex;
        }
    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
            ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        try {
            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = OiyokanCsdlEntityContainer.getOiyoInfoInstance();

            // https://olingo.apache.org/doc/odata4/tutorials/write/tutorial_write.html

            // 1. Retrieve the entity type from the URI

            // 1. retrieve the Entity Type
            List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

            // Note: only in our example we can assume that the first segment is the
            // EntitySet
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
            EdmEntityType edmEntityType = edmEntitySet.getEntityType();

            final OiyoSettingsEntitySet oiyoEntitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo,
                    edmEntitySet.getName());
            if (oiyoEntitySet.getCanCreate() != null && oiyoEntitySet.getCanCreate() == false) {
                // [M051] WARN: No Create access by canCreate==false.
                System.err.println(OiyokanMessages.M051 + ": Entity:" + edmEntitySet.getName());
                throw new ODataApplicationException(OiyokanMessages.M051 + ": Entity:" + edmEntitySet.getName(), //
                        OiyokanMessages.M051_CODE, Locale.ENGLISH);
            }

            // 2. create the data in backend
            // 2.1. retrieve the payload from the POST request for the entity to create and
            // deserialize it
            InputStream requestInputStream = request.getBody();
            ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
            DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
            Entity requestEntity = result.getEntity();
            // 2.2 do the creation in backend, which returns the newly created entity
            Entity createdEntity = new OiyoBasicJdbcEntityOneBuilder(oiyoInfo).createEntityData(uriInfo, edmEntitySet,
                    requestEntity);

            // 3. serialize the response (we have to return the created entity)
            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
            // expand and select currently not supported
            EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

            ODataSerializer serializer = this.odata.createSerializer(responseFormat);
            SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity,
                    options);

            // 4. configure the response object
            response.setContent(serializedResponse.getContent());
            response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

        } catch (RuntimeException ex) {
            ex.printStackTrace();
            System.err.println("OiyokanEntityProcessor#createEntity: exception: " + ex.toString());
            throw ex;
        }
    }

    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
            ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        try {
            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = OiyokanCsdlEntityContainer.getOiyoInfoInstance();

            List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

            // Note: only in our example we can assume that the first segment is the
            // EntitySet
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

            final OiyoSettingsEntitySet oiyoEntitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo,
                    edmEntitySet.getName());
            if (oiyoEntitySet.getCanUpdate() != null && oiyoEntitySet.getCanUpdate() == false) {
                // [M053] WARN: No Update access by canUpdate==false.
                System.err.println(OiyokanMessages.M053 + ": Entity:" + edmEntitySet.getName());
                throw new ODataApplicationException(OiyokanMessages.M053 + ": Entity:" + edmEntitySet.getName(), //
                        OiyokanMessages.M053_CODE, Locale.ENGLISH);
            }

            // 2. retrieve the data from backend
            List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
            // Olingサンプルには記載あったものの、Oiyokanでは該当レコードの存在チェックは実施しない。

            InputStream requestInputStream = request.getBody();
            ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
            DeserializerResult result = deserializer.entity(requestInputStream, edmEntitySet.getEntityType());
            final Entity requestEntity = result.getEntity();

            if (request.getMethod().equals(HttpMethod.PATCH)) {
                final boolean ifMatch = ("*".equals(request.getHeader("If-Match")));
                final boolean ifNoneMatch = ("*".equals(request.getHeader("If-None-Match")));

                // 指定項目のみ設定
                // in case of PATCH, the existing property is not touched
                new OiyoBasicJdbcEntityOneBuilder(oiyoInfo).updateEntityDataPatch(uriInfo, edmEntitySet, keyPredicates,
                        requestEntity, ifMatch, ifNoneMatch);
            } else if (request.getMethod().equals(HttpMethod.PUT)) {
                // [M016] NOT SUPPORTED: PUT: use PATCH to update Entity.
                System.err.println(OiyokanMessages.M016);
                throw new ODataApplicationException(OiyokanMessages.M016, OiyokanMessages.M206_CODE, Locale.ENGLISH);
            } else {
                // [M216] UNEXPECTED: Must NOT pass this case.
                System.err.println(OiyokanMessages.M216);
                throw new ODataApplicationException(OiyokanMessages.M216, OiyokanMessages.M216_CODE, Locale.ENGLISH);
            }

            // TODO FIXME
            // Upon successful completion the service responds with either 200 OK and a
            // representation of the updated entity, or 204 No Content. The client may
            // request that the response SHOULD include a body by specifying a Prefer header
            // with a value of return=representation, or by specifying the system query
            // options $select or $expand.

            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

        } catch (RuntimeException ex) {
            ex.printStackTrace();
            System.err.println("OiyokanEntityProcessor#updateEntity: exception: " + ex.toString());
            throw ex;
        }
    }

    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {
        try {
            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = OiyokanCsdlEntityContainer.getOiyoInfoInstance();

            // 1. Retrieve the entity set which belongs to the requested entity
            List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
            // Note: only in our example we can assume that the first segment is the
            // EntitySet
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

            final OiyoSettingsEntitySet oiyoEntitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo,
                    edmEntitySet.getName());
            if (oiyoEntitySet.getCanDelete() != null && oiyoEntitySet.getCanDelete() == false) {
                // [M054] WARN: No Delete access by canDelete==false.
                System.err.println(OiyokanMessages.M054 + ": Entity:" + edmEntitySet.getName());
                throw new ODataApplicationException(OiyokanMessages.M054 + ": Entity:" + edmEntitySet.getName(), //
                        OiyokanMessages.M054_CODE, Locale.ENGLISH);
            }

            // 2. delete the data in backend
            List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
            new OiyoBasicJdbcEntityOneBuilder(oiyoInfo).deleteEntityData(uriInfo, edmEntitySet, keyPredicates);

            // 3. configure the response object
            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

        } catch (RuntimeException ex) {
            // ex.printStackTrace();
            System.err.println("OiyokanEntityProcessor#deleteEntity: exception: " + ex.toString());
            throw ex;
        }
    }
}
