package org.apache.sling.dynamicinclude.predicates;

import java.util.function.Predicate;

// TODO rename class to something more fitting
public class IgnoreUrlParamsPredicate<String> implements Predicate<String> {

	@Override
	public boolean test(String string) {
		return false;
	}
}
