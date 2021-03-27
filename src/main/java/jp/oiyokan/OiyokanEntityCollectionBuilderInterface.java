package jp.oiyokan;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;

public interface OiyokanEntityCollectionBuilderInterface {
    /**
     * 指定のEDM要素セットに対応する要素コレクションを作成.
     * 
     * @param edmEntitySet EDM要素セット.
     * @param uriInfo      SQL構築のデータ構造.
     * @return 要素コレクション.
     * @throws ODataApplicationException OData App Exception occured.
     */
    public EntityCollection build(EdmEntitySet edmEntitySet, UriInfo uriInfo) throws ODataApplicationException;

}
