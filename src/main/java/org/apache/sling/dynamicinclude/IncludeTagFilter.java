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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.dynamicinclude.generator.IncludeGenerator;
import org.apache.sling.dynamicinclude.generator.IncludeGeneratorWhiteboard;
import org.apache.sling.dynamicinclude.impl.UrlBuilder;
import org.apache.sling.servlets.annotations.SlingServletFilter;
import org.apache.sling.servlets.annotations.SlingServletFilterScope;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SlingServletFilter(scope = SlingServletFilterScope.INCLUDE)
@Component(property = {Constants.SERVICE_RANKING + ":Integer=-500"})
public class IncludeTagFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(IncludeTagFilter.class);

    private static final String COMMENT = "<!-- SDI include (path: %s, resourceType: %s) -->\n";

    @Reference
    private ConfigurationWhiteboard configurationWhiteboard;

    @Reference
    private IncludeGeneratorWhiteboard generatorWhiteboard;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        final SlingHttpServletRequest slingRequest = (SlingHttpServletRequest) request;
        final String resourceType = slingRequest.getResource().getResourceType();

        final Configuration config = configurationWhiteboard.getConfiguration(slingRequest, resourceType);
        if (config == null) {
            chain.doFilter(request, response);
            return;
        }

        final IncludeGenerator generator = generatorWhiteboard.getGenerator(config.getIncludeTypeName());
        if (generator == null) {
            LOG.error("Invalid generator: " + config.getIncludeTypeName());
            chain.doFilter(request, response);
            return;
        }

        final PrintWriter writer = response.getWriter();
        final String url = getUrl(config, slingRequest);
        if (url == null) {
            chain.doFilter(request, response);
            return;
        }

        if (config.getAddComment()) {
            writer.append(String.format(COMMENT, StringEscapeUtils.escapeHtml4(url), resourceType));
        }

        // Only write the includes markup if the required, configurable request
        // header is present
        if (shouldWriteIncludes(config, slingRequest)) {
            String include = generator.getInclude(url);
            LOG.debug(include);
            writer.append(include);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean shouldWriteIncludes(Configuration config, SlingHttpServletRequest request) {
        // Do not skip GET requests when DisableIgnoreUrlParams set to true.
        if (!config.isDisableIgnoreUrlParams() && requestHasParameters(config.getIgnoreUrlParams(), request)) {
            return false;
        }
        final String requiredHeader = config.getRequiredHeader();
        return StringUtils.isBlank(requiredHeader) || containsHeader(requiredHeader, request);
    }

    private boolean requestHasParameters(List<String> ignoreUrlParams, SlingHttpServletRequest request) {
        final Enumeration<?> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            final String paramName = (String) paramNames.nextElement();
            if (!ignoreUrlParams.contains(paramName)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsHeader(String requiredHeader, SlingHttpServletRequest request) {
        final String name, expectedValue;
        if (StringUtils.contains(requiredHeader, '=')) {
            final String split[] = StringUtils.split(requiredHeader, '=');
            name = split[0];
            expectedValue = split[1];
        } else {
            name = requiredHeader;
            expectedValue = null;
        }

        final String actualValue = request.getHeader(name);
        if (actualValue == null) {
            return false;
        } else if (expectedValue == null) {
            return true;
        } else {
            return actualValue.equalsIgnoreCase(expectedValue);
        }
    }

    private String getUrl(Configuration config, SlingHttpServletRequest request) {
        String url = buildUrl(config, request);
        if (config.isRewritePath()) {
            url = removeQuestionMark(url);
            url = request.getResourceResolver().map(request, url);
        } else {
            url = encodeJcrContentPart(url);
            try {
                url = new URI(null, null, url, null).toASCIIString();
            } catch (URISyntaxException e) {
                LOG.error("Include url is in the wrong format", e);
                return null;
            }
        }

        return url;
    }

    private String buildUrl(Configuration config, SlingHttpServletRequest request) {
        final Resource resource = request.getResource();

        // The below code gets the path to the content and then passes it to the buildUrl method
        // so that the path to the component is replaced with path to the content
        ValueMap vm = resource.adaptTo(ValueMap.class);
        String contentPath = "";
        Boolean replaceToContentPath = false;
        if (config.isContentPathRewriteEnabled() &&
                null != vm &&
                vm.containsKey(config.getContentPathProperty())) {
            contentPath = vm.get(config.getContentPathProperty(), String.class);
            if (contentPath.length() > 0) {
                replaceToContentPath = true;
            }
        }

        final boolean synthetic = ResourceUtil.isSyntheticResource(request.getResource());
        return UrlBuilder.buildUrl(config.getIncludeSelector(), resource.getResourceType(), synthetic, config, request.getRequestPathInfo(), contentPath, replaceToContentPath);
    }

    private static String sanitize(String path) {
        return StringUtils.defaultString(path);
    }

    private static String encodeJcrContentPart(String url) {
        return url.replace("jcr:content", "_jcr_content");
    }

    private static String removeQuestionMark(String url) {
        return url.replaceAll("[?]", "");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
