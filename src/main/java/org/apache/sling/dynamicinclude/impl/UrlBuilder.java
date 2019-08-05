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

package org.apache.sling.dynamicinclude.impl;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.dynamicinclude.Configuration;

public final class UrlBuilder {


    public static String buildUrl(String includeSelector, String resourceType, boolean synthetic, Configuration config, RequestPathInfo pathInfo) {
        final StringBuilder builder = new StringBuilder();

        final String resourcePath = pathInfo.getResourcePath();
        builder.append(resourcePath);
        String currentSelectorString = StringUtils.defaultString(pathInfo.getSelectorString());
        if (pathInfo.getSelectorString() != null) {
            builder.append('.').append(currentSelectorString);
        }
        if (includeSelectorNotAlreadyPresent(pathInfo.getSelectors(), includeSelector)) {
            builder.append('.').append(includeSelector);
        }
        builder.append('.').append(pathInfo.getExtension());
        if (synthetic) {
            builder.append('/').append(resourceType);
            if (config.hasExtensionSet()) {
                builder.append('.').append(config.getExtension());
            }
        } else {
            if (config.isAppendSuffix()) {
                builder.append(StringUtils.defaultString(pathInfo.getSuffix()));
            }
        }
        return builder.toString();
    }

    private static boolean includeSelectorNotAlreadyPresent(String[] currentSelectors, String includeSelector) {
        if (includeSelector.isEmpty()) {
            return false;
        }
        return !Arrays.asList(currentSelectors).contains(includeSelector);
    }
}
