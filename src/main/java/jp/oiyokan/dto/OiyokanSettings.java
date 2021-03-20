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
@JsonPropertyOrder({ "databaseList", "entitySetList" })
public class OiyokanSettings {
    @JsonProperty("databaseList")
    private List<OiyokanSettingsDatabase> databaseList = null;
    @JsonProperty("entitySetList")
    private List<OiyokanSettingsEntitySet> entitySetList = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("databaseList")
    public List<OiyokanSettingsDatabase> getDatabaseList() {
        return databaseList;
    }

    @JsonProperty("databaseList")
    public void setDatabaseList(List<OiyokanSettingsDatabase> databaseList) {
        this.databaseList = databaseList;
    }

    @JsonProperty("entitySetList")
    public List<OiyokanSettingsEntitySet> getEntitySetList() {
        return entitySetList;
    }

    @JsonProperty("entitySetList")
    public void setEntitySetList(List<OiyokanSettingsEntitySet> entitySetList) {
        this.entitySetList = entitySetList;
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