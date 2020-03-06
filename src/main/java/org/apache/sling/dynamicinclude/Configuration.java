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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.dynamicinclude.pathmatcher.PathMatcher;
import org.apache.sling.dynamicinclude.pathmatcher.PrefixPathMatcher;
import org.apache.sling.dynamicinclude.pathmatcher.RegexPathMatcher;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.metatype.annotations.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Include filter configuration.
 */
@Component(service = Configuration.class,
    immediate = true,
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    property = {
        Constants.SERVICE_VENDOR + "=The Apache Software Foundation",
        "webconsole.configurationFactory.nameHint={include-filter.config.include-type} for [{include-filter.config.resource-types}] at path: {include-filter.config.path}"
    })
@Designate(ocd = Configuration.Config.class, factory = true)
public class Configuration {

  @ObjectClassDefinition(name = "Apache Sling Dynamic Include - Configuration")
  public @interface Config {
      @AttributeDefinition(name="Enabled", description="Check to enable the filter")
      boolean include$_$filter_config_enabled() default false;

      @AttributeDefinition(name="Base path regular expression", description="This SDI configuration will work only for paths matching this value. If value starts with \\\"^\\\" sign, regex matching will be performed. Otherwise it will check for path prefix.")
      String include$_$filter_config_path() default "/content";

      @AttributeDefinition(name="Resource types", description="Filter will replace components with selected resource types", cardinality = Integer.MAX_VALUE)
      String include$_$filter_config_resource$_$types() default "";

      @AttributeDefinition(name = "Include type", description = "Type of generated include tags", options = {
          @Option(label = "Apache SSI", value = "SSI"),
          @Option(label = "ESI", value = "ESI"),
          @Option(label = "Javascript", value = "JSI")
      })
      String include$_$filter_config_include$_$type() default "SSI";

      @AttributeDefinition(name="Add comment", description = "Add comment to included components")
      boolean include$_$filter_config_add__comment() default false;

      @AttributeDefinition(name = "Filter selector", description = "Selector used to mark included resources")
      String include$_$filter_config_selector() default "nocache";

      @AttributeDefinition(name = "Extension", description = "Extension to append to virtual resources to make caching possible")
      String include$_$filter_config_extension() default "";

      @AttributeDefinition(name = "Component TTL", description = "\"Time to live\" cache header for rendered component (in seconds)")
      String include$_$filter_config_ttl() default "";

      @AttributeDefinition(name = "Required header", description = "SDI will work only for requests with given header")
      String include$_$filter_config_required__header() default "Server-Agent=Communique-Dispatcher";

      @AttributeDefinition(name = "Ignore URL params", description = "SDI will process the request even if it contains configured GET parameters", cardinality = Integer.MAX_VALUE)
      String include$_$filter_config_ignoreUrlParams() default "";

      @AttributeDefinition(name =  "Include path rewriting", description = "Check to enable include path rewriting")
      boolean include$_$filter_config_rewrite() default false;

      @AttributeDefinition(name =  "Append suffix to dynamic includes", description = "Check to append the suffix of the parent request to the dynamic include.")
      boolean include$_$filter_config_appendSuffix() default true;

      @AttributeDefinition(name =  "Disable ignore URL params check", description = "Disable the check in the Ignore URL Params setting.")
      boolean include$_$filter_config_disableIgnoreUrlParams() default false;

      @AttributeDefinition(name = "Rewrite to Content Path", description = "Check to enable rewriting to Content Path than Resource Path")
      boolean include$_$filter_config_contentPathRewrite() default false;

      @AttributeDefinition(name = "Content Path Property", description = "Filter will replace content path with selected resource types from this property")
      String include$_$filter_config_contentPathProperty() default "fragmentVariationPath";

      @AttributeDefinition(name = "Content selector", description = "Selector used to include content without HTML wrapper")
      String include$_$filter_config_contentSelector() default "content";
  }

  private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

  private PathMatcher pathMatcher;

  private boolean isEnabled;

  private String includeSelector;

  private String extension;

  private int ttl;

  private List<String> resourceTypes;

  private boolean addComment;

  private String includeTypeName;

  private String requiredHeader;

