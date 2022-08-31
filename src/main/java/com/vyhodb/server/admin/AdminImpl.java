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

package com.vyhodb.server.admin;

import com.vyhodb.admin.*;
import com.vyhodb.admin.server.AdminClient;
import com.vyhodb.admin.server.Response;
import com.vyhodb.server.Server;
import com.vyhodb.server.TrxSpace;
import com.vyhodb.space.Record;
import com.vyhodb.storage.data.DataFile;
import com.vyhodb.storage.log.LogFile;
import com.vyhodb.storage.pagefile.BackupHeader;
import com.vyhodb.storage.pagefile.Destination;
import com.vyhodb.storage.pagefile.HeaderFile;
import com.vyhodb.storage.pagefile.PageHeader;
import com.vyhodb.utils.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

/**
 *
 * @author Igor Vykhodtcev
 */
public final class AdminImpl extends Admin {
    
    private final InetSocketAddress _localAddress;
    
    public AdminImpl() {
        _localAddress = new InetSocketAddress(0);
    }
    
    public AdminImpl(InetSocketAddress localAddress) {
        _localAddress = localAddress;
    }
    
    public void newStorage(String logFilename, String dataFilename) throws IOException
    {
        if (Utils.isEmpty(logFilename)) {
            throw new IllegalArgumentException("[logFilename] is empty");
        }
        if (Utils.isEmpty(dataFilename)) {
            throw new IllegalArgumentException("[dataFilename] is empty");
        }
        if (logFilename.equals(dataFilename))
            throw new IOException("log and data file names are equal!");
                
        Server storage = null;
        
        // Don't move line below into try-catch below !!!!!!
        // If files exists they will be removed !!!
        newStorageFiles(logFilename, dataFilename);
        
        try
        {
            Properties props = new Properties();
            props.setProperty("storage.log", logFilename);
            props.setProperty("storage.data", dataFilename);
            props.setProperty("storage.cacheSize", "0");
            props.setProperty("storage.modifyBufferSize", "5");
            props.setProperty("storage.logBufferSize", "5");
            
            storage = Server.start(props);
            
            TrxSpace space = storage.startModifyTrx();
            Record root = space.newRecord();
            assert(root.getId() == 0L);
            // Allocate large root record, so that it is placed
            // in the same page as descriptor and has enough space.
            byte[] filling = new byte[256];
            root.setField("filling", filling);
            space.commit();
            
            // Remove filling field
            space = storage.startModifyTrx();
            root = space.getRecord(0L);
            root.setField("filling", null);
            space.commit();
            
            storage.close();
        } 
        catch(Exception ex)
        {
            if (storage != null)
            {
                storage.close();
            }
            
            removeStorageFiles(logFilename, dataFilename);
            throw ex;
        }
    }
    
    private static void newStorageFiles(String logFilename, String dataFilename) throws IOException
    {
        UUID logId = null;
        
        // Log file
        try (LogFile logFile = new LogFile(logFilename)) 
        {
            logId = logFile.getLogId();
            logFile.syncHeader();
        }
        
        // Data file
        try (DataFile dataFile = new DataFile(dataFilename, logId)) 
        {
            // Creates and writes page 0.
            ByteBuffer emptyPage = ByteBuffer.allocate(PageHeader.PAGE_SIZE);
            PageHeader.emptyPage(emptyPage, 0);
            PageHeader.setPageId(0, emptyPage, 0);
            
            dataFile.position(0L);
            dataFile.write(emptyPage, 0, 1);
            dataFile.fsync();
        }
    }
    
