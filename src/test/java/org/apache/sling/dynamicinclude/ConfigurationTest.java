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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.apache.sling.dynamicinclude.pathmatcher.PrefixPathMatcher;
import org.apache.sling.testing.mock.osgi.junit.OsgiContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ConfigurationTest {

  private Configuration tested;
  
  @Rule
  public final OsgiContext context = new OsgiContext();

  @Before
  public void setUp() {
    tested = new Configuration();
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowExceptionWhenRegexisInvalid() throws Exception {
      
    context.registerInjectActivateService(tested, "include-filter.config.path", "^(");
  }

  @Test
  public void shouldSetDefaultValuesWhenPropertiesAreEmpty() throws Exception {
    Map<String, Object> properties = new HashMap<String, Object>();

    context.registerInjectActivateService(tested, properties);

    assertThat(tested.getPathMatcher().getClass().isAssignableFrom(PrefixPathMatcher.class), is(true));
    assertThat(tested.getAddComment(), is(false));
    assertThat(tested.getIgnoreUrlParams().size(), is(0));
    assertThat(tested.getIncludeSelector(), is("nocache"));
    assertThat(tested.getIncludeTypeName(), is("SSI"));
    assertThat(tested.getRequiredHeader(), is("Server-Agent=Communique-Dispatcher"));
    assertThat(tested.getTtl(), is(-1));
    assertThat(tested.isEnabled(), is(false));
    assertThat(tested.hasTtlSet(), is(false));
    assertThat(tested.isRewritePath(), is(false));
  }

  @Test
  public void shouldSetConfigurationValues() throws Exception {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("include-filter.config.path", "/content/test/path");
    properties.put("include-filter.config.include-type", "ESI");
    properties.put("include-filter.config.add_comment", true);
    properties.put("include-filter.config.ttl", 60);
    properties.put("include-filter.config.enabled", true);
    properties.put("include-filter.config.resource-types", new String[]{"test/resource/type"});
    properties.put("include-filter.config.required_header", "CustomHeader: value");
    properties.put("include-filter.config.selector", "cache");
    properties.put("include-filter.config.rewrite", true);
    properties.put("include-filter.config.ignoreUrlParams", new String[] {"query"});

    context.registerInjectActivateService(tested, properties);

    assertThat(tested.getPathMatcher().getClass().isAssignableFrom(PrefixPathMatcher.class), is(true));
    assertThat(tested.getAddComment(), is(true));
    assertThat(tested.getIgnoreUrlParams().size(), is(1));
    assertThat(tested.getIncludeSelector(), is("cache"));
    assertThat(tested.getIncludeTypeName(), is("ESI"));
    assertThat(tested.getRequiredHeader(), is("CustomHeader: value"));
    assertThat(tested.getTtl(), is(60));
    assertThat(tested.isEnabled(), is(true));
    assertThat(tested.hasTtlSet(), is(true));
    assertThat(tested.isRewritePath(), is(true));
    assertThat(tested.isSupportedResourceType("test/resource/type"), is(true));
  }
}
