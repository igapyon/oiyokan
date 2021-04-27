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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

/**
 * Oiyokan による Entity Processor
 */
public class OiyokanEntityProcessor implements EntityProcessor {
    private static final Log log = LogFactory.getLog(OiyokanEntityProcessor.class);

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        log.trace("OiyokanEntityProcessor#init()");

        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
            throws ODataApplicationException, ODataLibraryException {
        log.trace("OiyokanEntityProcessor#readEntity(" + request.getRawODataPath() + "," + request.getRawQueryPath()
                + ")");

        try {
            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = OiyokanEdmProvider.getOiyoInfoInstance();

            log.trace("OiyokanEntityProcessor#readEntity: 1. retrieve the Entity Type");
            List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

            log.trace("OiyokanEntityProcessor#readEntity: "
                    + "Note: only in our example we can assume that the first segment is the EntitySet");
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

            log.trace("OiyokanEntityProcessor#readEntity: 2. retrieve the data from backend");
            List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();

            // データベースに接続.
            final OiyoSettingsDatabase database = OiyoInfoUtil.getOiyoDatabaseByEntitySetName(oiyoInfo,
                    edmEntitySet.getName());

            final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, edmEntitySet.getName());
            if (entitySet.getCanRead() != null && entitySet.getCanRead() == false) {
                // [IY8102] ERROR: No Read access by canRead==false.
                log.error(OiyokanMessages.IY8102 + ": Entity:" + edmEntitySet.getName());
                throw new ODataApplicationException(OiyokanMessages.IY8102 + ": Entity:" + edmEntitySet.getName(), //
                        OiyokanMessages.IY8102_CODE, Locale.ENGLISH);
            }

            Entity entity = null;
            try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
                entity = new OiyoBasicJdbcEntityOneBuilder(oiyoInfo).readEntityData(connTargetDb, uriInfo, edmEntitySet,
                        keyPredicates);
            } catch (SQLException ex) {
                // [IY3107] Database exception occured (readEntity)
                log.error(OiyokanMessages.IY3107 + ": " + ex.toString());
                throw new ODataApplicationException(OiyokanMessages.IY3107, //
                        OiyokanMessages.IY3107_CODE, Locale.ENGLISH);
            }

            log.trace("OiyokanEntityProcessor#readEntity: 3. serialize");
            EdmEntityType edmEntityType = edmEntitySet.getEntityType();

            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
            // expand and select currently not supported
            EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