    public void backup(String logFilename, String dataFilename, String backupFilename, int bufferSize, BackupListener listener) throws IOException
    {
        if (Utils.isEmpty(logFilename)) {
            throw new IllegalArgumentException("[logFilename] is empty");
        }
        if (Utils.isEmpty(dataFilename)) {
            throw new IllegalArgumentException("[dataFilename] is empty");
        }
        if (Utils.isEmpty(backupFilename)) {
            throw new IllegalArgumentException("[backupFilename] is empty");
        }
        if (bufferSize < 1) {
            throw new IllegalArgumentException("[bufferSize] must be > 0");
        }
        
        if (listener == null) {
            throw new IllegalArgumentException("[listener] is null");
        }
        
        ProgressListener dataListener = listener.getDataStageListener();
        ProgressListener logListener = listener.getLogStageListener();
        StageListener closeListener = listener.getCloseStageListener();
        if (dataListener == null) {
            throw new IllegalArgumentException("[listener.getDataProgressListener()] is null");
        }
        if (logListener == null) {
            throw new IllegalArgumentException("[listener.getLogProgressListener] is null");
        }
        if (closeListener == null) {
            throw new IllegalArgumentException("[listener.getCloseStageListener()] is null");
        }
        
        if (logFilename.equals(dataFilename))
            throw new IOException("log and data filenames are equal!");
        if (backupFilename.equals(logFilename))
            throw new IOException("backup and log filenames are equal!");
        if (backupFilename.equals(dataFilename))
            throw new IOException("backup and data filenames are equal!");
        
        if (Files.exists(Paths.get(backupFilename)))
            throw new IOException("Specified backup file already exists. Please specify another backup filename.");
        
        dataListener.started();
        
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize << PageHeader.PAGE_SIZE_MULTIPLICATOR);
        
        LogFile logFile = new LogFile(logFilename, true);
        DataFile dataFile = new DataFile(dataFilename, true, true);
        HeaderFile backupFile = new HeaderFile(backupFilename, false, false);
        
        // Checks storage
        {
            logFile.checkRecovery();
            logFile.checkDataHeader(dataFile.getLogId());
        }
        
        // Creates backup header
        BackupHeader backupHeader = new BackupHeader();
        backupHeader.start = logFile.getNext() - 1;
        backupHeader.masterLogId = logFile.getLogId();
        backupHeader.backupLength = logFile.getDataLength() + 1;
        
        // Copies pages from data to backup
        dataFile.position(0L);
        backupFile.position(0L);
        SourceProgress progress = new SourceProgress(dataFile, dataListener, logFile.getDataLength());
        backupFile.transferFrom(progress, buffer, logFile.getDataLength());
        
        // Copies last log page
        logFile.position(logFile.getCheckpoint() - 1);
        backupFile.position(backupHeader.backupLength - 1);
        backupFile.transferFrom(logFile, buffer, 1);
        
        // Closes data and log 
        logFile.close();
        dataFile.close();
        dataListener.completed();
        
        // Just to notify
        logListener.started();
        logListener.completed();
        
