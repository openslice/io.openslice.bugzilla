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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author ctranoris
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)

public class Comment {

//	@JsonProperty("id")
//    private int id;
	@JsonProperty("bug_id")
    private int bug_id;
	@JsonProperty("attachment_id")
    private int attachment_id;
	@JsonProperty("count")
    private int count;
	@JsonProperty("comment")
    private String comment;
	@JsonProperty("creator")
    private String creator;	
	@JsonProperty("time")
    private String time;
	@JsonProperty("creation_time")
    private String creation_time;
	@JsonProperty("is_private")
    private Boolean is_private;
	@JsonProperty("is_markdown")
    private Boolean is_markdown;
    @JsonProperty("tags")
    private List<String> tags = null;
    
//
//	@JsonProperty("id")
//	public int getId() {
//		return id;
//	}
//	
//	@JsonProperty("id")
//	public void setId(int id) {
//		this.id = id;
//	}
	
	@JsonProperty("bug_id")
	public int getBug_id() {
		return bug_id;
	}
	
	@JsonProperty("bug_id")
	public void setBug_id(int bug_id) {
		this.bug_id = bug_id;
	}

	@JsonProperty("attachment_id")
	public int getAttachment_id() {
		return attachment_id;
	}

	@JsonProperty("attachment_id")
	public void setAttachment_id(int attachment_id) {
		this.attachment_id = attachment_id;
	}

	@JsonProperty("count")
	public int getCount() {
		return count;
	}

	@JsonProperty("count")
	public void setCount(int count) {
		this.count = count;
	}

	@JsonProperty("comment")
	public String getComment() {
		return comment;
	}

	@JsonProperty("comment")
	public void setComment(String comment) {
		this.comment = comment;
	}

	@JsonProperty("creator")
	public String getCreator() {
		return creator;
	}

	@JsonProperty("creator")
	public void setCreator(String creator) {
		this.creator = creator;
	}

	@JsonProperty("time")
	public String getTime() {
		return time;
	}

	@JsonProperty("time")
	public void setTime(String time) {
		this.time = time;
	}
	

	@JsonProperty("creation_time")
	public String getCreation_time() {
		return creation_time;
	}

	@JsonProperty("creation_time")
	public void setCreation_time(String creation_time) {
		this.creation_time = creation_time;
	}

	@JsonProperty("is_private")
	public Boolean getIs_private() {
		return is_private;
	}

	@JsonProperty("is_private")
	public void setIs_private(Boolean is_private) {
		this.is_private = is_private;
	}

	@JsonProperty("is_markdown")
	public Boolean getIs_markdown() {
		return is_markdown;
	}

	@JsonProperty("is_markdown")
	public void setIs_markdown(Boolean is_markdown) {
		this.is_markdown = is_markdown;
	}
	
    @JsonProperty("tags")
	public List<String> getTags() {
		return tags;
	}
    
    @JsonProperty("tags")
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
    
    
    
	
}
