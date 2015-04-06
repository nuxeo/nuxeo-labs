/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package org.nuxeo.labs.operations.services;

/**
 * 
 *
 * @since 7.2
 */
public class RESTUtils {
    
    public static String formatForJSON(String inStr) {

        String result = inStr;
        if(result == null || result.isEmpty()) {
            result = "\"\"";
        } else {
            int l = result.length();
            if(l == 1) {
                result = doubleQuoteString(result);
            } else {
                char firstChar = result.charAt(0);
                char lastChar = result.charAt(l - 1);
                if(firstChar == '[' && lastChar == ']') {
                    // It's an array => no quotes
                } else if(firstChar == '{' && lastChar == '}') {
                    // It's an object => no quotes
                } else if(firstChar == '"' && lastChar == '"') {
                 // Already quoted?
                }else {
                    result = doubleQuoteString(result);
                }
            }
        }
        
        return result;
    }

    public static String doubleQuoteString(String inStr) {

        return "\"" + inStr + "\"";
        
    }
}
