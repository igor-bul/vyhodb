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

package com.vyhodb.f.common;

import java.util.Map;

import com.vyhodb.f.F;

import static com.vyhodb.f.CommonFactory.*;

public class Loop extends F {

    private static final long serialVersionUID = -7878246204398024472L;
    private F _loopF;

    public Loop() {
    }

    @Override
    public Object evalTree(Object current, Map<String, Object> context) {
        return _loopF.evalTree(current, context);
    }

    public void setLoop(F... loopF) {
        if (loopF == null) {
            throw new IllegalArgumentException("[loopF] is null");
        }

        _loopF = composite(loopF);
    }
}
