package org.rest.common.web.base;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.hamcrest.core.AnyOf;
import org.junit.Test;
import org.rest.common.client.IEntityOperations;
import org.rest.common.client.marshall.IMarshaller;
import org.rest.common.client.template.IRestTemplate;
import org.rest.common.persistence.model.IEntity;
import org.rest.common.util.LinkUtil;
import org.rest.common.web.util.HTTPLinkHeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import com.google.common.base.Preconditions;
import com.google.common.net.HttpHeaders;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

@ActiveProfiles({ "client", "test", "mime_json" })
public abstract class AbstractDiscoverabilityRestIntegrationTest<T extends IEntity> {

    private Class<T> clazz;

    @Autowired
    private IMarshaller marshaller;

    public AbstractDiscoverabilityRestIntegrationTest(final Class<T> clazzToSet) {
        Preconditions.checkNotNull(clazzToSet);
        clazz = clazzToSet;
    }

    // tests

    // GET (single)

    @Test
    public final void whenResourceIsRetrieved_thenURIToGetAllResourcesIsDiscoverable() {
        // Given
        final String uriOfExistingResource = getApi().createAsUri(createNewEntity(), null);

        // When
        final Response getResponse = getApi().findOneByUriAsResponse(uriOfExistingResource, null);

        // Then
        final String uriToAllResources = HTTPLinkHeaderUtil.extractURIByRel(getResponse.getHeader(HttpHeaders.LINK), LinkUtil.REL_COLLECTION);

        final Response getAllResponse = getApi().findAllByUriAsResponse(uriToAllResources, null);
        assertThat(getAllResponse.getStatusCode(), is(200));
    }

    // GET (paginated)

    @Test
    public final void whenFirstPageOfResourcesIsRetrieved_thenSomethingIsDiscoverable() {
        // When
        final Response response = getApi().findAllPaginatedAsResponse(1, 10, null);

        // Then
        final String linkHeader = response.getHeader(HttpHeaders.LINK);
        assertNotNull(linkHeader);
    }

    @Test
    public final void whenFirstPageOfResourcesIsRetrieved_thenNextPageIsDiscoverable() {
        getApi().createAsUri(createNewEntity(), null);
        getApi().createAsUri(createNewEntity(), null);

        // When
        final Response response = getApi().findAllPaginatedAsResponse(1, 1, null);

        // Then
        final String uriToNextPage = HTTPLinkHeaderUtil.extractURIByRel(response.getHeader(HttpHeaders.LINK), LinkUtil.REL_NEXT);
        assertNotNull(uriToNextPage);
    }

    @Test
    public final void whenFirstPageOfResourcesAreRetrieved_thenSecondPageIsDiscoverable() {
        getApi().createAsUri(createNewEntity(), null);
        getApi().createAsUri(createNewEntity(), null);

        // When
        final Response response = getApi().findAllPaginatedAsResponse(0, 1, null);

        // Then
        final String uriToNextPage = HTTPLinkHeaderUtil.extractURIByRel(response.getHeader(HttpHeaders.LINK), LinkUtil.REL_NEXT);
        assertEquals(getUri() + "?page=1&size=1", uriToNextPage);
    }

    @Test
    public final void whenPageOfResourcesIsRetrieved_thenLastPageIsDiscoverable() {
        getApi().create(createNewEntity());
        getApi().create(createNewEntity());

        // When
        final Response response = getApi().findAllPaginatedAsResponse(0, 1, null);

        // Then
        final String uriToLastPage = HTTPLinkHeaderUtil.extractURIByRel(response.getHeader(HttpHeaders.LINK), LinkUtil.REL_LAST);
        assertNotNull(uriToLastPage);
    }

    @Test
    public final void whenLastPageOfResourcesIsRetrieved_thenNoNextPageIsDiscoverable() {
        // When
        final Response response = getApi().findAllPaginatedAsResponse(1, 1, null);
        final String uriToLastPage = HTTPLinkHeaderUtil.extractURIByRel(response.getHeader(HttpHeaders.LINK), LinkUtil.REL_LAST);

        // Then
        final Response responseForLastPage = getApi().findAllByUriAsResponse(uriToLastPage, null);
        final String uriToNextPage = HTTPLinkHeaderUtil.extractURIByRel(responseForLastPage.getHeader(HttpHeaders.LINK), LinkUtil.REL_NEXT);
        assertNull(uriToNextPage);
    }

    // POST

    @Test
    public final void whenInvalidPOSTIsSentToValidURIOfResource_thenAllowHeaderListsTheAllowedActions() {
        // Given
        final String uriOfExistingResource = getApi().createAsUri(createNewEntity(), null);

        // When
        final Response res = givenAuthenticated().post(uriOfExistingResource);

        // Then
        final String allowHeader = res.getHeader(HttpHeaders.ALLOW);
        assertThat(allowHeader, AnyOf.<String> anyOf(containsString("GET"), containsString("PUT"), containsString("DELETE")));
    }

    @Test
    public final void whenResourceIsCreated_thenURIOfTheNewlyCreatedResourceIsDiscoverable() {
        // When
        final T unpersistedResource = createNewEntity();
        final String uriOfNewlyCreatedResource = getApi().createAsUri(unpersistedResource, null);

        // Then
        final Response response = getApi().findOneByUriAsResponse(uriOfNewlyCreatedResource, null);
        final T resourceFromServer = marshaller.decode(response.body().asString(), clazz);
        assertThat(unpersistedResource, equalTo(resourceFromServer));
    }

    // template method

    protected abstract IRestTemplate<T> getApi();

    protected abstract IEntityOperations<T> getEntityOps();

    protected abstract String getUri();

    protected abstract void change(final T resource);

    protected abstract T createNewEntity();

    protected abstract RequestSpecification givenAuthenticated();

}
