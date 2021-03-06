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

import java.io.Serializable;
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
 * oiyokan-settings.json の entitySet セクション下の entityType オブジェクト下の property
 * オブジェクト。データベースの列に相当。
 * 
 * http://www.jsonschema2pojo.org/ を利用して自動生成.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "dbName", "edmType", "jdbcType", "dbType", "jdbcStream", "jdbcSetMethod", "autoGenKey",
        "nullable", "maxLength", "lengthFixed", "precision", "scale", "dbDefault", "filterTreatNullAsBlank" })
@Generated("jsonschema2pojo")
public class OiyoSettingsProperty implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    private String name;
    @JsonProperty("dbName")
    private String dbName;
    @JsonProperty("edmType")
    private String edmType;
    @JsonProperty("jdbcType")
    private String jdbcType;
    @JsonProperty("dbType")
    private String dbType;
    @JsonProperty("jdbcStream")
    private Boolean jdbcStream;
    @JsonProperty("jdbcSetMethod")
    private String jdbcSetMethod;
    @JsonProperty("autoGenKey")
    private Boolean autoGenKey;
    @JsonProperty("nullable")
    private Boolean nullable;
    @JsonProperty("maxLength")
    private Integer maxLength;
    @JsonProperty("lengthFixed")
    private Boolean lengthFixed;
    @JsonProperty("precision")
    private Integer precision;
    @JsonProperty("scale")
    private Integer scale;
    @JsonProperty("dbDefault")
    private String dbDefault;
    @JsonProperty("filterTreatNullAsBlank")
    private Boolean filterTreatNullAsBlank;
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

    @JsonProperty("edmType")
    public String getEdmType() {
        return edmType;
    }

    @JsonProperty("edmType")
    public void setEdmType(String edmType) {
        this.edmType = edmType;
    }

    @JsonProperty("jdbcType")
    public String getJdbcType() {
        return jdbcType;
    }

    @JsonProperty("jdbcType")
    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
    }

    @JsonProperty("dbType")
    public String getDbType() {
        return dbType;
    }

    @JsonProperty("dbType")
    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    @JsonProperty("jdbcStream")
    public Boolean getJdbcStream() {
        return jdbcStream;
    }

    @JsonProperty("jdbcStream")
    public void setJdbcStream(Boolean jdbcStream) {
        this.jdbcStream = jdbcStream;
    }

    @JsonProperty("jdbcSetMethod")
    public String getJdbcSetMethod() {
        return jdbcSetMethod;
    }

    @JsonProperty("jdbcSetMethod")
    public void setJdbcSetMethod(String jdbcSetMethod) {
        this.jdbcSetMethod = jdbcSetMethod;
    }

    @JsonProperty("autoGenKey")
    public Boolean getAutoGenKey() {
        return autoGenKey;
    }

    @JsonProperty("autoGenKey")
    public void setAutoGenKey(Boolean autoGenKey) {
        this.autoGenKey = autoGenKey;
    }

    @JsonProperty("nullable")
    public Boolean getNullable() {
        return nullable;
    }

    @JsonProperty("nullable")
    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    @JsonProperty("maxLength")
    public Integer getMaxLength() {
        return maxLength;
    }

    @JsonProperty("maxLength")
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    @JsonProperty("lengthFixed")
    public Boolean getLengthFixed() {
        return lengthFixed;
    }

    @JsonProperty("lengthFixed")
    public void setLengthFixed(Boolean lengthFixed) {
        this.lengthFixed = lengthFixed;
    }

    @JsonProperty("precision")
    public Integer getPrecision() {
        return precision;
    }

    @JsonProperty("precision")
    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    @JsonProperty("scale")
    public Integer getScale() {
        return scale;
    }

    @JsonProperty("scale")
    public void setScale(Integer scale) {
        this.scale = scale;
    }

    @JsonProperty("dbDefault")
    public String getDbDefault() {
        return dbDefault;
    }

    @JsonProperty("dbDefault")
    public void setDbDefault(String dbDefault) {
        this.dbDefault = dbDefault;
    }

    @JsonProperty("filterTreatNullAsBlank")
    public Boolean getFilterTreatNullAsBlank() {
        return filterTreatNullAsBlank;
    }

    @JsonProperty("filterTreatNullAsBlank")
    public void setFilterTreatNullAsBlank(Boolean filterTreatNullAsBlank) {
        this.filterTreatNullAsBlank = filterTreatNullAsBlank;
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
