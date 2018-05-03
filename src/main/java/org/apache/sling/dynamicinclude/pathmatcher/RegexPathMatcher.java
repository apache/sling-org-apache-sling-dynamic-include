package org.apache.sling.dynamicinclude.pathmatcher;

import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

public class RegexPathMatcher implements PathMatcher {

  private final Pattern configurationPattern;

  public RegexPathMatcher(String configurationRegex) {
    this.configurationPattern = Pattern.compile(configurationRegex);
  }

  @Override
  public boolean match(String path) {
    return StringUtils.isNotBlank(path) && configurationPattern.matcher(path).matches();
  }
}