        // Writes backup header and closes backup 
        closeListener.started();
        backupFile.writeHeader(backupHeader);
        backupFile.fsync();
        backupFile.close();
        closeListener.completed();
    }
    
    public void restore(String backupFilename, String logFilename, String dataFilename, int bufferSize, boolean slave, ProgressListener listener) throws IOException
    {
        if (Utils.isEmpty(backupFilename)) {
            throw new IllegalArgumentException("[backupFilename] is empty");
        }
        if (Utils.isEmpty(logFilename)) {
            throw new IllegalArgumentException("[logFilename] is empty");
        }
        if (Utils.isEmpty(dataFilename)) {
            throw new IllegalArgumentException("[dataFilename] is empty");
        }
        if (bufferSize < 1) {
            throw new IllegalArgumentException("[bufferSize] must be > 0");
        }
        
        if (listener == null) {
            throw new IllegalArgumentException("[listener] is null");
        }
        
        if (logFilename.equals(dataFilename))
            throw new IOException("log and data filenames are equals!");
        if (backupFilename.equals(logFilename))
            throw new IOException("backup and log filenames are equal!");
        if (backupFilename.equals(dataFilename))
            throw new IOException("backup and data filenames are equal!");
        
        LogFile logFile = null;
        DataFile dataFile = null;
        HeaderFile backupFile = null;
        
        listener.started();
        
        try
        {
            ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize << PageHeader.PAGE_SIZE_MULTIPLICATOR);
            
            backupFile = new HeaderFile(backupFilename, false, true);
            
            // Reads backup header
            BackupHeader backupHeader = new BackupHeader();
            backupFile.readHeader(backupHeader);
            backupHeader.checkVersion();
            
            // Checks backup file length
            if (backupFile.size() != backupHeader.backupLength)
                throw new IOException("Backup file has wrong size. Backup file is corrupted.");
            
            // Creates log file
            logFile = new LogFile(logFilename, backupHeader, slave);
            
            // Creates data file
            dataFile = new DataFile(dataFilename, logFile.getLogId());
            
            // Copies pages from backup to data
            backupFile.position(0L);
            dataFile.position(0L);
            SourceProgress progress = new SourceProgress(backupFile, listener, logFile.getDataLength());
            dataFile.transferFrom(progress, buffer, logFile.getDataLength());
            
            // Copies last log page
            backupFile.position(logFile.getDataLength());
            logFile.position(logFile.getStart());
            logFile.transferFrom(backupFile, buffer, 1);
            
            // Sync data
            dataFile.fsync();
            
            // Writes log header
            logFile.syncHeader();
        }
        finally {
            if (backupFile != null) backupFile.close();
            if (dataFile != null) dataFile.close();
            if (logFile != null) logFile.close();
        }
        
        listener.completed();
    }
    
    private static void move(String from, String to) throws IOException
    {
        // Moves shrink file
        Path source = Paths.get(from);
        Path dest = Paths.get(to);
        
        Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
    }
    
    public LogInfo getLogInfo(String logFilename) throws IOException
    {
        if (Utils.isEmpty(logFilename)) {
            throw new IllegalArgumentException("[logFilename] is empty");
        }
        
        try (LogFile logFile = new LogFile(logFilename, true)) {
            return logFile.getLogInfo();
        }
    }
    
    public void remoteBackup(InetSocketAddress adminAddress, String backupFilename, int bufferSize, BackupListener listener) throws IOException, WrongPageIdException {
        if (Utils.isEmpty(backupFilename)) {
            throw new IllegalArgumentException("[backupFilename] is empty");
        }
        if (adminAddress == null) {
            throw new IllegalArgumentException("[adminAddress] is null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("[listener] is null");
        }
        if (bufferSize < 1) {
            throw new IllegalArgumentException("[bufferSize] must be > 0");
        }

        ProgressListener dataListener = listener.getDataStageListener();
        ProgressListener logListener = listener.getLogStageListener();
        StageListener closeListener = listener.getCloseStageListener();
        if (dataListener == null) {
            throw new IllegalArgumentException("[listener.getDataProgressListener()] is null");
        }
        if (logListener == null) {
            throw new IllegalArgumentException("[listener.getLogProgressListener] is null");
        }
        if (closeListener == null) {
            throw new IllegalArgumentException("[listener.getCloseStageListener()] is null");
        }
        
        HeaderFile backupFile = new HeaderFile(backupFilename, false);
        try (AdminClient client = new AdminClient(adminAddress, _localAddress)) {
            try {
                // Reads data
                dataListener.started();
                
                ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize << PageHeader.PAGE_SIZE_MULTIPLICATOR);
                
                backupFile.position(0L);
                Response response = client.readData();
                SourceProgress dataProgress = new SourceProgress(client, dataListener, response.dataLength);
                backupFile.transferFrom(dataProgress, buffer, response.dataLength);
                dataListener.completed();
                
                // Applies log
                long newNext = client.syncLog(response.next);
                long count = newNext - response.next;
                if (count > 0)
                {
                    logListener.started();
                    SourceProgress logProgress = new SourceProgress(client, logListener, count);
                    Destination applier = backupFile.getApplier();
                    applier.transferFrom(logProgress, buffer, count);
                    logListener.completed();
                }
    
                // Last log page
                client.readOneLogPage(newNext - 1);
                client.read(buffer, 0, 1);
                long dataLength = backupFile.size();
                backupFile.position(dataLength);
                backupFile.write(buffer, 0, 1);
                
                // Writes backupHeader
                BackupHeader header = new BackupHeader();
                header.masterLogId = response.masterLogId;
                header.backupLength = dataLength + 1;      // because of last log page
                header.start = newNext - 1;                // because we will restore last log page
                backupFile.writeHeader(header);
                
                // Fsync and close
                closeListener.started();
                backupFile.fsync();
                closeListener.completed();
            }
            finally {
                if (backupFile != null) {
                    backupFile.close();
                }
            }
        }
    }
    
    public void clearSlave(String logFilename) throws IOException
    {
        try (LogFile logFile = new LogFile(logFilename, true)) {
            logFile.checkRecovery();
            logFile.clearSlave();
        }
    }
    
    public void remoteClearSlave(InetSocketAddress adminAddress) throws IOException
    {
        if (adminAddress == null) {
            throw new IllegalArgumentException("[adminAddress] is null");
        }
        
        try (AdminClient server = new AdminClient(adminAddress, _localAddress)) {
            server.clearSlave();
        }
    }
    
    public void remoteStorageClose(InetSocketAddress adminAddress, long storageCloseTimeout) throws IOException
    {
        if (adminAddress == null) {
            throw new IllegalArgumentException("[adminAddress] is null");
        }
        if (storageCloseTimeout < 500) {
            throw new IllegalArgumentException("[storageCloseTimeout] must be >= 500");
        }
        
        try (AdminClient server = new AdminClient(adminAddress, _localAddress)) {
            server.storageClose(storageCloseTimeout);
        }
    }
    
    public void remoteShrink(InetSocketAddress adminAddress, ShrinkListener listener) throws IOException, WrongPageIdException
    {
        if (adminAddress == null) {
            throw new IllegalArgumentException("[adminAddress] is null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("[listener] is null");
        }
        
        try (AdminClient server = new AdminClient(adminAddress, _localAddress)) {
            listener.started();
            server.shrink(Long.MIN_VALUE);
            listener.completed();
        }
    }
    
    private void shrink(String logFilename, long shrinkStartPageId, int bufferSize) throws IOException, WrongPageIdException
    {
        // Opens log file for validation
        try (LogFile logFile = new LogFile(logFilename, true)) {
            logFile.checkRecovery();
            
            if (shrinkStartPageId == Long.MIN_VALUE) {
                shrinkStartPageId = logFile.getNext();
            }
            
            // Checks start page
            if (shrinkStartPageId > logFile.getNext())
                throw new WrongPageIdException("Can't start shrink. Shrink start page > next. Next:" + logFile.getNext() + ", shrink start page:" + shrinkStartPageId);
            if ((shrinkStartPageId - 1) < logFile.getStart())
                throw new WrongPageIdException("Can't start shrink. Shrink start page must be more then start. Start:" + logFile.getStart() + ", shrink start page:" + shrinkStartPageId);
        }

        long start = shrinkStartPageId - 1;
                
        // Allocates buffer
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize << PageHeader.PAGE_SIZE_MULTIPLICATOR);
        
        LogFile newLogFile = commonShrink(start, logFilename, buffer);
        newLogFile.setSuccessfulStop(true);
        newLogFile.syncHeader();
        newLogFile.close();
    }
    
    public static LogFile commonShrink(long start, String logFilename, ByteBuffer buffer) throws IOException {
        final String oldLogFilename = logFilename + ".old";
        
        // Renames old log file and opens it
        AdminImpl.move(logFilename, oldLogFilename);
        LogFile oldLog = new LogFile(oldLogFilename, true);
        
        // Creates new log file
        LogFile newLogFile = new LogFile(logFilename, oldLog);
        newLogFile.setStart(start);
        newLogFile.setSuccessfulStop(false);

        // Copies pages
        oldLog.position(start);
        newLogFile.position(start);
        newLogFile.transferFrom(oldLog, buffer, newLogFile.getNext() - start);
        newLogFile.fsync();
        newLogFile.syncHeader();
        
        // Closes and removes old log file
        oldLog.close();
        Files.delete(Paths.get(oldLogFilename));
        
        return newLogFile;
    }
    
    public LogInfo remoteGetLogInfo(InetSocketAddress adminAddress) throws IOException
    {
        if (adminAddress == null) {
            throw new IllegalArgumentException("[adminAddress] is null");
        }
        
        try (AdminClient server = new AdminClient(adminAddress, _localAddress)) {
           return server.getLogInfo();
        }
    }
    
    public void remotePingAdmin(InetSocketAddress adminAddress) throws IOException
    {
        if (adminAddress == null) {
            throw new IllegalArgumentException("[adminAddress] is null");
        }
        
        try (AdminClient server = new AdminClient(adminAddress, _localAddress)) {
            server.ping();
        }
    }

    @Override
    public void shrink(String logFilename, int bufferSize, ShrinkListener listener) throws IOException {
        if (Utils.isEmpty(logFilename)) {
            throw new IllegalArgumentException("[logFilename] is empty");
        }
        if (bufferSize < 1) {
            throw new IllegalArgumentException("[bufferSize] must be > 0");
        }
        if (listener == null) {
            throw new IllegalArgumentException("[listener] is null");
        }
        
        listener.started();
        shrink(logFilename, Long.MIN_VALUE, bufferSize);
        listener.completed();
    }

    @Override
    public void shrinkMaster(String masterLogFilename, int bufferSize, Collection<InetSocketAddress> slaves, ShrinkListener listener) throws IOException {
        if (Utils.isEmpty(masterLogFilename)) {
            throw new IllegalArgumentException("[masterLogFilename] is empty");
        }
        if (bufferSize < 1) {
            throw new IllegalArgumentException("[bufferSize] must be > 0");
        }
        if (slaves == null) {
            throw new IllegalArgumentException("[slaves] is null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("[listener] is null");
        }
        
        LogInfo masterLogInfo = getLogInfo(masterLogFilename);
        
        long shrinkStart = getMinNext(masterLogInfo.getNext(), _localAddress, slaves, listener);
        
        listener.started();
        shrink(masterLogFilename, shrinkStart, bufferSize);
        listener.completed();
    }

    @Override
    public void remoteShrinkMaster(InetSocketAddress masterAdminAddress, Collection<InetSocketAddress> slaves,  ShrinkListener listener) throws IOException {
        if (masterAdminAddress == null) {
            throw new IllegalArgumentException("[masterAdminAddress] is null");
        }
        if (slaves == null) {
            throw new IllegalArgumentException("[slaves] is null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("[listener] is null");
        }
        
        try (AdminClient server = new AdminClient(masterAdminAddress, _localAddress)) {
            LogInfo masterLogInfo = server.getLogInfo(); 
            
            long shrinkStart = getMinNext(masterLogInfo.getNext(), _localAddress, slaves, listener);
            
            listener.started();
            server.shrink(shrinkStart);
            listener.completed();
        }
    }

    private static long getMinNext(long next, InetSocketAddress localAddress, Collection<InetSocketAddress> slaves, ShrinkListener listener) throws IOException {
        if (slaves.isEmpty()) {
            throw new IOException("Slave list is empty! Shrink is interrupted.");
        }
        
        for (InetSocketAddress slaveAddress : slaves) {
            try (AdminClient adminClient = new AdminClient(slaveAddress, localAddress)) {
                LogInfo logInfo = adminClient.getLogInfo();
                if (logInfo.getNext() < next) {
                    next = logInfo.getNext();
                }
                listener.slaveQueried(slaveAddress.toString());
            }
        }
        
        return next;
    }
}
