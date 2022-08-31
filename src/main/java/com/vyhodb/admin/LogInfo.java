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

package com.vyhodb.admin;

import java.util.UUID;

/**
 * Log Info descriptor.
 * <p>
 * Class represents log info fields.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public final class LogInfo {

    private static final String SEPARATOR = "-------------------------------------------------" + System.lineSeparator();

    private long _checkpoint;
    private long _dataLength;
    private UUID _logId;
    private UUID _masterLogId;
    private long _next;
    private boolean _slave;
    private long _start;
    private boolean _successfulStop;
    private short _version;

    /**
     * Serialization constructor.
     */
    @Deprecated
    public LogInfo() {
    }

    /**
     * Constructor.
     * 
     * @param logId
     * @param start
     * @param checkpoint
     * @param next
     * @param dataLength
     * @param slave
     * @param masterLogId
     * @param successfulStop
     * @param version
     */
    public LogInfo(UUID logId, long start, long checkpoint, long next, long dataLength, boolean slave, UUID masterLogId, boolean successfulStop, short version) {
        _logId = logId;
        _start = start;
        _checkpoint = checkpoint;
        _next = next;
        _dataLength = dataLength;
        _slave = slave;
        _masterLogId = masterLogId;
        _successfulStop = successfulStop;
        _version = version;
    }

    public long getCheckpoint() {
        return _checkpoint;
    }

    public long getDataLength() {
        return _dataLength;
    }

    public UUID getLogId() {
        return _logId;
    }

    public UUID getMasterLogId() {
        return _masterLogId;
    }

    public long getNext() {
        return _next;
    }

    public long getStart() {
        return _start;
    }

    public short getVersion() {
        return _version;
    }

    public boolean isSlave() {
        return _slave;
    }

    public boolean isSuccessfulStop() {
        return _successfulStop;
    }

    public String toFullString() {
        String ls = System.lineSeparator();

        StringBuilder builder = new StringBuilder();

        builder.append("Log info").append(ls).append(SEPARATOR).append("       next: ").append(_next).append(ls).append("      start: ").append(_start).append(ls).append(" checkpoint: ").append(_checkpoint).append(ls).append(" dataLength: ").append(_dataLength).append(ls).append(SEPARATOR).append("      logId: ").append(_logId).append(ls).append("    version: ").append(_version).append(ls).append(SEPARATOR).append("      slave: ").append(_slave).append(ls).append("masterLogId: ").append(_masterLogId).append(ls).append(SEPARATOR).append("successfulStop: ").append(_successfulStop).append(ls).append(SEPARATOR);

        return builder.toString();
    }

    @Override
    public String toString() {
        String ls = System.lineSeparator();

        StringBuilder builder = new StringBuilder();

        builder.append("Log info").append(ls).append(SEPARATOR).append("      logId: ").append(_logId).append(ls).append("    version: ").append(_version).append(ls).append(SEPARATOR).append("      slave: ").append(_slave).append(ls).append("masterLogId: ").append(_masterLogId).append(ls).append(SEPARATOR).append("successfulStop: ").append(_successfulStop).append(ls).append(SEPARATOR);

        return builder.toString();
    }
}
