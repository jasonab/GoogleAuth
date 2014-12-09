/*
 * Copyright (c) 2014, Enrico Maria Crisostomo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the name of the author nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.warrenstrange.googleauth;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Not really a unit test, but it shows the basic usage of this package.
 * To properly test the authenticator, manual intervention and multiple steps
 * are required:
 * <ol>
 * <li>Run the test in order to generate the required information for a
 * Google Authenticator application to be configured.</li>
 * <li>Set the <code>SECRET_KEY</code> field with the value generated by the
 * <code>GoogleAuthTest#createCredentials</code> method.</li>
 * <li>Generate the current code with the Google Authenticator application and
 * set the <code>VALIDATION_CODE</code> accordingly.</li>
 * <li>Check that the <code>#authorise</code> method correctly validates the
 * data when invoking the <code>GoogleAuthenticator#authorize</code> method.
 * </li>
 * </ol>
 */
public class GoogleAuthTest {

    // Change this to the saved secret from the running the above test.
    @SuppressWarnings("SpellCheckingInspection")
    private static final String SECRET_KEY = "KR52HV2U5Z4DWGLJ";
    private static final int VALIDATION_CODE = 598775;

    @BeforeClass
    public static void setupMockCredentialRepository() {
        System.setProperty(
                CredentialRepositoryMock.MOCK_SECRET_KEY_NAME,
                SECRET_KEY);
    }

    @Test
    public void createCredentials() {
        GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder gacb =
                new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                        .setKeyRepresentation(KeyRepresentation.BASE64);
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator(gacb.build());

        final GoogleAuthenticatorKey key =
                googleAuthenticator.createCredentials();
        final String secret = key.getKey();
        final List<Integer> scratchCodes = key.getScratchCodes();

        String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthURL("Test Org.", "test@prova.org", key);

        System.out.println("Please register (otpauth uri): " + otpAuthURL);
        System.out.println("Base64-encoded secret key is " + secret);

        for (Integer i : scratchCodes) {
            if (!googleAuthenticator.validateScratchCode(i)) {
                throw new IllegalArgumentException("An invalid code has been " +
                        "generated: this is an application bug.");
            }
            System.out.println("Scratch code: " + i);
        }
    }

    @Test
    public void createCredentialsForUser() {
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

        final GoogleAuthenticatorKey key =
                googleAuthenticator.createCredentials("testName");
        final String secret = key.getKey();
        final List<Integer> scratchCodes = key.getScratchCodes();

        String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthURL("Test Org.", "test@prova.org", key);

        System.out.println("Please register (otpauth uri): " + otpAuthURL);
        System.out.println("Secret key is " + secret);

        for (Integer i : scratchCodes) {
            if (!googleAuthenticator.validateScratchCode(i)) {
                throw new IllegalArgumentException("An invalid code has been " +
                        "generated: this is an application bug.");
            }
            System.out.println("Scratch code: " + i);
        }
    }

    @Test
    public void getCurrentCode() {
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator(8);

        int totpPassword = googleAuthenticator.getCurrentCode("CHNP5U3RRO42ICES");

        System.out.println("Current TOTP password: " + totpPassword);
    }

    @Test
    public void authorise() {
        GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder gacb =
                new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                        .setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(30))
                        .setWindowSize(5);
        GoogleAuthenticator ga = new GoogleAuthenticator(gacb.build());

        boolean isCodeValid = ga.authorize(SECRET_KEY, VALIDATION_CODE);

        System.out.println("Check VALIDATION_CODE = " + isCodeValid);
    }

    @Test
    public void authoriseUser() {
        GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder gacb =
                new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                        .setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(30))
                        .setWindowSize(5);
        GoogleAuthenticator ga = new GoogleAuthenticator(gacb.build());

        boolean isCodeValid = ga.authorizeUser("testName", VALIDATION_CODE);

        System.out.println("Check VALIDATION_CODE = " + isCodeValid);
    }
}
