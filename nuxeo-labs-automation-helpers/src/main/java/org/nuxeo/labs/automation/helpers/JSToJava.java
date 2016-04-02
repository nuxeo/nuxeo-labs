/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package org.nuxeo.labs.automation.helpers;

import java.util.ArrayList;

import jdk.nashorn.internal.objects.NativeArray;

import org.nuxeo.ecm.automation.context.ContextHelper;

/**
 * @since 7.4
 */
public class JSToJava implements ContextHelper {

    /**
     * Receives a JavaScript array, returns a Java ArrayList.
     * Does not handle what's inside the arrays, but it should be Java objects.
     * 
     * @param inArray
     * @return
     * @since 7.4
     */
    // Example of JavaScript
    //
    // function run(input, params) {
    //
    // var doc, i;
    // var arr = new Array(0);
    //
    // doc = Repository.GetDocument(input, {'value': "66dbd6d1-2101-4bb4-ac13-9bd3269ca319"});
    // arr.push(doc);
    // doc = Repository.GetDocument(input, {'value': "3e2c050d-0446-4f9b-8ef6-885c113afd4f"});
    // arr.push(doc);
    // doc = Repository.GetDocument(input, {'value': "0fcae6cc-9829-4840-87dc-bcb123df7049"});
    // arr.push(doc);
    //
    // var javaArray = JSToJava.arrayToArrayList(arr);
    // // Now, we have a DocumentModelList, available for operations
    //
    // Log(null, {'level': "warn", 'message': "Test length: " + javaArray.length});
    //
    // for(i = 0; i < javaArray.length; ++i) {
    //   Log(null, {'level': "warn", 'message': "Title: " + javaArray.get(i).getTitle()});
    // }
    //
    // }
    public ArrayList<Object> arrayToArrayList(NativeArray inArray) {

        ArrayList<Object> javaArray = new ArrayList<Object>();
        javaArray.addAll(inArray.values());
        return javaArray;
    }
}
