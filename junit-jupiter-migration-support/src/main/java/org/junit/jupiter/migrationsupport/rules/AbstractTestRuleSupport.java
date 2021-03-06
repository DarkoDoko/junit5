/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Member;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Rule;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.migrationsupport.rules.adapter.AbstractTestRuleAdapter;
import org.junit.jupiter.migrationsupport.rules.adapter.GenericBeforeAndAfterAdvice;
import org.junit.jupiter.migrationsupport.rules.member.RuleAnnotatedMember;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.rules.TestRule;

@API(Experimental)
public abstract class AbstractTestRuleSupport
		implements BeforeEachCallback, TestExecutionExceptionHandler, AfterEachCallback {

	private final Class<Rule> annotationType = Rule.class;
	private final Class<? extends TestRule> ruleType;
	private final Function<RuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator;

	protected AbstractTestRuleSupport(Function<RuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator,
			Class<? extends TestRule> ruleType) {
		this.adapterGenerator = adapterGenerator;
		this.ruleType = ruleType;
	}

	protected abstract RuleAnnotatedMember createRuleAnnotatedMember(TestExtensionContext context, Member member);

	protected abstract List<Member> findRuleAnnotatedMembers(Object testInstance);

	protected Class<Rule> getAnnotationType() {
		return this.annotationType;
	}

	protected Class<? extends TestRule> getRuleType() {
		return this.ruleType;
	}

	@Override
	public void beforeEach(TestExtensionContext context) throws Exception {
		this.invokeAppropriateMethodOnRuleAnnotatedMembers(context, GenericBeforeAndAfterAdvice::before);
	}

	@Override
	public void handleTestExecutionException(TestExtensionContext context, Throwable throwable) throws Throwable {
		this.invokeAppropriateMethodOnRuleAnnotatedMembers(context, advice -> {
			try {
				advice.handleTestExecutionException(throwable);
			}
			catch (Throwable t) {
				throw ExceptionUtils.throwAsUncheckedException(t);
			}
		});
	}

	@Override
	public void afterEach(TestExtensionContext context) throws Exception {
		this.invokeAppropriateMethodOnRuleAnnotatedMembers(context, GenericBeforeAndAfterAdvice::after);
	}

	private void invokeAppropriateMethodOnRuleAnnotatedMembers(TestExtensionContext context,
			Consumer<GenericBeforeAndAfterAdvice> methodCaller) {
		List<Member> members = this.findRuleAnnotatedMembers(context.getTestInstance());

		// @formatter:off
        members.stream()
                .map(member -> this.createRuleAnnotatedMember(context, member))
                .map(this.adapterGenerator)
		        .forEach(methodCaller::accept);
        // @formatter:on
	}

}
