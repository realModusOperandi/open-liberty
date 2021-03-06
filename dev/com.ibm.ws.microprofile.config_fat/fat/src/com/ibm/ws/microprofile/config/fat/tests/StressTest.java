/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.microprofile.config.fat.tests;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.ibm.ws.fat.util.SharedServer;

import componenttest.custom.junit.runner.Mode;
import componenttest.custom.junit.runner.Mode.TestMode;

/**
 *
 */
@Mode(TestMode.FULL)
public class StressTest extends AbstractConfigApiTest {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("StressServer");

    public StressTest() {
        super("/stress/");
    }

    @Override
    protected SharedServer getSharedServer() {
        return SHARED_SERVER;
    }

    @Rule
    public TestName testName = new TestName();

    @Test
    public void testLargeConfigSources() throws Exception {
        test(testName.getMethodName());
    }

    @Test
    public void testLargeDynamicUpdates() throws Exception {
        test(testName.getMethodName());
    }

    @Test
    public void testManyConfigSources() throws Exception {
        test(testName.getMethodName());
    }

    @Test
    public void testRegistrationDeregistration() throws Exception {
        //this test used to have a x1000 loop inside it but the http request was timing out
        //so have switched the internal loop to x100 and calling the test 10 times
        for (int i = 0; i < 10; i++) {
            test(testName.getMethodName());
        }
    }

}
