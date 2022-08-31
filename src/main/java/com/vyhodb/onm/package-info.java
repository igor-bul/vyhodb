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

/**
 * Provides <b>"ONM API"</b> (Object-to-Network-model-Mapping) classes and interfaces which allow reading/writing java object 
 * graph from/to vyhodb record graph.
 * <p>
 * ONM framework provides the following features:
 * <p>
 * <ul>
 * <li><b>ONM Reading:</b> traverses over records and creates java object graph corresponding to traversed route.</li>
 * <li><b>ONM Writing:</b> updates vyhodb records (create/update/delete) by a java object graph.</li>
 * <li><b>ONM Cloning:</b> traverses over java object graph and creates it's copy, corresponding to traversed route.</li>
 * </ul>
 * <p>
 * ONM Reading and ONM Cloning use <b>Functions API</b> framework for traversing over records and java object graph.
 * {@linkplain com.vyhodb.onm.OnmFactory} contains functions for starting ONM Reading and ONM Cloning as well as object navigation functions.
 * <p>
 * ONM Reading functions are just two functions which should wrap record navigation functions (see {@linkplain com.vyhodb.f.NavigationFactory}).
 * <p>
 * ONM Cloning functions are two functions, which, in turns, should wrap object navigation functions (defined in OnmFactory as well).
 * <p>
 * ONM Writing is implemented by one method {@linkplain com.vyhodb.onm.Writer#write(Mapping, Object, com.vyhodb.space.Space) Writer.write()}.
 * 
 * <h5>Mapping</h5>
 * <p>
 * {@linkplain com.vyhodb.onm.Mapping Mapping} class is a cache of mapping information between java class's fields from one hand and Record's
 * fields, parent/child links from another. Mapping is specified either by annotations 
 * or by XML file, which schema is <a href="./doc-files/OnmSchema.xsd" type="text/xml" target="_blank">OnmSchema.xsd</a>. 
 *    
 * <h5>Requirements for Java classes</h5>
 * <p>
 * The following rules are applied for java classes, which objects participate in ONM (reading/writing/cloning):
 * <ol>
 * <li>Class must be annotated by {@linkplain com.vyhodb.onm.Record @Record} or have &lt;class&gt; element in mapping xml.</li>
 * <li>Class must have default no-argument public constructor</li>
 * <li>Only visible class fields can participate on ONM: class's fields and inherited public/protected fields.</li>
 * </ol>
 * 
 * <p><b>ONM Reading example</b>
 * <pre>
 *   // Creates mapping cache
 *   Mapping mapping = Mapping.newAnnotationMapping();
 *   
 *   // Builds ONM reading function
 *   F onmReadF = 
 *       startRead(Root.class, mapping,
 *          children("order2root",
 *              children("item2order",
 *                  parent("item2product") 
 *              )
 *          ),
 *          children("product2root")
 *      );
 *    
 *   // Evaluates ONM reading function.
 *   Root rootObject = (Root) onmReadF.eval(someRecord);
 * </pre>
 * 
 * <p><b>ONM Writing example</b>
 * <pre>
 *   Writer.write(Mapping.newAnnotationMapping, root, space);
 * </pre>
 * 
 * <p><b>ONM Cloning example</b>
 * <pre>
 *   Order sourceOrder = ...
 *   
 *   // Creates mapping cache
 *   Mapping mapping = Mapping.newAnnotationMapping();
 *   
 *   // Builds ONM Cloning function
 *   F cloneF = 
 *       startClone(mapping,
 *           objectChildren("item2order", 
 *               objectParent("item2product")
 *           )
 *      );
 *      
 *   // Evaluates ONM Cloning function. Result - cloned object graph.  
 *   Order order = (Order) cloneF.eval(sourceOrder); 
 * </pre>
 * 
 * @see com.vyhodb.f.F
 */
package com.vyhodb.onm;

