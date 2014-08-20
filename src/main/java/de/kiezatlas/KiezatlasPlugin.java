package de.kiezatlas;

import de.kiezatlas.service.KiezatlasService;

import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.plugins.geomaps.service.GeomapsService;
import de.deepamehta.plugins.facets.model.FacetValue;
import de.deepamehta.plugins.facets.service.FacetsService;
import de.deepamehta.core.AssociationDefinition;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.Directives;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.core.service.ResultList;
import de.deepamehta.core.service.annotation.ConsumesService;
import de.deepamehta.core.service.event.PostUpdateTopicListener;
import de.deepamehta.core.service.event.PreSendTopicListener;
import de.deepamehta.core.util.DeepaMehtaUtils;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;



@Path("/site")
@Consumes("application/json")
@Produces("application/json")
public class KiezatlasPlugin extends PluginActivator implements KiezatlasService, PostUpdateTopicListener,
                                                                                  PreSendTopicListener {

    // ------------------------------------------------------------------------------------------------------- Constants

    private static final String TYPE_URI_GEO_OBJECT      = "ka2.geo_object";
    private static final String TYPE_URI_GEO_OBJECT_NAME = "ka2.geo_object.name";

    // Website-Geomap association
    private static final String WEBSITE_GEOMAP = "dm4.core.association";
    private static final String ROLE_TYPE_WEBSITE = "dm4.core.default";     // Note: used for both associations
    private static final String ROLE_TYPE_GEOMAP = "dm4.core.default";
    // Website-Facet Types association
    private static final String WEBSITE_FACET_TYPES = "dm4.core.association";
    private static final String ROLE_TYPE_FACET_TYPE = "dm4.core.default";

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private GeomapsService geomapsService;
    private FacetsService facetsService;

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods




    // **********************
    // *** Plugin Service ***
    // **********************



    @GET
    @Path("/{url}")
    @Produces("text/html")
    public InputStream launchWebclient() {
        try {
            // Note: the template parameters are evaluated at client-side
            return dms.getPlugin("de.deepamehta.webclient").getResourceAsStream("web/index.html");
        } catch (Exception e) {
            throw new RuntimeException("Launching the webclient failed", e);
        }
    }

    @GET
    @Path("/geomap/{geomap_id}")
    @Override
    public Topic getWebsite(@PathParam("geomap_id") long geomapId) {
        try {
            return dms.getTopic(geomapId, false).getRelatedTopic(WEBSITE_GEOMAP, ROLE_TYPE_WEBSITE,
                ROLE_TYPE_GEOMAP, "ka2.website", false, false);
        } catch (Exception e) {
            throw new RuntimeException("Finding the geomap's website topic failed (geomapId=" + geomapId + ")", e);
        }
    }

    @GET
    @Path("/{website_id}/facets")
    @Override
    public ResultList<RelatedTopic> getFacetTypes(@PathParam("website_id") long websiteId) {
        try {
            return dms.getTopic(websiteId, false).getRelatedTopics(WEBSITE_FACET_TYPES, ROLE_TYPE_WEBSITE,
                ROLE_TYPE_FACET_TYPE, "dm4.core.topic_type", false, false, 0);
        } catch (Exception e) {
            throw new RuntimeException("Finding the website's facet types failed (websiteId=" + websiteId + ")", e);
        }
    }

    @GET
    @Path("/criteria")
    @Override
    public List<Topic> getAllCriteria() {
        List<Topic> criteria = dms.getTopics("uri", new SimpleValue("ka2.criteria.*"), false);
        // remove facet types
        Iterator<Topic> i = criteria.iterator();
        while (i.hasNext()) {
            Topic crit = i.next();
            if (crit.getUri().endsWith(".facet")) {
                i.remove();
            }
        }
        //
        return criteria;
    }

    @GET
    @Path("/geomap/{geomap_id}/objects")
    @Override
    public List<Topic> getGeoObjects(@PathParam("geomap_id") long geomapId) {
        try {
            return fetchGeoObjects(geomapId);
        } catch (Exception e) {
            throw new RuntimeException("Fetching the geomap's geo objects failed (geomapId=" + geomapId + ")", e);
        }
    }

    @GET
    @Path("/category/{id}/objects")
    @Override
    public List<RelatedTopic> getGeoObjectsByCategory(@PathParam("id") long categoryId,
                                                      @QueryParam("fetch_composite") boolean fetchComposite) {
        return dms.getTopic(categoryId, false).getRelatedTopics("dm4.core.aggregation", "dm4.core.child",
            "dm4.core.parent", TYPE_URI_GEO_OBJECT, fetchComposite, false, 0).getItems();
    }

    @GET
    @Path("/geoobject")
    @Override
    public GeoObjects searchGeoObjects(@QueryParam("search") String searchTerm, @QueryParam("clock") long clock) {
        GeoObjects result = new GeoObjects(clock);
        for (Topic geoObjectName : dms.searchTopics("*" + searchTerm + "*", TYPE_URI_GEO_OBJECT_NAME)) {
            result.add(getGeoObject(geoObjectName));
        }
        return result;
    }

    @GET
    @Path("/category/objects")
    @Override
    public GroupedGeoObjects searchCategories(@QueryParam("search") String searchTerm,
                                              @QueryParam("clock") long clock) {
        GroupedGeoObjects result = new GroupedGeoObjects(clock);
        for (Topic criteria : getAllCriteria()) {
            for (Topic category : dms.searchTopics("*" + searchTerm + "*", criteria.getUri())) {
                List<RelatedTopic> geoObjects = getGeoObjectsByCategory(category.getId(), false);
                                                                                       // fetchComposite=false
                result.add(criteria, category, geoObjects);
            }
        }
        return result;
    }



    // ****************************
    // *** Hook Implementations ***
    // ****************************



    /**
     * Note: we *wait* for the Access Control service but we don't actually *consume* it.
     * This ensures the Kiezatlas types are properly setup for Access Control.
     */
    @Override
    @ConsumesService({GeomapsService.class, FacetsService.class, AccessControlService.class})
    public void serviceArrived(PluginService service) {
        if (service instanceof GeomapsService) {
            geomapsService = (GeomapsService) service;
        } else if (service instanceof FacetsService) {
            facetsService = (FacetsService) service;
        }
    }

    @Override
    public void serviceGone(PluginService service) {
        if (service == geomapsService) {
            geomapsService = null;
        } else if (service == facetsService) {
            facetsService = null;
        }
    }



    // ********************************
    // *** Listener Implementations ***
    // ********************************



    @Override
    public void preSendTopic(Topic topic, ClientState clientState) {
        if (!topic.getTypeUri().equals(TYPE_URI_GEO_OBJECT)) {
            return;
        }
        //
        ResultList<RelatedTopic> facetTypes = getFacetTypes(clientState);
        if (facetTypes == null) {
            return;
        }
        //
        enrichWithFacets(topic, facetTypes);
    }

    @Override
    public void postUpdateTopic(Topic topic, TopicModel newModel, TopicModel oldModel, ClientState clientState,
                                                                                       Directives directives) {
        if (!topic.getTypeUri().equals(TYPE_URI_GEO_OBJECT)) {
            return;
        }
        //
        ResultList<RelatedTopic> facetTypes = getFacetTypes(clientState);
        if (facetTypes == null) {
            return;
        }
        //
        updateFacets(topic, facetTypes, newModel, clientState, directives);
    }

    // ------------------------------------------------------------------------------------------------- Private Methods




    // === Enrich with facets ===

    private void enrichWithFacets(Topic geoObject, ResultList<RelatedTopic> facetTypes) {
        for (Topic facetType : facetTypes) {
            String facetTypeUri = facetType.getUri();
            if (!isMultiFacet(facetTypeUri)) {
                enrichWithSingleFacet(geoObject, facetTypeUri);
            } else {
                enrichWithMultiFacet(geoObject, facetTypeUri);
            }
        }
    }

    // ---

    private void enrichWithSingleFacet(Topic geoObject, String facetTypeUri) {
        Topic facetValue = facetsService.getFacet(geoObject, facetTypeUri);
        // Note: facetValue is null in 2 cases:
        // 1) The geo object has just been created (no update yet)
        // 2) The geo object has been created outside a geomap and then being revealed in a geomap.
        if (facetValue == null) {
            logger.info("### Enriching geo object " + geoObject.getId() + " with its \"" + facetTypeUri +
                "\" facet value ABORTED -- no such facet in DB");
            return;
        }
        //
        logger.info("### Enriching geo object " + geoObject.getId() + " with its \"" + facetTypeUri +
            "\" facet value (" + facetValue + ")");
        geoObject.getCompositeValue().getModel().put(facetValue.getTypeUri(), facetValue.getModel());
    }

    private void enrichWithMultiFacet(Topic geoObject, String facetTypeUri) {
        List<RelatedTopic> facetValues = facetsService.getFacets(geoObject, facetTypeUri);
        logger.info("### Enriching geo object " + geoObject.getId() + " with its \"" + facetTypeUri +
            "\" facet values (" + facetValues + ")");
        String childTypeUri = getChildTypeUri(facetTypeUri);
        // Note: we set the facet values at once (using put()) instead of iterating (and using add()) as after an geo
        // object update request the facet values are already set. Using add() would result in having the values twice.
        geoObject.getCompositeValue().getModel().put(childTypeUri, DeepaMehtaUtils.toTopicModels(facetValues));
    }



    // === Update facets ===

    private void updateFacets(Topic geoObject, ResultList<RelatedTopic> facetTypes, TopicModel newModel,
                                                                       ClientState clientState, Directives directives) {
        for (Topic facetType : facetTypes) {
            String facetTypeUri = facetType.getUri();
            String childTypeUri = getChildTypeUri(facetTypeUri);
            if (!isMultiFacet(facetTypeUri)) {
                TopicModel facetValue = newModel.getCompositeValueModel().getTopic(childTypeUri);
                logger.info("### Storing facet of type \"" + facetTypeUri + "\" for geo object " + geoObject.getId() +
                    " (facetValue=" + facetValue + ")");
                FacetValue value = new FacetValue(childTypeUri).put(facetValue);
                facetsService.updateFacet(geoObject, facetTypeUri, value, clientState, directives);
            } else {
                List<TopicModel> facetValues = newModel.getCompositeValueModel().getTopics(childTypeUri);
                logger.info("### Storing facets of type \"" + facetTypeUri + "\" for geo object " + geoObject.getId() +
                    " (facetValues=" + facetValues + ")");
                FacetValue value = new FacetValue(childTypeUri).put(facetValues);
                facetsService.updateFacet(geoObject, facetTypeUri, value, clientState, directives);
            }
        }
    }



    // === Helper ===

    /**
     * Returns the facet types for the current topicmap, or null if the facet types can't be determined.
     * There can be several reasons for the latter:
     *   a) there is no "current topicmap". This can be the case with 3rd-party clients.
     *   b) the current topicmap is not a geomap.
     *   c) the geomap is not part of a Kiezatlas Website.
     *
     * @return  The facet types (as a result set, may be empty), or <code>null</code>.
     */
    private ResultList<RelatedTopic> getFacetTypes(ClientState clientState) {
        if (!clientState.has("dm4_topicmap_id")) {
            logger.info("### Finding geo object facet types ABORTED -- topicmap is unknown (no \"dm4_topicmap_id\" " +
                "cookie was sent)");
            return null;
        }
        //
        long topicmapId = clientState.getLong("dm4_topicmap_id");
        if (!isGeomap(topicmapId)) {
            logger.info("### Finding geo object facet types for topicmap " + topicmapId + " ABORTED -- not a geomap");
            return null;
        }
        //
        Topic website = getWebsite(topicmapId);
        if (website == null) {
            logger.info("### Finding geo object facet types for geomap " + topicmapId + " ABORTED -- not part of a " +
                "Kiezatlas website");
            return null;
        }
        //
        logger.info("### Finding geo object facet types for geomap " + topicmapId);
        return getFacetTypes(website.getId());
    }

    private List<Topic> fetchGeoObjects(long geomapId) {
        List<Topic> geoObjects = new ArrayList();
        for (TopicModel geoCoord : geomapsService.getGeomap(geomapId)) {
            Topic geoObject = geomapsService.getDomainTopic(geoCoord.getId());
            geoObjects.add(geoObject);
            // ### TODO: optimization. Include only name and address in returned geo objects.
            // ### For the moment the entire objects are returned, including composite values and facets.
        }
        return geoObjects;
    }

    // ---

    private Topic getGeoObject(Topic geoObjectName) {
        return geoObjectName.getRelatedTopic("dm4.core.composition", "dm4.core.child", "dm4.core.parent",
            TYPE_URI_GEO_OBJECT, false, false); // ### TODO: Core API should provide type-driven navigation
    }

    private boolean isGeomap(long topicmapId) {
        Topic topicmap = dms.getTopic(topicmapId, true);
        String rendererUri = topicmap.getCompositeValue().getString("dm4.topicmaps.topicmap_renderer_uri");
        return rendererUri.equals("dm4.geomaps.geomap_renderer");
    }

    // ---

    // ### FIXME: there is a copy in FacetsPlugin.java
    private boolean isMultiFacet(String facetTypeUri) {
        return getAssocDef(facetTypeUri).getChildCardinalityUri().equals("dm4.core.many");
    }

    // ### FIXME: there is a copy in FacetsPlugin.java
    private String getChildTypeUri(String facetTypeUri) {
        return getAssocDef(facetTypeUri).getChildTypeUri();
    }

    // ### FIXME: there is a copy in FacetsPlugin.java
    private AssociationDefinition getAssocDef(String facetTypeUri) {
        // Note: a facet type has exactly *one* association definition
        return dms.getTopicType(facetTypeUri).getAssocDefs().iterator().next();
    }
}
