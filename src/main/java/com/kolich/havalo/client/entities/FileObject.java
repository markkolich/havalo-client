/**
 * Copyright (c) 2012 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.havalo.client.entities;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public final class FileObject implements Serializable {
	
	private static final long serialVersionUID = -4070962176507989584L;

	@SerializedName("name")
	private String name_;
	
	@SerializedName("headers")
	private Map<String, List<String>> headers_;
	
	// For GSON
	public FileObject() {
		headers_ = new LinkedHashMap<String, List<String>>();
	}
	
	public String getName() {
		return name_;
	}
	
	public FileObject setName(String name) {
		name_ = name;
		return this;
	}
	
	public Map<String, List<String>> getHeaders() {
		return headers_;
	}
	
	public FileObject setHeaders(Map<String, List<String>> headers) {
		headers_ = headers;
		return this;
	}
	
	public List<String> getHeaders(String name) {
		return headers_.get(name);
	}
	
	public String getFirstHeader(String name) {
		final List<String> headers;
		if((headers = getHeaders(name)) != null) {
			return headers.get(0);
		}
		return null;
	}

	// Straight from Eclipse
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name_ == null) ? 0 : name_.hashCode());
		return result;
	}

	// Straight from Eclipse
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileObject other = (FileObject) obj;
		if (name_ == null) {
			if (other.name_ != null)
				return false;
		} else if (!name_.equals(other.name_))
			return false;
		return true;
	}
	
}
