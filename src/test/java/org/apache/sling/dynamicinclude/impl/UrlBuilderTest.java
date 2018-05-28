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

package org.apache.sling.dynamicinclude.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.request.RequestPathInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UrlBuilderTest {

    @Mock
    private RequestPathInfo requestPathInfo;

    @Test
    public void shouldAppendTheIncludeSelectorToUrlWithNoSelectors() {
        givenAnHtmlRequestForResource("/resource/path");
        withSelectorString(null);
        boolean isSyntheticResource = false;

        String actualResult = UrlBuilder.buildUrl("include", "apps/example/resource/type", isSyntheticResource, requestPathInfo);

        assertThat(actualResult, is("/resource/path.include.html"));
    }

    @Test
    public void shouldAppendTheIncludeSelectorToUrlThatAlreadyContainsOtherSelectors() {
        givenAnHtmlRequestForResource("/resource/path");
        withSelectorString("foo.bar.baz");
        boolean isSyntheticResource = false;

        String actualResult = UrlBuilder.buildUrl("include", "apps/example/resource/type", isSyntheticResource, requestPathInfo);

        assertThat(actualResult, is("/resource/path.foo.bar.baz.include.html"));
    }

    @Test
    public void shouldAppendTheIncludeSelectorToUrlContainingMixedAlphanumericSelectors() {
        givenAnHtmlRequestForResource("/resource/path");
        withSelectorString("foo.2.31");
        boolean isSyntheticResource = false;

        String actualResult = UrlBuilder.buildUrl("include", "apps/example/resource/type", isSyntheticResource, requestPathInfo);

        assertThat(actualResult, is("/resource/path.foo.2.31.include.html"));
    }

    @Test
    public void shouldNotDuplicateTheIncludeSelectorIfAlreadyPresent() {
        givenAnHtmlRequestForResource("/resource/path");
        withSelectorString("foo.include");
        boolean isSyntheticResource = false;

        String actualResult = UrlBuilder.buildUrl("include", "apps/example/resource/type", isSyntheticResource, requestPathInfo);

        assertThat(actualResult, is("/resource/path.foo.include.html"));
    }

    @Test
    public void shouldAppendTheIncludeSelectorWhenThereIsAnotherSelectorThatAccidentallyContainsTheIncludeOne() {
        givenAnHtmlRequestForResource("/resource/path");
        withSelectorString("longerSelectorThatHappensToContainTheIncludeSelector");
        boolean isSyntheticResource = false;

        String actualResult = UrlBuilder.buildUrl("IncludeSelector", "apps/example/resource/type", isSyntheticResource, requestPathInfo);

        assertThat(actualResult, is("/resource/path.longerSelectorThatHappensToContainTheIncludeSelector.IncludeSelector.html"));
    }

    @Test
    public void shouldAppendSuffixForSyntheticResources() {
        givenAnHtmlRequestForResource("/resource/path");
        withSelectorString("foo.include");
        boolean isSyntheticResource = true;

        String actualResult = UrlBuilder.buildUrl("include", "apps/example/resource/type", isSyntheticResource, requestPathInfo);

        assertThat(actualResult, is("/resource/path.foo.include.html/apps/example/resource/type"));
    }

    private void givenAnHtmlRequestForResource(String resourcePath) {
        when(requestPathInfo.getExtension()).thenReturn("html");
        when(requestPathInfo.getResourcePath()).thenReturn(resourcePath);
    }

    private void withSelectorString(String selectorString) {
        when(requestPathInfo.getSelectorString()).thenReturn(selectorString);
        when(requestPathInfo.getSelectors()).thenReturn(StringUtils.defaultString(selectorString).split("\\."));
    }
}
