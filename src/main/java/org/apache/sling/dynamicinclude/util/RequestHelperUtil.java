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

import java.util.Collection;

public class RequestHelperUtil {

	private RequestHelperUtil() {
		// private constructor to prevent instance creation of util classes
	}

	/**
	 * Checks if a request contains any parameters that are not defined in the ignoreUrlParams.
	 * Wildcards as they are possible to configure on Apache-Dispatcher, are also properly checked.
	 *
	 * @param ignoreUrlParams The list of configured ignoreUrlParams (regular expressions)
	 * @param request         The slingRequest whose parameters we want to check
	 * @return true if there was any parameter that is not defined on the ignoreUrlsParams-Collection, otherwise false
	 */
	public static boolean requestHasNonIgnoredParameters(Collection<String> ignoreUrlParams, SlingHttpServletRequest request) {
		return request.getParameterMap().keySet().stream()
				.anyMatch(urlParameter -> !matchesRegularExpressionIgnoreUrlParameter(ignoreUrlParams, urlParameter));
	}

	private static boolean matchesRegularExpressionIgnoreUrlParameter(Collection<String> ignoreUrlParameters, String requestParameter) {
		for (String ignoreUrlParameter : ignoreUrlParameters) {
			if (requestParameter.matches(ignoreUrlParameter)) {
				return true;
			}
		}

		return false;
	}

}
