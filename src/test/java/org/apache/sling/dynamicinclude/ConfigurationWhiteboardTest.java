package org.apache.sling.dynamicinclude;

import static org.apache.sling.dynamicinclude.Configuration.PROPERTY_FILTER_ENABLED;
import static org.apache.sling.dynamicinclude.Configuration.PROPERTY_FILTER_PATH;
import static org.apache.sling.dynamicinclude.Configuration.PROPERTY_FILTER_RESOURCE_TYPES;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.PatternSyntaxException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationWhiteboardTest {

  private static final String TEST_RESOURCE_PATH = "/content/test/engl/home/pageresource";
  public static final String TEST_RESOURCE_TYPE = "test/component/resourceType";

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
    properties.put(PROPERTY_FILTER_ENABLED, enabled);
    properties.put(PROPERTY_FILTER_PATH, pathRegex);
    properties.put(PROPERTY_FILTER_RESOURCE_TYPES, resourceTypes);
    configuration.activate(null, properties);
    return configuration;
  }

  @Test
  public void shouldNotReturnConfigsIfNotConfigsHaveBeenBound() throws Exception {
    assertThat(tested.getConfiguration(request, TEST_RESOURCE_TYPE), is(nullValue()));
  }

  @Test
  public void shouldNotReturnConfigurationIfResourceTypeDoesNotMatch() throws Exception {
    Configuration testConfiguration = buildConfiguration(true, "^/content.*$", new String[]{"invalid/resourceType"});
    tested.bindConfigs(testConfiguration);

    assertThat(tested.getConfiguration(request, TEST_RESOURCE_TYPE), is(nullValue()));
  }

  @Test
  public void shouldNotReturnConfigurationIfConfigurationIsDisabled() throws Exception {
    Configuration testConfiguration = buildConfiguration(false, "^/content.*$", new String[]{TEST_RESOURCE_TYPE});
    tested.bindConfigs(testConfiguration);

    assertThat(tested.getConfiguration(request, TEST_RESOURCE_TYPE), is(nullValue()));
  }

  @Test
  public void shouldNotReturnConfigurationIfPathDoesNotMatchRegex() throws Exception {
    Configuration testConfiguration = buildConfiguration(true, "^/content/notMatched/.*$",
        new String[]{TEST_RESOURCE_TYPE});
    tested.bindConfigs(testConfiguration);

    assertThat(tested.getConfiguration(request, TEST_RESOURCE_TYPE), is(nullValue()));
  }

  @Test
  public void shouldReturnValidConfiguration() throws Exception {
    Configuration testConfiguration = buildConfiguration(true, "^/content.*$", new String[]{TEST_RESOURCE_TYPE});
    tested.bindConfigs(testConfiguration);

    assertThat(tested.getConfiguration(request, TEST_RESOURCE_TYPE), is(testConfiguration));
  }
}