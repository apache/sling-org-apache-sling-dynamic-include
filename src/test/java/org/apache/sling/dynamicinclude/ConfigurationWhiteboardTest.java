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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.testing.mock.osgi.junit.OsgiContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationWhiteboardTest {
    
  private static final String TEST_RESOURCE_PATH = "/content/test/engl/home/pageresource";
  public static final String TEST_RESOURCE_TYPE = "test/component/resourceType";
  
  @Rule
  public final OsgiContext context = new OsgiContext();

  private ConfigurationWhiteboard tested;

  @Mock
  private SlingHttpServletRequest request;

  @Mock
  private RequestPathInfo requestPathInfo;

  @Before
  public void setUp() throws Exception {
    tested = new ConfigurationWhiteboard();
    when(request.getRequestPathInfo()).thenReturn(requestPathInfo);
    when(requestPathInfo.getResourcePath()).thenReturn(TEST_RESOURCE_PATH);
  }

  private Configuration buildConfiguration(boolean enabled, String pathRegex, String[] resourceTypes) {
    Configuration configuration = new Configuration();
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("include-filter.config.enabled", enabled);
    properties.put("include-filter.config.path", pathRegex);
    properties.put("include-filter.config.resource-types", resourceTypes);
    
    return context.registerInjectActivateService(configuration, properties);
  }

  @Test
  public void shouldNotReturnConfigsIfNotConfigsHaveBeenBound() throws Exception {
    assertThat(tested.getConfiguration(request, TEST_RESOURCE_TYPE), is(nullValue()));
  }

  @Test
  public void shouldNotReturnConfigurationIfResourceTypeDoesNotMatch() throws Exception {
    buildConfiguration(true, "^/content.*$", new String[]{"invalid/resourceType"});
    context.registerInjectActivateService(tested);

    assertThat(tested.getConfiguration(request, TEST_RESOURCE_TYPE), is(nullValue()));
  }

  @Test
  public void shouldNotReturnConfigurationIfConfigurationIsDisabled() throws Exception {
    buildConfiguration(false, "^/content.*$", new String[]{TEST_RESOURCE_TYPE});
    context.registerInjectActivateService(tested);

    assertThat(tested.getConfiguration(request, TEST_RESOURCE_TYPE), is(nullValue()));
  }

  @Test
  public void shouldNotReturnConfigurationIfPathDoesNotMatchRegex() throws Exception {
    buildConfiguration(true, "^/content/notMatched/.*$",
        new String[]{TEST_RESOURCE_TYPE});
    context.registerInjectActivateService(tested);

    assertThat(tested.getConfiguration(request, TEST_RESOURCE_TYPE), is(nullValue()));
  }

  @Test
  public void shouldReturnValidConfiguration() throws Exception {
    Configuration testConfiguration = buildConfiguration(true, "^/content.*$", new String[]{TEST_RESOURCE_TYPE});
    context.registerInjectActivateService(tested);

    assertThat(tested.getConfiguration(request, TEST_RESOURCE_TYPE), is(testConfiguration));
  }
}
