/**
 * Copyright 2017 University of Patras 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and limitations under the License.
 */


package io.openslice.bugzilla.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    @JsonProperty("can_login")
    private Boolean canLogin;
    @JsonProperty("email")
    private String email;
    @JsonProperty("login")
    private String login;
    @JsonProperty("email_enabled")
    private Boolean emailEnabled;
    @JsonProperty("groups")
    private List<Group> groups = null;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("login_denied_text")
    private String loginDeniedText;
    @JsonProperty("name")
    private String name;
    @JsonProperty("password")
    private String password;
    @JsonProperty("real_name")
    private String realName;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("saved_reports")
    private List<Object> savedReports = null;
    @JsonProperty("saved_searches")
    private List<Object> savedSearches = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("can_login")
    public Boolean getCanLogin() {
        return canLogin;
    }

    @JsonProperty("can_login")
    public void setCanLogin(Boolean canLogin) {
        this.canLogin = canLogin;
    }
    
    @JsonProperty("login")
    public String getLogin() {
        return login;
    }

    @JsonProperty("login")
    public void setLogin(String login) {
        this.login = login;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("email_enabled")
    public Boolean getEmailEnabled() {
        return emailEnabled;
    }

    @JsonProperty("email_enabled")
    public void setEmailEnabled(Boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    @JsonProperty("groups")
    public List<Group> getGroups() {
        return groups;
    }

    @JsonProperty("groups")
    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("login_denied_text")
    public String getLoginDeniedText() {
        return loginDeniedText;
    }

    @JsonProperty("login_denied_text")
    public void setLoginDeniedText(String loginDeniedText) {
        this.loginDeniedText = loginDeniedText;
    }
    
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("real_name")
    public String getRealName() {
        return realName;
    }

    @JsonProperty("real_name")
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    @JsonProperty("full_name")
    public String getFulllName() {
        return fullName;
    }

    @JsonProperty("full_name")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @JsonProperty("saved_reports")
    public List<Object> getSavedReports() {
        return savedReports;
    }

    @JsonProperty("saved_reports")
    public void setSavedReports(List<Object> savedReports) {
        this.savedReports = savedReports;
    }

    @JsonProperty("saved_searches")
    public List<Object> getSavedSearches() {
        return savedSearches;
    }

    @JsonProperty("saved_searches")
    public void setSavedSearches(List<Object> savedSearches) {
        this.savedSearches = savedSearches;
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
