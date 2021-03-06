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

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

public final class ObjectList implements Serializable {
	
	private static final long serialVersionUID = 6785074165873456209L;

	@SerializedName("objects")
	private Set<FileObject> objects_;
				
	// For GSON
	public ObjectList() {
		objects_ = new TreeSet<>();
	}
	
	public Set<FileObject> getObjectList() {
		return new TreeSet<>(objects_);
	}
	
	// Straight from Eclipse
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((objects_ == null) ? 0 : objects_.hashCode());
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
		ObjectList other = (ObjectList) obj;
		if (objects_ == null) {
			if (other.objects_ != null)
				return false;
		} else if (!objects_.equals(other.objects_))
			return false;
		return true;
	}
	
}