  private boolean disableIgnoreUrlParams;

  private List<String> ignoreUrlParams;

  private boolean rewritePath;

  private boolean appendSuffix;

  private boolean contentPathRewriteEnabled;

  private String contentPathProperty;

  private String contentSelector;

  @Activate
  public void activate(Config cfg) {
    isEnabled = cfg.include$_$filter_config_enabled();
    String pathPattern = cfg.include$_$filter_config_path();
    pathMatcher = choosePathMatcher(pathPattern);
    String[] resourceTypeList;
    resourceTypeList = PropertiesUtil.toStringArray(cfg.include$_$filter_config_resource$_$types(), new String[0]);
    for (int i = 0; i < resourceTypeList.length; i++) {
      String[] s = resourceTypeList[i].split(";");
      String name = s[0].trim();
      resourceTypeList[i] = name;
    }
    this.resourceTypes = Arrays.asList(resourceTypeList);

    includeSelector = cfg.include$_$filter_config_selector();
    extension = cfg.include$_$filter_config_extension();
    ttl = PropertiesUtil.toInteger(cfg.include$_$filter_config_ttl(), -1);
    addComment = cfg.include$_$filter_config_add__comment();
    includeTypeName = cfg.include$_$filter_config_include$_$type();
    requiredHeader = cfg.include$_$filter_config_required__header();
    ignoreUrlParams = Arrays.asList(PropertiesUtil.toStringArray(cfg.include$_$filter_config_ignoreUrlParams(),
        new String[0]));
    rewritePath = cfg.include$_$filter_config_rewrite();
    appendSuffix = cfg.include$_$filter_config_appendSuffix();
    disableIgnoreUrlParams = cfg.include$_$filter_config_disableIgnoreUrlParams();
    contentPathRewriteEnabled = cfg.include$_$filter_config_contentPathRewrite();
    contentPathProperty = cfg.include$_$filter_config_contentPathProperty();
    contentSelector = cfg.include$_$filter_config_contentSelector();
  }

  private PathMatcher choosePathMatcher(String pathPattern) {
    PathMatcher result;
    if (pathPattern.startsWith("^")) {
      LOG.debug("Configured path value: {} is a regexp - will use a RegexPathMatcher.", pathPattern);
      result = new RegexPathMatcher(pathPattern);
    } else {
      LOG.debug("Configured path value: {} is NOT a regexp - will use a PrefixPathMatcher.", pathPattern);
      result = new PrefixPathMatcher(pathPattern);
    }
    return result;
  }

  public PathMatcher getPathMatcher() {
    return pathMatcher;
  }

  public boolean hasIncludeSelector(SlingHttpServletRequest request) {
    return ArrayUtils.contains(request.getRequestPathInfo().getSelectors(), includeSelector);
  }

  public String getIncludeSelector() {
    return includeSelector;
  }

  public boolean hasExtension(final SlingHttpServletRequest request) {
    final String suffix = request.getRequestPathInfo().getSuffix();
    return suffix.endsWith("." + this.extension);
  }

  public boolean hasExtensionSet() {
    return StringUtils.isNotBlank(this.extension);
  }

  public String getExtension() {
    return this.extension;
  }

  public boolean hasTtlSet() {
    return ttl >= 0;
  }

  public int getTtl() {
    return ttl;
  }

  public boolean isSupportedResourceType(String resourceType) {
    return StringUtils.isNotBlank(resourceType) && resourceTypes.contains(resourceType);
  }

  public boolean getAddComment() {
    return addComment;
  }

  public String getIncludeTypeName() {
    return includeTypeName;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public String getRequiredHeader() {
    return requiredHeader;
  }

  public List<String> getIgnoreUrlParams() {
    return ignoreUrlParams;
  }

  public boolean isDisableIgnoreUrlParams() {
    return disableIgnoreUrlParams;
  }

  public boolean isRewritePath() {
    return rewritePath;
  }

  public boolean isAppendSuffix() {
      return appendSuffix;
  }

  public boolean isContentPathRewriteEnabled() { return contentPathRewriteEnabled; }

  public String getContentPathProperty() { return contentPathProperty; }

  public String getContentSelector() { return contentSelector; }

}
