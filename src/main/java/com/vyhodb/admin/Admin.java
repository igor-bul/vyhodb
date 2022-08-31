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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * This class represents "Admin API" and provides methods for vyhodb
 * administrative tasks.
 * <p>
 * Methods {@linkplain #getInstance()} are used to get instance of Admin API.
 * <p>
 * All methods are split into two groups:
 * <ol>
 * <li>Method which operates on closed vyhodb storage (data and log files).
 * Vyhodb server should not be running.</li>
 * <li>Methods with <b>remote</b> prefix. These methods operate on remote
 * running vyhodb server by establishing network connection to <b>Admin
 * component</b>. <b>Admin component</b> on remote vyhodb server must be turned
 * on.</li>
 * </ol>
 * 
 * <p>
 * <b>Thread safety</b>
 * <p>
 * Implementations of this class are thread safe.
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public abstract class Admin {

    private static final String DEFAULT_ADMIN_IMPL_CLASS = "com.vyhodb.server.admin.AdminImpl";

    /**
     * Returns Admin API implementation instance.
     * 
     * @return Admin API object
     */
    public static Admin getInstance() {
        try {
            Class<?> adminImplClass = Class.forName(DEFAULT_ADMIN_IMPL_CLASS);
            Constructor<?> constructor = adminImplClass.getConstructor();
            return (Admin) constructor.newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Can't instantiate Admin.", ex);
        }
    }

    /**
     * Returns Admin API implementation instance.
     * 
     * @param localAddress
     *            specifies local socket address which is by default used to
     *            establish network connections to remote Admin component.
     * @return Admin API object
     */
    public static Admin getInstance(InetSocketAddress localAddress) {
        if (localAddress == null) {
            throw new IllegalArgumentException("localAddress == null");
        }

        try {
            Class<?> adminImplClass = Class.forName(DEFAULT_ADMIN_IMPL_CLASS);
            Constructor<?> constructor = adminImplClass.getConstructor(InetSocketAddress.class);
            return (Admin) constructor.newInstance(localAddress);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Can't instantiate Admin.", ex);
        }
    }

    /**
     * Creates backup file of vyhodb storage.
     * <p>
     * Vyhodb storage (data and log files) should be closed (no running vyhodb
     * server).
     * 
     * @see ConsoleBackupListener
     * @see DummyBackupListener
     * 
     * @param logFilename
     *            path to log file
     * @param dataFilename
     *            path to data file
     * @param backupFilename
     *            path to backup file
     * @param bufferSize
     *            buffer size (in pages) used for page copying
     * @param listener
     *            backup progress listener
     * @throws IOException
     */
    public abstract void backup(String logFilename, String dataFilename, String backupFilename, int bufferSize, BackupListener listener) throws IOException;

    /**
     * Clears slave flag and turns slave storage into normal operation mode.
     * <p>
     * Storage must be closed (no running vyhodb server).
     * 
     * @param logFilename
     *            path to log file
     * @throws IOException
     */
    public abstract void clearSlave(String logFilename) throws IOException;

    /**
     * Returns Log Info of closed storage.
     * 
     * @param logFilename
     *            path to log file
     * @return log info
     * @throws IOException
     */
    public abstract LogInfo getLogInfo(String logFilename) throws IOException;

    /**
     * Creates new vyhodb storage.
     * 
     * @param logFilename
     *            path to new log file
     * @param dataFilename
     *            path to new data file
     * @throws IOException
     */
    public abstract void newStorage(String logFilename, String dataFilename) throws IOException;

    /**
     * Creates backup file of running vyhodb server.
     * <p>
     * Backup file is created on local system, where method is invoked.
     * 
     * @see DummyBackupListener
     * @see ConsoleBackupListener
     * 
     * @param adminAddress
     *            socket address of vyhodb server's Admin component
     * @param backupFilename
     *            local path to backup file
     * @param bufferSize
     *            buffer size (in pages)
     * @param listener
     *            backup progress listener
     * @throws IOException
     */
    public abstract void remoteBackup(InetSocketAddress adminAddress, String backupFilename, int bufferSize, BackupListener listener) throws IOException;

    /**
     * Clears slave flag on running vyhodb slave server.
     * <p>
     * Slave Replication Agent component will be automatically stopped and
     * vyhodb server will be able to process Modify transactions.
     * 
     * @param adminAddress
     *            socket address of vyhodb server's Admin component
     * @throws IOException
     */
    public abstract void remoteClearSlave(InetSocketAddress adminAddress) throws IOException;

    /**
     * Returns Log Info of running vyhodb server.
     * 
     * @param adminAddress
     *            socket address of Admin Component.
     * @return log info structure
     * @throws IOException
     */
    public abstract LogInfo remoteGetLogInfo(InetSocketAddress adminAddress) throws IOException;

    /**
     * Sends ping to admin component of running vyhodb server.
     * 
     * @param adminAddress
     *            socket address of Admin Component.
     * @throws IOException
     *             if Admin component isn't started or vyhodb server is
     *             unavailable.
     */
    public abstract void remotePingAdmin(InetSocketAddress adminAddress) throws IOException;

    /**
     * Shrinks log file of running vyhodb server.
     * <p>
     * Removes old transactions from log file and decreases it size.
     * <p>
     * <b>Note! If vyhodb server plays Master role has slave replication servers
     * (storages), then
     * {@link #remoteShrinkMaster(InetSocketAddress, Collection, ShrinkListener)}
     * must be used. Otherwise, slave servers will get out of sync.</b>
     * 
     * @see ConsoleShrinkListener
     * 
     * @param adminAddress
     *            socket address of vyhodb server's Admin Component
     * @param listener
     *            shrink
     * @throws IOException
     */
    public abstract void remoteShrink(InetSocketAddress adminAddress, ShrinkListener listener) throws IOException;

    /**
     * Shrinks log file of running Master vyhodb server.
     * <p>
     * Method asks slave servers in order to find out which transactions haven't
     * been replicated yet. It uses this information to remove only those
     * transactions which have already been replicated to specified slave
     * servers, so that non-replicated transactions could leave in log file.
     * 
     * @see ConsoleShrinkListener
     * 
     * @param adminAddress
     *            socket address of Admin component on Master server
     * @param slaves
     *            socket addresses of slave's Admin components
     * @param listener
     *            shrink progress listener
     * @throws IOException
     */
    public abstract void remoteShrinkMaster(InetSocketAddress adminAddress, Collection<InetSocketAddress> slaves, ShrinkListener listener) throws IOException;

    /**
     * Closes running vyhodb server.
     * <p>
     * If server is running in stand-alone mode, then JVM process is stopped.
     * 
     * @param adminAddress
     *            socket address of vyhodb server's Admin component
     * @param storageCloseTimeout
     *            timeout (in milliseconds) before server closing. Must be >=
     *            500 milliseconds.
     * @throws IOException
     */
    public abstract void remoteStorageClose(InetSocketAddress adminAddress, long storageCloseTimeout) throws IOException;

    /**
     * Removes storage files.
     * 
     * @param logFilename
     *            path to log file
     * @param dataFilename
     *            path to data file
     * @throws IOException
     */
    public void removeStorageFiles(String logFilename, String dataFilename) throws IOException {
        if (logFilename.equals(dataFilename))
            throw new IOException("log and data filenames are equals!");

        Files.deleteIfExists(Paths.get(logFilename));
        Files.deleteIfExists(Paths.get(dataFilename));
    }

    /**
     * Creates new vyhodb storage from backup file.
     * 
     * @see ConsoleRestoreListener
     * @see DummyProgressListener
     * 
     * @param backupFilename
     *            path to existed backup file
     * @param logFilename
     *            path to new log file
     * @param dataFilename
     *            path to new data file
     * @param bufferSize
     *            buffer size (in pages) used for page copying
     * @param slave
     *            indicates whether new storage is slave replication storage
     *            (true) or not (false)
     * @param listener
     *            restore progress listener
     * @throws IOException
     */
    public abstract void restore(String backupFilename, String logFilename, String dataFilename, int bufferSize, boolean slave, ProgressListener listener) throws IOException;

    /**
     * Shrinks log file.
     * <p>
     * Removes old transactions from log file and decreases it size. Storage
     * should be closed and no running vyhodb server should exist.
     * <p>
     * <b>Note! If vyhodb storage has slave replication servers (storage), then
     * {@link #shrinkMaster(String, int, Collection, ShrinkListener)} must be
     * used. Otherwise, slave servers will get out of sync.</b>
     * 
     * @see ConsoleShrinkListener
     * 
     * @param logFilename
     *            path to log file
     * @param bufferSize
     *            buffer size (in pages)
     * @param listener
     *            shrink progress listener
     * @throws IOException
     */
    public abstract void shrink(String logFilename, int bufferSize, ShrinkListener listener) throws IOException;

    /**
     * Shrinks log file of master storage.
     * <p>
     * Method asks slave servers in order to find out which transactions haven't
     * been replicated. It uses this information to remove only those
     * transactions which have already been replicated to specified slave
     * servers, so that non-replicated transactions could leave in log file.
     * <p>
     * Master server should be stopped and log file should be closed. Slave
     * servers must be running and their Admin Components must be turned on.
     * 
     * @see ConsoleShrinkListener
     * 
     * @param masterLogFilename
     *            path to master log file
     * @param bufferSize
     *            buffer size (in pages)
     * @param slaves
     *            socket addresses of slave's Admin components
     * @param listener
     *            shrink progress listener
     * @throws IOException
     */
    public abstract void shrinkMaster(String masterLogFilename, int bufferSize, Collection<InetSocketAddress> slaves, ShrinkListener listener) throws IOException;
}
