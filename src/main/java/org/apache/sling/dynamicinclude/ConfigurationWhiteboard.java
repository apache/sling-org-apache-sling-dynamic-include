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

package org.apache.sling.dynamicinclude;

import static org.osgi.service.component.annotations.FieldOption.UPDATE;
import static org.osgi.service.component.annotations.ReferenceCardinality.MULTIPLE;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ConfigurationWhiteboard.class)
public class ConfigurationWhiteboard {

    @Reference(service = Configuration.class, cardinality = MULTIPLE, policy = DYNAMIC, fieldOption = UPDATE)
    // declared Collection due to SLING-8986
    private volatile Collection<Configuration> configs = new CopyOnWriteArraySet<Configuration>();

    public Configuration getConfiguration(SlingHttpServletRequest request, String resourceType) {
        for (Configuration c : configs) {
            if (isEnabled(c, request) && c.isSupportedResourceType(resourceType)) {
                return c;
            }
        }
        return null;
    }

    private boolean isEnabled(Configuration config, SlingHttpServletRequest request) {
        final String requestPath = request.getRequestPathInfo().getResourcePath();
        return config.isEnabled() && config.getPathMatcher().match(requestPath);
    }
    
    // visible for testing
    void bindConfigs(final Configuration config) {
        configs.add(config);
    }
}
