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
@JsonPropertyOrder({ "namespace", "containerName", "database", "entitySet" })
public class Oiyo13Settings {
    @JsonProperty("namespace")
    private String namespace;

    @JsonProperty("containerName")
    private String containerName;

    @JsonProperty("database")
    private List<Oiyo13SettingsDatabase> databaseList = null;

    @JsonProperty("entitySet")
    private List<Oiyo13SettingsEntitySet> entitySetList = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("namespace")
    public String getNamespace() {
        return namespace;
    }

    @JsonProperty("namespace")
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @JsonProperty("containerName")
    public String getContainerName() {
        return containerName;
    }

    @JsonProperty("containerName")
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    @JsonProperty("database")
    public List<Oiyo13SettingsDatabase> getDatabaseList() {
        return databaseList;
    }

    @JsonProperty("database")
    public void setDatabaseList(List<Oiyo13SettingsDatabase> databaseList) {
        this.databaseList = databaseList;
    }

    @JsonProperty("entitySet")
    public List<Oiyo13SettingsEntitySet> getEntitySetList() {
        return entitySetList;
    }

    @JsonProperty("entitySet")
    public void setEntitySetList(List<Oiyo13SettingsEntitySet> entitySetList) {
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