/*-
 * ========================LICENSE_START=================================
 * io.openslice.bugzilla
 * %%
 * Copyright (C) 2019 openslice.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
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

/**
 * @author ctranoris
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)

public class Bug {

    @JsonProperty("alias")
    private List<Object> alias = null;
    @JsonProperty("assigned_to")
    private String assignedTo;
    @JsonProperty("assigned_to_detail")
    private AssignedToDetail assignedToDetail;
    @JsonProperty("blocks")
    private List<Object> blocks = null;
    @JsonProperty("cc")
    private List<String> cc = null;
    @JsonProperty("cc_detail")
    private List<CcDetail> ccDetail = null;
    @JsonProperty("classification")
    private String classification;
    @JsonProperty("component")
    private String component;
    @JsonProperty("comment")
    private Comment comment;
    @JsonProperty("creation_time")
    private String creationTime;
    @JsonProperty("creator")
    private String creator;
    @JsonProperty("creator_detail")
    private CreatorDetail creatorDetail;
    @JsonProperty("deadline")
    private Object deadline;
    @JsonProperty("depends_on")
    private List<Object> dependsOn = null;
    @JsonProperty("dupe_of")
    private Object dupeOf;
    @JsonProperty("flags")
    private List<Object> flags = null;
    @JsonProperty("groups")
    private List<Object> groups = null;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("is_cc_accessible")
    private Boolean isCcAccessible;
    @JsonProperty("is_confirmed")
    private Boolean isConfirmed;
    @JsonProperty("is_creator_accessible")
    private Boolean isCreatorAccessible;
    @JsonProperty("is_open")
    private Boolean isOpen;
    @JsonProperty("keywords")
    private List<Object> keywords = null;
    @JsonProperty("last_change_time")
    private String lastChangeTime;
    @JsonProperty("op_sys")
    private String opSys;
    @JsonProperty("platform")
    private String platform;
    @JsonProperty("priority")
    private String priority;
    @JsonProperty("product")
    private String product;
    @JsonProperty("qa_contact")
    private String qaContact;
    @JsonProperty("resolution")
    private String resolution;
    @JsonProperty("see_also")
    private List<Object> seeAlso = null;
    @JsonProperty("severity")
    private String severity;
    @JsonProperty("status")
    private String status;
    @JsonProperty("summary")
    private String summary;
    @JsonProperty("description")
    private String description;
    @JsonProperty("target_milestone")
    private String targetMilestone;
    @JsonProperty("url")
    private String url;
    @JsonProperty("version")
    private String version;
    @JsonProperty("whiteboard")
    private String whiteboard;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("alias")
    public List<Object> getAlias() {
        return alias;
    }

    @JsonIgnore
    public String getAliasFirst() {
    	
    	if ( alias!= null ) {    	
    		if ( alias.size() >0 ) {
    			return (String) alias.get(0).toString();
    		}
    	}
        
        return "";
    }
    
    
    
    @JsonProperty("alias")
    public void setAlias(List<Object> alias) {
        this.alias = alias;
    }

    @JsonProperty("assigned_to")
    public String getAssignedTo() {
        return assignedTo;
    }

    @JsonProperty("assigned_to")
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    @JsonProperty("assigned_to_detail")
    public AssignedToDetail getAssignedToDetail() {
        return assignedToDetail;
    }

    @JsonProperty("assigned_to_detail")
    public void setAssignedToDetail(AssignedToDetail assignedToDetail) {
        this.assignedToDetail = assignedToDetail;
    }

    @JsonProperty("blocks")
    public List<Object> getBlocks() {
        return blocks;
    }

    @JsonProperty("blocks")
    public void setBlocks(List<Object> blocks) {
        this.blocks = blocks;
    }

    @JsonProperty("cc")
    public List<String> getCc() {
        return cc;
    }

    @JsonProperty("cc")
    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    @JsonProperty("cc_detail")
    public List<CcDetail> getCcDetail() {
        return ccDetail;
    }

    @JsonProperty("cc_detail")
    public void setCcDetail(List<CcDetail> ccDetail) {
        this.ccDetail = ccDetail;
    }

    @JsonProperty("classification")
    public String getClassification() {
        return classification;
    }

    @JsonProperty("classification")
    public void setClassification(String classification) {
        this.classification = classification;
    }

    @JsonProperty("component")
    public String getComponent() {
        return component;
    }

    @JsonProperty("component")
    public void setComponent(String component) {
        this.component = component;
    }
    
    @JsonProperty("comment")
    public Comment getComment() {
        return comment;
    }

    @JsonProperty("comment")
    public void setComment(Comment comment) {
        this.comment = comment;
    }

    @JsonProperty("creation_time")
    public String getCreationTime() {
        return creationTime;
    }

    @JsonProperty("creation_time")
    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    @JsonProperty("creator")
    public String getCreator() {
        return creator;
    }

    @JsonProperty("creator")
    public void setCreator(String creator) {
        this.creator = creator;
    }

    @JsonProperty("creator_detail")
    public CreatorDetail getCreatorDetail() {
        return creatorDetail;
    }

    @JsonProperty("creator_detail")
    public void setCreatorDetail(CreatorDetail creatorDetail) {
        this.creatorDetail = creatorDetail;
    }

    @JsonProperty("deadline")
    public Object getDeadline() {
        return deadline;
    }

    @JsonProperty("deadline")
    public void setDeadline(Object deadline) {
        this.deadline = deadline;
    }

    @JsonProperty("depends_on")
    public List<Object> getDependsOn() {
        return dependsOn;
    }

    @JsonProperty("depends_on")
    public void setDependsOn(List<Object> dependsOn) {
        this.dependsOn = dependsOn;
    }

    @JsonProperty("dupe_of")
    public Object getDupeOf() {
        return dupeOf;
    }

    @JsonProperty("dupe_of")
    public void setDupeOf(Object dupeOf) {
        this.dupeOf = dupeOf;
    }

    @JsonProperty("flags")
    public List<Object> getFlags() {
        return flags;
    }

    @JsonProperty("flags")
    public void setFlags(List<Object> flags) {
        this.flags = flags;
    }

    @JsonProperty("groups")
    public List<Object> getGroups() {
        return groups;
    }

    @JsonProperty("groups")
    public void setGroups(List<Object> groups) {
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

    @JsonProperty("is_cc_accessible")
    public Boolean getIsCcAccessible() {
        return isCcAccessible;
    }

    @JsonProperty("is_cc_accessible")
    public void setIsCcAccessible(Boolean isCcAccessible) {
        this.isCcAccessible = isCcAccessible;
    }

    @JsonProperty("is_confirmed")
    public Boolean getIsConfirmed() {
        return isConfirmed;
    }

    @JsonProperty("is_confirmed")
    public void setIsConfirmed(Boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    @JsonProperty("is_creator_accessible")
    public Boolean getIsCreatorAccessible() {
        return isCreatorAccessible;
    }

    @JsonProperty("is_creator_accessible")
    public void setIsCreatorAccessible(Boolean isCreatorAccessible) {
        this.isCreatorAccessible = isCreatorAccessible;
    }

    @JsonProperty("is_open")
    public Boolean getIsOpen() {
        return isOpen;
    }

    @JsonProperty("is_open")
    public void setIsOpen(Boolean isOpen) {
        this.isOpen = isOpen;
    }

    @JsonProperty("keywords")
    public List<Object> getKeywords() {
        return keywords;
    }

    @JsonProperty("keywords")
    public void setKeywords(List<Object> keywords) {
        this.keywords = keywords;
    }

    @JsonProperty("last_change_time")
    public String getLastChangeTime() {
        return lastChangeTime;
    }

    @JsonProperty("last_change_time")
    public void setLastChangeTime(String lastChangeTime) {
        this.lastChangeTime = lastChangeTime;
    }

    @JsonProperty("op_sys")
    public String getOpSys() {
        return opSys;
    }

    @JsonProperty("op_sys")
    public void setOpSys(String opSys) {
        this.opSys = opSys;
    }

    @JsonProperty("platform")
    public String getPlatform() {
        return platform;
    }

    @JsonProperty("platform")
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @JsonProperty("priority")
    public String getPriority() {
        return priority;
    }

    @JsonProperty("priority")
    public void setPriority(String priority) {
        this.priority = priority;
    }

    @JsonProperty("product")
    public String getProduct() {
        return product;
    }

    @JsonProperty("product")
    public void setProduct(String product) {
        this.product = product;
    }

    @JsonProperty("qa_contact")
    public String getQaContact() {
        return qaContact;
    }

    @JsonProperty("qa_contact")
    public void setQaContact(String qaContact) {
        this.qaContact = qaContact;
    }

    @JsonProperty("resolution")
    public String getResolution() {
        return resolution;
    }

    @JsonProperty("resolution")
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    @JsonProperty("see_also")
    public List<Object> getSeeAlso() {
        return seeAlso;
    }

    @JsonProperty("see_also")
    public void setSeeAlso(List<Object> seeAlso) {
        this.seeAlso = seeAlso;
    }

    @JsonProperty("severity")
    public String getSeverity() {
        return severity;
    }

    @JsonProperty("severity")
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("summary")
    public String getSummary() {
        return summary;
    }

    @JsonProperty("summary")
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }
    
    

    @JsonProperty("target_milestone")
    public String getTargetMilestone() {
        return targetMilestone;
    }

    @JsonProperty("target_milestone")
    public void setTargetMilestone(String targetMilestone) {
        this.targetMilestone = targetMilestone;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("whiteboard")
    public String getWhiteboard() {
        return whiteboard;
    }

    @JsonProperty("whiteboard")
    public void setWhiteboard(String whiteboard) {
        this.whiteboard = whiteboard;
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
