/*-
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sling.dynamicinclude.api;

import org.apache.sling.api.SlingHttpServletRequest;

/**
 * Include generator interface
 */
public interface IncludeGenerator {
    String getType();

    /**
     * Returns the string used to include the resource.
     * For example, this might be a Javascript code that retrieves a snippet of html,
     * an Apache SSI Tag, or an Edge Side Include Tag.
     * <p>
     * This method receives the sling request and an Url that has already be normalized as followed:
     * <ul>
     * <li>The query string has been removed</li>
     * <li>The Url has been mapped using the ResourceResolver</li>
     * <li>The jcr:content paths have been encoded to _jcr_content.</li>
     * </ul>
     *
     * @param request       the Sling request object
     * @param normalizedUrl the requested url, normalized
     * @return a String used to include the resource
     **/
    String getInclude(SlingHttpServletRequest request, String normalizedUrl);
}
