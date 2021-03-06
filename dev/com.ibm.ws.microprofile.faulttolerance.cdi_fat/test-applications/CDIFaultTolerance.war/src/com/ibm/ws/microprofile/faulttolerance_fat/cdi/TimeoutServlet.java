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
package com.ibm.ws.microprofile.faulttolerance_fat.cdi;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

import com.ibm.ws.microprofile.faulttolerance_fat.cdi.beans.TimeoutBean;
import com.ibm.ws.microprofile.faulttolerance_fat.util.ConnectException;

import componenttest.app.FATServlet;

/**
 * Servlet implementation class Test
 */
@WebServlet("/timeout")
public class TimeoutServlet extends FATServlet {
    private static final long serialVersionUID = 1L;

    @Inject
    TimeoutBean bean;

    public void testTimeout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //should timeout after a second as per default
        long start = System.currentTimeMillis();
        try {
            bean.connectA();
            throw new AssertionError("TimeoutException not thrown");
        } catch (TimeoutException e) {
            //expected!
            long timeout = System.currentTimeMillis();
            long duration = timeout - start;
            if (duration > 2000) { //the default timeout is 1000ms, if it takes 2000ms to fail then there is something wrong
                throw new AssertionError("TimeoutException not thrown quickly enough: " + timeout);
            }
        } catch (ConnectException e) {
            throw new ServletException(e);
        }

    }

    public void testException(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //should just throw an exception (we're checking we get the right exception even thought it is async internally)
        try {
            bean.connectB();
        } catch (ConnectException e) {
            String expected = "ConnectException: A simple exception";
            String actual = e.getMessage();
            if (!expected.equals(actual)) {
                throw new AssertionError("Expected: " + expected + ", Actual: " + actual);
            }
        }
    }

    /**
     * This test should only pass if MP_Fault_Tolerance_NonFallback_Enabled is set to false
     */
    public void testTimeoutDisabled(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            bean.connectA();
            fail("No exception thrown");
        } catch (ConnectException e) {
            // expected, as Timeout should be disabled
        } catch (TimeoutException e) {
            // Not expected! rethrow
            throw e;
        }
    }

    /**
     * This test ensures that a timeout prompts a retry
     */
    public void testTimeoutWithRetry(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            bean.connectC();
            fail("No exception thrown");
        } catch (TimeoutException e) {
            // Expected, ensure that the correct number of calls have been made
            assertThat("connectC calls", bean.getConnectCCalls(), is(8));
        }
    }

    public void testTimeoutWithRetryAsync(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            bean.connectD().get();
            fail("No exception thrown");
        } catch (ExecutionException e) {
            // Expected, ensure that the cause is correct
            assertThat("Execution exception cause", e.getCause(), instanceOf(TimeoutException.class));
            // Ensure that the correct number of calls have been made
            assertThat("connectD calls", bean.getConnectDCalls(), is(8));
        }
    }

    public void testTimeoutZero(HttpServletRequest request, HttpServletResponse response) throws Exception {
        bean.connectE();
        // No TimeoutException expected
    }

}
