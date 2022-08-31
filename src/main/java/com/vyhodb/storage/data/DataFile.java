/*
 * MIT License
 *
 * Copyright (c) 2015-present Igor Vykhodtsev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.vyhodb.storage.data;

import com.vyhodb.server.PrimitiveUtils;
import com.vyhodb.storage.pagefile.Header;
import com.vyhodb.storage.pagefile.HeaderFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class DataFile extends HeaderFile {

    private final DataHeader _header;
    
    /**
     * Opens existed
     * 
     * @param fileName
     * @param exists
     * @param readOnly
     * @throws IOException
     */
    public DataFile(String fileName, boolean exists, boolean readOnly) throws IOException {
        super(fileName, exists, readOnly);
        
        _header = new DataHeader();
        if (exists) {
            readHeader(_header);
        }
    }
    
    /**
     * New Data File.
     * 
     * @param fileName
     * @param logId
     * @throws IOException
     */
    public DataFile(String fileName, UUID logId) throws IOException {
        super(fileName, false, false);
        
        _header = new DataHeader();
        _header.logId = logId;
        writeHeader(_header);
        fsync();
    }
    
    public UUID getLogId() {
        return _header.logId;
    }

    private static class DataHeader implements Header {

        private static final int HEADER_DATA_PREAMBULA = 858993459;
        
        public UUID logId;
        
        @Override
        public void read(ByteBuffer buffer) throws IOException {
            if (HEADER_DATA_PREAMBULA != buffer.getInt())
                throw new IOException("Wrong data file preambula. Specified data file is corrupted or isn't a proper data file.");
            
            logId = PrimitiveUtils.getUUID(buffer);
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(HEADER_DATA_PREAMBULA);
            PrimitiveUtils.putUUID(logId, buffer);
        }
        
    }
}
