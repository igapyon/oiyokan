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
@JsonPropertyOrder({ "name", "type", "description", "jdbcDriver", "jdbcUrl", "jdbcUser", "jdbcPass" })
public class Oiyo13SettingsDatabase {
    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("jdbcDriver")
    private String jdbcDriver;

    @JsonProperty("jdbcUrl")
    private String jdbcUrl;

    @JsonProperty("jdbcUser")
    private String jdbcUser;

    @JsonProperty("jdbcPass")
    private String jdbcPass;

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

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("jdbcDriver")
    public String getJdbcDriver() {
        return jdbcDriver;
    }

    @JsonProperty("jdbcDriver")
    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    @JsonProperty("jdbcUrl")
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @JsonProperty("jdbcUrl")
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @JsonProperty("jdbcUser")
    public String getJdbcUser() {
        return jdbcUser;
    }

    @JsonProperty("jdbcUser")
    public void setJdbcUser(String jdbcUser) {
        this.jdbcUser = jdbcUser;
    }

    @JsonProperty("jdbcPass")
    public String getJdbcPass() {
        return jdbcPass;
    }

    @JsonProperty("jdbcPass")
    public void setJdbcPass(String jdbcPass) {
        this.jdbcPass = jdbcPass;
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