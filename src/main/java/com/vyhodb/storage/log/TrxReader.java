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

package com.vyhodb.storage.log;

import com.vyhodb.storage.pagefile.Destination;
import com.vyhodb.storage.pagefile.PageHeader;
import com.vyhodb.storage.pagefile.Source;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class TrxReader {
    
    private final Source _source;
    private final Destination _dest;
    private final ByteBuffer _pageBuffer;
    
    public TrxReader(Source source, Destination dest, ByteBuffer pageBuffer)
    {
        _source = source;
        _dest = dest;
        _pageBuffer = pageBuffer;
    }
    
    public long readTrx(long restPages) throws TransactionCorruptedException, IOException
    {
        if (restPages == 0)
            return 0;
        
        long trxSize = 0;
        UUID trxId = null;
        
        do {
            if ((restPages - trxSize) <= 0)
                throw new TransactionCorruptedException("Can't reach STOP page. Rest pages:" + restPages + " , current trx size:" + trxSize);
                
            _source.read(_pageBuffer, 0, 1);
            
            if (! (PageHeader.isStop(_pageBuffer, 0) || PageHeader.isIntermediate(_pageBuffer, 0))) {
                throw new TransactionCorruptedException("Wrong page type. It is niether INTERMIDIATE nor STOP.");
            }
            
            if (trxId == null) {
                trxId = PageHeader.getTrxId(_pageBuffer, 0);
            }
            
            if (! trxId.equals(PageHeader.getTrxId(_pageBuffer, 0))) 
                throw new TransactionCorruptedException("Wrong trx id in page.");
            
            _dest.write(_pageBuffer, 0, 1);
            trxSize++;
        } 
        while (!PageHeader.isStop(_pageBuffer, 0));
        
        return trxSize;
    }
    
}
