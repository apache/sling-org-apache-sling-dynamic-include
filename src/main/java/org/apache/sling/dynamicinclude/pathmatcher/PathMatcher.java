package org.apache.sling.dynamicinclude.pathmatcher;

public interface PathMatcher {

  /**
   * Matches given path with the configured path parameter
   * @param path path to match
   * @return true if path matches, false otherwise
   */
  boolean match(String path);
}
