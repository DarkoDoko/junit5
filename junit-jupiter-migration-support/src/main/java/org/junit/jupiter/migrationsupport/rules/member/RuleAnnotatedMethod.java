/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules.member;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.rules.TestRule;

@API(Internal)
public class RuleAnnotatedMethod extends AbstractRuleAnnotatedMember {

	public RuleAnnotatedMethod(Object testInstance, Method member) {
		this.testRuleInstance = (TestRule) ReflectionUtils.invokeMethod(member, testInstance);
	}

}
