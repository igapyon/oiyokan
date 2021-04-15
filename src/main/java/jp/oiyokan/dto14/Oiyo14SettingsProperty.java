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
@JsonPropertyOrder({ "name", "dbEntityName", "type", "jdbcType", "jdbcSetMethod", "nullable", "length", "fixLength",
        "scale", "precision" })
@Generated("jsonschema2pojo")
public class Oiyo14SettingsProperty {

    @JsonProperty("name")
    private String name;
    @JsonProperty("dbEntityName")
    private String dbEntityName;
    @JsonProperty("type")
    private String type;
    @JsonProperty("jdbcType")
    private String jdbcType;
    @JsonProperty("jdbcSetMethod")
    private String jdbcSetMethod;
    @JsonProperty("nullable")
    private Boolean nullable;
    @JsonProperty("length")
    private Integer length;
    @JsonProperty("fixLength")
    private Boolean fixLength;
    @JsonProperty("scale")
    private Object scale;
    @JsonProperty("precision")
    private Object precision;
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

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("jdbcType")
    public String getJdbcType() {
        return jdbcType;
    }

    @JsonProperty("jdbcType")
    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
    }

    @JsonProperty("jdbcSetMethod")
    public String getJdbcSetMethod() {
        return jdbcSetMethod;
    }

    @JsonProperty("jdbcSetMethod")
    public void setJdbcSetMethod(String jdbcSetMethod) {
        this.jdbcSetMethod = jdbcSetMethod;
    }

    @JsonProperty("nullable")
    public Boolean getNullable() {
        return nullable;
    }

    @JsonProperty("nullable")
    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    @JsonProperty("length")
    public Integer getLength() {
        return length;
    }

    @JsonProperty("length")
    public void setLength(Integer length) {
        this.length = length;
    }

    @JsonProperty("fixLength")
    public Boolean getFixLength() {
        return fixLength;
    }

    @JsonProperty("fixLength")
    public void setFixLength(Boolean fixLength) {
        this.fixLength = fixLength;
    }

    @JsonProperty("scale")
    public Object getScale() {
        return scale;
    }

    @JsonProperty("scale")
    public void setScale(Object scale) {
        this.scale = scale;
    }

    @JsonProperty("precision")
    public Object getPrecision() {
        return precision;
    }

    @JsonProperty("precision")
    public void setPrecision(Object precision) {
        this.precision = precision;
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