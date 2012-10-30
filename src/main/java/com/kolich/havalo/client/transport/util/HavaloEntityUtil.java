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

package com.kolich.havalo.client.transport.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.util.ByteArrayBuffer;

/**
 * Entity processor that limits the amount of data shoved into
 * memory to 1MB or less.
 */
public final class HavaloEntityUtil implements HavaloHttpEntityUtil {
	
	/**
	 * Reads at most 1MB into memory by default.
	 */
	private static final int ONE_MB_IN_BYTES = 1048576; // 1MB
	
	/**
	 * The input read buffer size.
	 */
	private static final int READ_BUFFER_SIZE = 4096; // 4KB
	
	/**
	 * The number of bytes to read from the response stream and
	 * into memory.  Default is 1MB.
	 */
	private int maxBytesToRead_ = ONE_MB_IN_BYTES;

	@Override
	public byte[] toByteArray(final HttpEntity entity) throws IOException {
		return toByteArray(entity, maxBytesToRead_);
	}
	
	/**
     * Read the contents of an entity and return it as a byte array.
     * 
     * @param entity
     * @return byte array containing the entity content. May be empty; never null.
     * @throws IOException if an error occurs reading the input stream
     * @throws IllegalArgumentException if entity is null or if content
     * length > Integer.MAX_VALUE
     */
    private static byte[] toByteArray(final HttpEntity entity,
    	final int maxBytesToRead) throws IOException {
        InputStream is = entity.getContent();
        if(is == null) {
            return new byte[] {};
        }
        if(entity.getContentLength() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("HTTP entity too " +
            	"large to be buffered in memory.");
        }
        int i = (int)entity.getContentLength();
        if(i < 0) {
            i = 4096;
        }
        final ByteArrayBuffer buffer = new ByteArrayBuffer(i);
        try {
            byte[] tmp = new byte[READ_BUFFER_SIZE];
            int totalRead = 0;
            int l = 0;
            while((l = is.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
                // Only read at most the number of bytes
                // specified into memory.
                totalRead += l;
                if(totalRead >= maxBytesToRead) {
                	break;
                }
            }
        } finally {
            IOUtils.closeQuietly(is);
        }
        return buffer.toByteArray();
    }
    
    public void setMaxBytesToRead(int maxBytesToRead) {
    	maxBytesToRead_ = maxBytesToRead;
    }
	
}
