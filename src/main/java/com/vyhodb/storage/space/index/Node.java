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

package com.vyhodb.storage.space.index;

import com.vyhodb.storage.space.index.iterator.TupleIterator;


public interface Node {
    
    public long getId();
    
    public void insert(Comparable<?> key, long link);

    public void remove(Comparable<?> key, long link);

    public int size();

    public boolean isUnderflow();
    public boolean isOverflow();
    public boolean isOnVergeUnderflow();
    
    public boolean isFull();
    
    public SplitResult split();
    
    public void concatinate(Node rightSibling, Comparable<?> key);
    
    public Comparable<?> redistLR(Comparable<?> parentKey, Node right);
    public Comparable<?> redistRL(Comparable<?> parentKey, Node left);
    
    public TupleIterator search(Comparable<?> searchKey);
    public TupleIterator left();
    public TupleIterator right();
    
    public void lock();
    public void unlock();
  
}