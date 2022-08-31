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

package com.vyhodb.rsi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies service contract/implementation version.
 * <p>
 * If application developer wishes that only service contacts of specific
 * versions would be able to invoke service implementation methods, he/she
 * should adds <code>@Version</code> annotation with required version to both
 * <strong>Service contract interface</strong> and <strong>Service
 * implementation class</strong>.
 * <p>
 * <b>Version validation</b>
 * <p>
 * Version validation is performed by RSI Server.
 * <p>
 * If service contract is annotated by @Version, then each RSI request includes
 * service contract version.
 * <p>
 * When RSI request reaches RSI Server it checks whether required service
 * implementation class is annotated by @Version annotation. If so, then RSI
 * Server compares versions. If they are different then
 * {@linkplain RsiServerException} is thrown and sent back to service proxy
 * object at client side.
 *
 * @author Igor Vykhodtsev
 * @since vyhodb 0.6.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Version {

    /**
     * Service version in free text format.
     * 
     * @return service version
     */
    String version();
}
