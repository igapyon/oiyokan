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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
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
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.queryoption.CountOptionImpl;
import org.apache.olingo.server.core.uri.validator.UriValidationException;

import jp.oiyokan.basic.OiyoBasicJdbcEntityCollectionBuilder;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

/**
 * Oiyokan ????????? EntityCollectionProcessor ??????.
 * 
 * ???????????????????????????????????????.
 */
public class OiyokanEntityCollectionProcessor implements EntityCollectionProcessor {
    private static final Log log = LogFactory.getLog(OiyokanEntityCollectionProcessor.class);

    /**
     * OData.
     */
    private OData odata;

    /**
     * ???????????????????????????.
     */
    private ServiceMetadata serviceMetadata;

    private OiyoInfo oiyoInfo = null;

    public OiyokanEntityCollectionProcessor(OiyoInfo oiyoInfo) {
        this.oiyoInfo = oiyoInfo;
    }

    /**
     * ?????????????????????????????? OData????????????????????????????????????????????????. {@inheritDoc}
     * 
     * @param odata           OData??????????????????.
     * @param serviceMetadata ???????????????????????????.
     */
    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        log.trace("OiyokanEntityCollectionProcessor#init()");

        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    /**
     * ???????????????????????????????????????. {@inheritDoc}
     * 
     * @param request        OData ???????????????.
     * @param response       OData ???????????????.
     * @param uriInfo        URI??????.
     * @param responseFormat ????????????????????????????????????.
     * @throws SerializerException       ??????????????????????????????.
     * @throws ODataApplicationException OData????????????????????????????????????.
     */
    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, //
            UriInfo uriInfo, ContentType responseFormat) //
            throws ODataApplicationException, SerializerException {
        log.trace("OiyokanEntityCollectionProcessor#readEntityCollection(" + request.getRawODataPath() + ","
                + request.getRawQueryPath() + ")");

        try {
            // ????????????????????? OiyoSettings ????????????
            OiyokanEdmProvider.setupOiyoSettingsInstance(oiyoInfo);
            if (oiyoInfo.getRawBaseUri() == null) {
                oiyoInfo.setRawBaseUri(request.getRawBaseUri());
            }

            // URI????????????URI??????????????????????????????.
            List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
            // URI??????????????????????????????????????????????????????????????????.
            // Note: ???????????????1???????????????????????????.
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
            // ??????????????????????????????EDM????????????????????????.
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

            OiyokanEntityCollectionBuilderInterface entityCollectionBuilder = getEntityCollectionBuilder(oiyoInfo,
                    edmEntitySet);

            // ?????????????????????????????????????????????????????????????????????.
            // ?????????????????????????????????.
            log.trace("OiyokanEntityCollectionProcessor#readEntityCollection: eCollection: begin.");
            EntityCollection eCollection = entityCollectionBuilder.build(edmEntitySet, uriInfo);
            log.trace("OiyokanEntityCollectionProcessor#readEntityCollection: eCollection: end.");

            // ???????????????????????????????????????????????????????????????????????????.
            ODataSerializer serializer = odata.createSerializer(responseFormat);

            // ?????????????????????????????????EDM???????????????????????????????????????URL????????????.
            EdmEntityType edmEntityType = edmEntitySet.getEntityType();
            ContextURL conUrl = ContextURL.with().entitySet(edmEntitySet).build();

            // ?????????Id?????????.
            final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();

            // ??????????????????.
            EntityCollectionSerializerOptions.Builder builder = EntityCollectionSerializerOptions.with() //
                    .id(id).contextURL(conUrl);
            if (uriInfo.getCountOption() != null) {
                // $count ??????.
                final CountOptionImpl copt = new CountOptionImpl();
                copt.setValue(true);
                builder.count(copt);
            }
            if (uriInfo.getSelectOption() != null) {
                // $select ??????.
                final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, edmEntitySet.getName());

                if (eCollection.getEntities().size() == 0 //
                        || (entitySet.getFilterEqAutoSelect() == null || !entitySet.getFilterEqAutoSelect())) {
                    builder.select(uriInfo.getSelectOption());
                } else {
                    // [IY2151] DEBUG: filterEqAutoSelect: (experimental) Auto select property if
                    // `$filter` specify property with eq.
                    log.debug(OiyokanMessages.IY2151);
                    // ????????????EQ???????????????$select???????????????????????????????????????????????????????????????
                    String propNames = "";
                    for (int index = 0; index < eCollection.getEntities().size(); index++) {
                        for (Property prop : eCollection.getEntities().get(index).getProperties()) {
                            if (propNames.length() != 0) {
                                propNames += ",";
                            }
                            propNames += prop.getName();
                        }
                    }

                    try {
                        // uriInfo = new Parser(serviceMetadata.getEdm(), odata)
                        // .parseUri(request.getRawODataPath(), request.getRawQueryPath(), null,
                        // request.getRawBaseUri());
                        // System.err.println("RawQueryPath: " + request.getRawQueryPath());
                        // System.err.println("RawODataPath:" + request.getRawODataPath());
                        // System.err.println("RawBaseUri:" + request.getRawBaseUri());
                        final Parser parser = new Parser(serviceMetadata.getEdm(), odata);
                        final UriInfo uriInfoWrk = parser.parseUri(request.getRawODataPath(), "$select=" + propNames,
                                null, request.getRawBaseUri());
                        builder.select(uriInfoWrk.getSelectOption());
                    } catch (UriParserException ex) {
                        // [IY2111] UNEXPECTED: UriParserException occured.
                        log.error(OiyokanMessages.IY2111 + ": " + ex.toString());
                        throw new ODataApplicationException(OiyokanMessages.IY2111, //
                                OiyokanMessages.IY2111_CODE, Locale.ENGLISH);
                    } catch (UriValidationException ex) {
                        ex.printStackTrace();
                        // [IY2112] UNEXPECTED: UriValidationException occured..
                        log.error(OiyokanMessages.IY2112 + ": " + ex.toString());
                        throw new ODataApplicationException(OiyokanMessages.IY2112, //
                                OiyokanMessages.IY2112_CODE, Locale.ENGLISH);
                    }
                }
            }

            log.trace("OiyokanEntityCollectionProcessor#readEntityCollection: serializer.entityCollection: begin.");
            SerializerResult serResult = serializer.entityCollection( //
                    serviceMetadata, edmEntityType, eCollection, builder.build());
            log.trace("OiyokanEntityCollectionProcessor#readEntityCollection: serializer.entityCollection: end.");

            // OData ????????????????????????.
            response.setContent(serResult.getContent());
            response.setStatusCode(HttpStatusCode.OK.getStatusCode());
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
            log.trace("OiyokanEntityCollectionProcessor#readEntityCollection: end response.");
        } catch (ODataApplicationException | ODataLibraryException ex) {
            // [IY9521] WARN: EntityCollectionProcessor.readEntityCollection: exception
            // caught
            log.warn(OiyokanMessages.IY9521 + ": " + request.getRawODataPath() + "," + request.getRawQueryPath() + ": "
                    + ex.toString());
            throw ex;
        } catch (RuntimeException ex) {
            // [IY9522] ERROR: EntityCollectionProcessor.readEntityCollection: runtime
            // exception caught
            log.error(OiyokanMessages.IY9522 + ": " + request.getRawODataPath() + "," + request.getRawQueryPath() + ": "
                    + ex.toString(), ex);
            throw ex;
        }
    }

    private static final OiyokanEntityCollectionBuilderInterface getEntityCollectionBuilder(OiyoInfo oiyoInfo,
            EdmEntitySet edmEntitySet) throws ODataApplicationException {
        log.trace("OiyokanEntityCollectionProcessor#getEntityCollectionBuilder(" + edmEntitySet.getName() + ")");

        final OiyokanConstants.DatabaseType databaseType = OiyoInfoUtil.getOiyoDatabaseTypeByEntitySetName(oiyoInfo,
                edmEntitySet.getName());

        switch (databaseType) {
        case h2:
        case PostgreSQL:
        case MySQL:
        case SQLSV2008:
        case ORCL18:
        default:
            // ??????????????????????????????????????? BasicJdbcEntityCollectionBuilder ??????????????????
            return new OiyoBasicJdbcEntityCollectionBuilder(oiyoInfo);
        case BigQuery:
            // [IY9999] NOT IMPLEMENTED: Generic NOT implemented message.
            log.error(OiyokanMessages.IY9999);
            throw new ODataApplicationException(OiyokanMessages.IY9999, 500, Locale.ENGLISH);
        }
    }
}
