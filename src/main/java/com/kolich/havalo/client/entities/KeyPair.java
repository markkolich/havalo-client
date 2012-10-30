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
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

public final class KeyPair implements Serializable {
	
	private static final long serialVersionUID = 6048441078878191903L;

	@SerializedName("key")
	private UUID key_;
	
	@SerializedName("secret")
	private String secret_;
			
	// For GSON
	public KeyPair() {}
		
	public UUID getKey() {
		return key_;
	}
	
	public KeyPair setKey(UUID key) {
		key_ = key;
		return this;
	}
	
	public KeyPair setKey(String key) {
		key_ = UUID.fromString(key);
		return this;
	}

	public String getSecret() {
		return secret_;
	}

	public KeyPair setSecret(String secret) {
		secret_ = secret;
		return this;
	}

	// Straight from Eclipse
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key_ == null) ? 0 : key_.hashCode());
		result = prime * result + ((secret_ == null) ? 0 : secret_.hashCode());
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
		KeyPair other = (KeyPair) obj;
		if (key_ == null) {
			if (other.key_ != null)
				return false;
		} else if (!key_.equals(other.key_))
			return false;
		if (secret_ == null) {
			if (other.secret_ != null)
				return false;
		} else if (!secret_.equals(other.secret_))
			return false;
		return true;
	}
	
}
