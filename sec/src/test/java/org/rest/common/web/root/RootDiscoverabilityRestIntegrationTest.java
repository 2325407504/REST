package org.rest.common.web.root;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.rest.common.client.security.IClientAuthenticationComponent;
import org.rest.common.web.util.HTTPLinkHeaderUtil;
import org.rest.sec.client.SecBusinessPaths;
import org.rest.sec.test.SecGeneralRestIntegrationTest;
import org.rest.sec.util.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

@Ignore("temp")
public class RootDiscoverabilityRestIntegrationTest extends SecGeneralRestIntegrationTest {

    @Autowired
    private SecBusinessPaths paths;
    @Autowired
    private IClientAuthenticationComponent auth;

    // tests

    // GET

    @Test
    public final void whenGetIsDoneOnRoot_thenSomeURIAreDiscoverable() {
        // When
        final Response getOnRootResponse = givenAuthenticated().get(paths.getRootUri());

        // Then
        final List<String> allURIsDiscoverableFromRoot = HTTPLinkHeaderUtil.extractAllURIs(getOnRootResponse.getHeader(HttpHeaders.LINK));

        assertThat(allURIsDiscoverableFromRoot, not(Matchers.<String> empty()));
    }

    @Test
    public final void whenGetIsDoneOnRoot_thenEntityURIIsDiscoverable() {
        // When
        final Response getOnRootResponse = givenAuthenticated().get(paths.getRootUri());

        // Then
        final List<String> allURIsDiscoverableFromRoot = HTTPLinkHeaderUtil.extractAllURIs(getOnRootResponse.getHeader(HttpHeaders.LINK));
        final int indexOfEntityUri = Iterables.indexOf(allURIsDiscoverableFromRoot, Predicates.containsPattern(paths.getUserUri()));
        assertThat(indexOfEntityUri, greaterThanOrEqualTo(0));
    }

    // util

    protected final RequestSpecification givenAuthenticated() {
        return auth.givenBasicAuthenticated(SecurityConstants.ADMIN_USERNAME, SecurityConstants.ADMIN_PASSWORD);
    }

}
