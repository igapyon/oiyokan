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
package jp.oiyokan.dto14;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * http://www.jsonschema2pojo.org/ を利用して自動生成.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "dbEntityName", "description", "databaseName", "canCreate", "canRead", "canUpdate",
        "canDelete", "entityType" })
@Generated("jsonschema2pojo")
public class Oiyo14SettingsEntitySet {

    @JsonProperty("name")
    private String name;
    @JsonProperty("dbEntityName")
    private String dbEntityName;
    @JsonProperty("description")
    private String description;
    @JsonProperty("databaseName")
    private String databaseName;
    @JsonProperty("canCreate")
    private Boolean canCreate;
    @JsonProperty("canRead")
    private Boolean canRead;
    @JsonProperty("canUpdate")
    private Boolean canUpdate;
    @JsonProperty("canDelete")
    private Boolean canDelete;
    @JsonProperty("entityType")
    private Oiyo14SettingsEntityType entityType;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("dbEntityName")
    public String getDbEntityName() {
        return dbEntityName;
    }

    @JsonProperty("dbEntityName")
    public void setDbEntityName(String dbEntityName) {
        this.dbEntityName = dbEntityName;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("databaseName")
    public String getDatabaseName() {
        return databaseName;
    }

    @JsonProperty("databaseName")
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @JsonProperty("canCreate")
    public Boolean getCanCreate() {
        return canCreate;
    }

    @JsonProperty("canCreate")
    public void setCanCreate(Boolean canCreate) {
        this.canCreate = canCreate;
    }

    @JsonProperty("canRead")
    public Boolean getCanRead() {
        return canRead;
    }

    @JsonProperty("canRead")
    public void setCanRead(Boolean canRead) {
        this.canRead = canRead;
    }

    @JsonProperty("canUpdate")
    public Boolean getCanUpdate() {
        return canUpdate;
    }

    @JsonProperty("canUpdate")
    public void setCanUpdate(Boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    @JsonProperty("canDelete")
    public Boolean getCanDelete() {
        return canDelete;
    }

    @JsonProperty("canDelete")
    public void setCanDelete(Boolean canDelete) {
        this.canDelete = canDelete;
    }

    @JsonProperty("entityType")
    public Oiyo14SettingsEntityType getEntityType() {
        return entityType;
    }

    @JsonProperty("entityType")
    public void setEntityType(Oiyo14SettingsEntityType entityType) {
        this.entityType = entityType;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}