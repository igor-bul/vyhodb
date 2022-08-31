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

/**
 * Console implementation of storage restoration listener.
 * <p>
 * Implementation prints progress messages to console.
 * 
 * @see Admin
 * 
 * @author Igor Vykhodtsev
 * @since vyhodb 0.9.0
 */
public class ConsoleRestoreListener implements ProgressListener {

    private final int _step;

    /**
     * Constructor.
     * 
     * @param step
     *            notification step in percents.
     */
    public ConsoleRestoreListener(int step) {
        _step = step;
    }

    @Override
    public void completed() {
        System.out.println("Restoring - 100% completed");
    }

    @Override
    public int getStep() {
        return _step;
    }

    @Override
    public void progress(int percent) {
        System.out.println("Restoring - " + percent + "% completed");
    }

    @Override
    public void started() {
        System.out.println("Restoring started");
    }
}
