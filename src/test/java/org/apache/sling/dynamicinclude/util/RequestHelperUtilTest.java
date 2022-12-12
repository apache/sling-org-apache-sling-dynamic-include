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

package org.apache.sling.dynamicinclude.util;

import org.apache.sling.api.SlingHttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class RequestHelperUtilTest {

	private static final String TEST_PARAM_NAME = "test-param";
	private static final String IGNORE_PARAM_REGEX_STAR_WILDCARD = "test-(.*)";

	private SlingHttpServletRequest slingHttpServletRequest;
	private Collection<String> ignoreUrlParams;

	@Before
	public void prepareTestData() {
		slingHttpServletRequest = Mockito.mock(SlingHttpServletRequest.class);

		ignoreUrlParams = new ArrayList<>();
	}

	@Test
	public void requestHasParameters_noParametersAtAll() {
		Mockito.when(slingHttpServletRequest.getParameterMap()).thenReturn(new HashMap<>());

		Assert.assertFalse(RequestHelperUtil.requestHasNonIgnoredParameters(ignoreUrlParams, slingHttpServletRequest));
	}

	@Test
	public void requestHasParameters_onlyIgnoredParameters_withoutWildcards() {
		ignoreUrlParams.add(TEST_PARAM_NAME);

		Map<String, String[]> parameterMap = new HashMap<>();
		parameterMap.put(TEST_PARAM_NAME, new String[] {});

		Mockito.when(slingHttpServletRequest.getParameterMap()).thenReturn(parameterMap);

		Assert.assertFalse(RequestHelperUtil.requestHasNonIgnoredParameters(ignoreUrlParams, slingHttpServletRequest));
	}

	@Test
	public void requestHasParameters_onlyIgnoredParameters_withWildcards() {
		ignoreUrlParams.add(IGNORE_PARAM_REGEX_STAR_WILDCARD);
		ignoreUrlParams.add("hello");

		Map<String, String[]> parameterMap = new HashMap<>();
		parameterMap.put(TEST_PARAM_NAME, new String[] {});
		parameterMap.put("hello", new String[] {});

		Mockito.when(slingHttpServletRequest.getParameterMap()).thenReturn(parameterMap);

		Assert.assertFalse(RequestHelperUtil.requestHasNonIgnoredParameters(ignoreUrlParams, slingHttpServletRequest));
	}

	@Test
	public void requestHasParameters_hasParametersThatAreNotIgnored_withoutWildcards() {
		ignoreUrlParams.add(TEST_PARAM_NAME);

		Map<String, String[]> parameterMap = new HashMap<>();
		parameterMap.put(TEST_PARAM_NAME, new String[] {});
		parameterMap.put("some-other-param", new String[] {});

		Mockito.when(slingHttpServletRequest.getParameterMap()).thenReturn(parameterMap);

		Assert.assertTrue(RequestHelperUtil.requestHasNonIgnoredParameters(ignoreUrlParams, slingHttpServletRequest));
	}

	@Test
	public void requestHasParameters_hasParametersThatAreNotIgnored_withWildcards() {
		ignoreUrlParams.add(IGNORE_PARAM_REGEX_STAR_WILDCARD);

		Map<String, String[]> parameterMap = new HashMap<>();
		parameterMap.put(TEST_PARAM_NAME, new String[] {});
		parameterMap.put("some-other-param", new String[] {});

		Mockito.when(slingHttpServletRequest.getParameterMap()).thenReturn(parameterMap);

		Assert.assertTrue(RequestHelperUtil.requestHasNonIgnoredParameters(ignoreUrlParams, slingHttpServletRequest));
	}

	@Test
	public void requestHasParameters_specificRegex_hasIgnoredParameters() {
		ignoreUrlParams.add("hello-[0-9]-world");

		Map<String, String[]> parameterMap = new HashMap<>();
		parameterMap.put(TEST_PARAM_NAME, new String[] {});
		parameterMap.put("hello-1-world", new String[] {});
		parameterMap.put("hello-2-world", new String[] {});

		Mockito.when(slingHttpServletRequest.getParameterMap()).thenReturn(parameterMap);

		Assert.assertTrue(RequestHelperUtil.requestHasNonIgnoredParameters(ignoreUrlParams, slingHttpServletRequest));
	}

	@Test
	public void requestHasParameters_specificRegex_hasNoIgnoredParameters() {
		ignoreUrlParams.add("hello-[0-9]-world");

		Map<String, String[]> parameterMap = new HashMap<>();
		parameterMap.put("hello-1-world", new String[] {});
		parameterMap.put("hello-2-world", new String[] {});
		parameterMap.put("hello-3-world", new String[] {});

		Mockito.when(slingHttpServletRequest.getParameterMap()).thenReturn(parameterMap);

		Assert.assertFalse(RequestHelperUtil.requestHasNonIgnoredParameters(ignoreUrlParams, slingHttpServletRequest));
	}

}
