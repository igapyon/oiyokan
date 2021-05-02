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
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * oiyokan-settings.json の entitySet セクション下の entityType オブジェクト。データベースのテーブルに相当。
 * 
 * http://www.jsonschema2pojo.org/ を利用して自動生成.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "dbName", "keyName", "property" })
@Generated("jsonschema2pojo")
public class OiyoSettingsEntityType {

    @JsonProperty("name")
    private String name;
    @JsonProperty("dbName")
    private String dbName;
    @JsonProperty("keyName")
    private List<String> keyName = null;
    @JsonProperty("property")
    private List<OiyoSettingsProperty> property = null;
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

    @JsonProperty("dbName")
    public String getDbName() {
        return dbName;
    }

    @JsonProperty("dbName")
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @JsonProperty("keyName")
    public List<String> getKeyName() {
        return keyName;
    }

    @JsonProperty("keyName")
    public void setKeyName(List<String> keyName) {
        this.keyName = keyName;
    }

    @JsonProperty("property")
    public List<OiyoSettingsProperty> getProperty() {
        return property;
    }

    @JsonProperty("property")
    public void setProperty(List<OiyoSettingsProperty> property) {
        this.property = property;
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