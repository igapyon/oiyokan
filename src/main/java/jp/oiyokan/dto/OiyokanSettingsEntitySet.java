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
package jp.oiyokan.dto;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * resources フォルダの oiyokan-settings.json の Javaインスタンス.
 * 
 * http://www.jsonschema2pojo.org/ を利用して自動生成.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "description", "entitySetName", "entityName", "databaseName", "dbTableNameLocal",
        "dbTableNameTarget" })
public class OiyokanSettingsEntitySet {
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("entitySetName")
    private String entitySetName;

    @JsonProperty("entityName")
    private String entityName;

    @JsonProperty("databaseName")
    private String databaseName;

    @JsonProperty("dbTableNameLocal")
    private String dbTableNameLocal;

    @JsonProperty("dbTableNameTarget")
    private String dbTableNameTarget;

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

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("entitySetName")
    public String getEntitySetName() {
        return entitySetName;
    }

    @JsonProperty("entitySetName")
    public void setEntitySetName(String entitySetName) {
        this.entitySetName = entitySetName;
    }

    @JsonProperty("entityName")
    public String getEntityName() {
        return entityName;
    }

    @JsonProperty("entityName")
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    @JsonProperty("databaseName")
    public String getDatabaseName() {
        return databaseName;
    }

    @JsonProperty("databaseName")
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @JsonProperty("dbTableNameLocal")
    public String getDbTableNameLocal() {
        return dbTableNameLocal;
    }

    @JsonProperty("dbTableNameLocal")
    public void setDbTableNameLocal(String dbTableNameLocal) {
        this.dbTableNameLocal = dbTableNameLocal;
    }

    @JsonProperty("dbTableNameTarget")
    public String getDbTableNameTarget() {
        return dbTableNameTarget;
    }

    @JsonProperty("dbTableNameTarget")
    public void setDbTableNameTarget(String dbTableNameTarget) {
        this.dbTableNameTarget = dbTableNameTarget;
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