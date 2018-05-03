package org.apache.sling.dynamicinclude.pathmatcher;

import org.apache.commons.lang.StringUtils;

public class PrefixPathMatcher implements PathMatcher {

  private final String configurationValue;

  public PrefixPathMatcher(String configurationValue) {
    this.configurationValue = configurationValue;
  }

  @Override
  public boolean match(String path) {
    return StringUtils.isNotBlank(path) && path.startsWith(configurationValue);
  }
}
