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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.annotations.SlingServletFilterScope;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@SlingServletFilter(scope = SlingServletFilterScope.REQUEST)
@Component(property = { 
    Constants.SERVICE_RANKING + ":Integer=" + Integer.MIN_VALUE,
    Constants.SERVICE_VENDOR  +"=The Apache Software Foundation"
})
public class SyntheticResourceFilter implements Filter {

    @Reference
    private ConfigurationWhiteboard configurationWhiteboard;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        final String resourceType = getResourceTypeFromSuffix(slingRequest);
        final Configuration config = configurationWhiteboard.getConfiguration(slingRequest, resourceType);

        if (config == null || !config.hasIncludeSelector(slingRequest)
                || !ResourceUtil.isSyntheticResource(slingRequest.getResource())
                || (config.hasExtensionSet() && !config.hasExtension(slingRequest))) {
            chain.doFilter(request, response);
            return;
        }

        final RequestDispatcherOptions options = new RequestDispatcherOptions();
        options.setForceResourceType(resourceType);
        String resourcePath = StringUtils.substringBefore(slingRequest.getRequestPathInfo().getResourcePath(), ".");
        Resource resource = slingRequest.getResourceResolver().resolve(resourcePath);
        final RequestDispatcher dispatcher = slingRequest.getRequestDispatcher(resource, options);
        dispatcher.forward(request, response);
    }

    private static String getResourceTypeFromSuffix(final SlingHttpServletRequest request) {
        String suffix = request.getRequestPathInfo().getSuffix();
        suffix = StringUtils.substringBeforeLast(suffix, ".");
        return StringUtils.removeStart(suffix, "/");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