            ODataSerializer serializer = odata.createSerializer(responseFormat);
            SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntityType, entity, options);
            InputStream entityStream = serializerResult.getContent();

            log.trace("OiyokanEntityProcessor#readEntity: 4. configure the response object");
            response.setContent(entityStream);
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

        } catch (ODataApplicationException | ODataLibraryException ex) {
            // TODO message
            log.warn("WARN: OiyokanEntityProcessor#readEntity(" + request.getRawODataPath() + ","
                    + request.getRawQueryPath() + "): " + ex.toString());
            throw ex;
        } catch (RuntimeException ex) {
            // TODO message
            log.error("ERROR: OiyokanEntityProcessor#readEntity(" + request.getRawODataPath() + ","
                    + request.getRawQueryPath() + "): " + ex.toString(), ex);
            throw ex;
        }
    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
            ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        log.trace("OiyokanEntityProcessor#createEntity(" + request.getRawODataPath() + "," + request.getRawQueryPath()
                + ")");

        try {
            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = OiyokanEdmProvider.getOiyoInfoInstance();

            // https://olingo.apache.org/doc/odata4/tutorials/write/tutorial_write.html

            log.trace("OiyokanEntityProcessor#createEntity: 1. retrieve the Entity Type from the URI");
            List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

            log.trace("OiyokanEntityProcessor#createEntity: "
                    + "Note: only in our example we can assume that the first segment is the EntitySet");
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
            EdmEntityType edmEntityType = edmEntitySet.getEntityType();

            final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, edmEntitySet.getName());
            if (entitySet.getCanCreate() != null && entitySet.getCanCreate() == false) {
                // [IY8101] ERROR: No Create access by canCreate==false.
                log.error(OiyokanMessages.IY8101 + ": Entity:" + edmEntitySet.getName());
                throw new ODataApplicationException(OiyokanMessages.IY8101 + ": Entity:" + edmEntitySet.getName(), //
                        OiyokanMessages.IY8101_CODE, Locale.ENGLISH);
            }

            log.trace("OiyokanEntityProcessor#createEntity: 2. create the data in backend");

            log.trace("OiyokanEntityProcessor#createEntity: "
                    + "2.1. retrieve the payload from the POST request for the entity to create and deserialize it");
            InputStream requestInputStream = request.getBody();
            log.trace("OiyokanEntityProcessor#createEntity: 2.1.1. createDeserializer");
            ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
            log.trace("OiyokanEntityProcessor#createEntity: 2.1.2. deserializer.entity");
            DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
            log.trace("OiyokanEntityProcessor#createEntity: 2.1.3. result.getEntity");
            Entity requestEntity = result.getEntity();

            log.trace("OiyokanEntityProcessor#createEntity: "
                    + "2.2 do the creation in backend, which returns the newly created entity");
            Entity createdEntity = new OiyoBasicJdbcEntityOneBuilder(oiyoInfo).createEntityData(uriInfo, edmEntitySet,
                    requestEntity);

            log.trace("OiyokanEntityProcessor#createEntity: "
                    + "3. serialize the response (we have to return the created entity)");
            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
            // expand and select currently not supported
            EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

            ODataSerializer serializer = this.odata.createSerializer(responseFormat);
            SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity,
                    options);

            log.trace("OiyokanEntityProcessor#createEntity: 4. configure the response object");
            response.setContent(serializedResponse.getContent());
            response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

        } catch (ODataApplicationException | ODataLibraryException ex) {
            // TODO message
            log.warn("WARN: OiyokanEntityProcessor#createEntity(" + request.getRawODataPath() + ","
                    + request.getRawQueryPath() + "): " + ex.toString());
            throw ex;
        } catch (RuntimeException ex) {
            // TODO message
            log.error("ERROR: OiyokanEntityProcessor#createEntity(" + request.getRawODataPath() + ","
                    + request.getRawQueryPath() + "): " + ex.toString(), ex);
            throw ex;
        }
    }

    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
            ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        log.trace("OiyokanEntityProcessor#updateEntity(" + request.getRawODataPath() + "," + request.getRawQueryPath()
                + ")");

        try {
            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = OiyokanEdmProvider.getOiyoInfoInstance();

            List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

            log.trace("OiyokanEntityProcessor#updateEntity: "
                    + "Note: only in our example we can assume that the first segment is the EntitySet");
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

            final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, edmEntitySet.getName());
            if (entitySet.getCanUpdate() != null && entitySet.getCanUpdate() == false) {
                // [IY8103] ERROR: No Update access by canUpdate==false.
                log.error(OiyokanMessages.IY8103 + ": Entity:" + edmEntitySet.getName());
                throw new ODataApplicationException(OiyokanMessages.IY8103 + ": Entity:" + edmEntitySet.getName(), //
                        OiyokanMessages.IY8103_CODE, Locale.ENGLISH);
            }

            log.trace("OiyokanEntityProcessor#updateEntity: 2. retrieve the data from backend");
            List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
            // Olingサンプルには記載あったものの、Oiyokanでは該当レコードの存在チェックは実施しない。

            InputStream requestInputStream = request.getBody();
            ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
            DeserializerResult result = deserializer.entity(requestInputStream, edmEntitySet.getEntityType());
            final Entity requestEntity = result.getEntity();

            if (request.getMethod().equals(HttpMethod.PATCH)) {
                final boolean ifMatch = ("*".equals(request.getHeader("If-Match")));
                final boolean ifNoneMatch = ("*".equals(request.getHeader("If-None-Match")));

                if (ifMatch) {
                    log.trace("OiyokanEntityProcessor#updateEntity: If-Match");
                }
                if (ifNoneMatch) {
                    log.trace("OiyokanEntityProcessor#updateEntity: If-None-Match");
                }

                // 指定項目のみ設定
                // in case of PATCH, the existing property is not touched
                new OiyoBasicJdbcEntityOneBuilder(oiyoInfo).updateEntityDataPatch(uriInfo, edmEntitySet, keyPredicates,
                        requestEntity, ifMatch, ifNoneMatch);
            } else if (request.getMethod().equals(HttpMethod.PUT)) {
                // [IY1106] NOT SUPPORTED: PUT: use PATCH to update Entity.
                log.error(OiyokanMessages.IY1106);
                throw new ODataApplicationException(OiyokanMessages.IY1106, OiyokanMessages.IY1106_CODE,
                        Locale.ENGLISH);
            } else {
                // [IY3113] UNEXPECTED: Must NOT pass this case.
                log.fatal(OiyokanMessages.IY3113);
                throw new ODataApplicationException(OiyokanMessages.IY3113, OiyokanMessages.IY3113_CODE,
                        Locale.ENGLISH);
            }

            // TODO FIXME 下記仕様が未実装。
            // Upon successful completion the service responds with either 200 OK and a
            // representation of the updated entity, or 204 No Content. The client may
            // request that the response SHOULD include a body by specifying a Prefer header
            // with a value of return=representation, or by specifying the system query
            // options $select or $expand.

            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

        } catch (ODataApplicationException | ODataLibraryException ex) {
            // TODO message
            log.warn("WARN: OiyokanEntityProcessor#updateEntity(" + request.getRawODataPath() + ","
                    + request.getRawQueryPath() + "): " + ex.toString());
            throw ex;
        } catch (RuntimeException ex) {
            // TODO message
            log.error("ERROR: OiyokanEntityProcessor#updateEntity(" + request.getRawODataPath() + ","
                    + request.getRawQueryPath() + "): " + ex.toString(), ex);
            throw ex;
        }
    }

    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
            throws ODataApplicationException, ODataLibraryException {
        log.trace("OiyokanEntityProcessor#deleteEntity(" + request.getRawODataPath() + "," + request.getRawQueryPath()
                + ")");

        try {
            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = OiyokanEdmProvider.getOiyoInfoInstance();

            log.trace("OiyokanEntityProcessor#deleteEntity: "
                    + "1. Retrieve the entity set which belongs to the requested entity");
            List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

            log.trace("OiyokanEntityProcessor#deleteEntity: "
                    + "Note: only in our example we can assume that the first segment is the EntitySet");
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

            final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, edmEntitySet.getName());
            if (entitySet.getCanDelete() != null && entitySet.getCanDelete() == false) {
                // [IY8104] ERROR: No Delete access by canDelete==false.
                log.error(OiyokanMessages.IY8104 + ": Entity:" + edmEntitySet.getName());
                throw new ODataApplicationException(OiyokanMessages.IY8104 + ": Entity:" + edmEntitySet.getName(), //
                        OiyokanMessages.IY8104_CODE, Locale.ENGLISH);
            }

            log.trace("OiyokanEntityProcessor#deleteEntity: " + "2. delete the data in backend");
            List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
            new OiyoBasicJdbcEntityOneBuilder(oiyoInfo).deleteEntityData(uriInfo, edmEntitySet, keyPredicates);

            log.trace("OiyokanEntityProcessor#deleteEntity: " + "3. configure the response object");
            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

        } catch (ODataApplicationException /* | ODataLibraryException */ ex) {
            // TODO message
            log.warn("WARN: OiyokanEntityProcessor#deleteEntity(" + request.getRawODataPath() + ","
                    + request.getRawQueryPath() + "): " + ex.toString());
            throw ex;
        } catch (RuntimeException ex) {
            // TODO message
            log.error("ERROR: OiyokanEntityProcessor#deleteEntity(" + request.getRawODataPath() + ","
                    + request.getRawQueryPath() + "): " + ex.toString(), ex);
            throw ex;
        }
    }
}
