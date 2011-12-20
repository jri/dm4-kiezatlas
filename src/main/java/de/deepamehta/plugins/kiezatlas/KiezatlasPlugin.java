package de.deepamehta.plugins.kiezatlas;

import de.deepamehta.plugins.facets.service.FacetsService;

import de.deepamehta.core.Association;
import de.deepamehta.core.AssociationDefinition;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.ResultSet;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.AssociationRoleModel;
import de.deepamehta.core.model.CompositeValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.model.TopicRoleModel;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.Directives;
import de.deepamehta.core.service.Plugin;
import de.deepamehta.core.service.PluginService;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;



@Path("/")
@Consumes("application/json")
@Produces("application/json")
public class KiezatlasPlugin extends Plugin {

    // Website-Geomap association
    private static final String WEBSITE_GEOMAP = "dm4.core.association";
    private static final String ROLE_TYPE_WEBSITE = "dm4.core.default";
    private static final String ROLE_TYPE_GEOMAP = "dm4.core.default";
    // Website-Facet Types association
    private static final String WEBSITE_FACET_TYPES = "dm4.core.association";
    // private static final String ROLE_TYPE_WEBSITE = "dm4.core.default";
    private static final String ROLE_TYPE_FACET_TYPE = "dm4.core.default";

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private FacetsService facetsService;

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods




    // **************************************************
    // *** Core Hooks (called from DeepaMehta 4 Core) ***
    // **************************************************



    @Override
    public void serviceArrived(PluginService service) {
        logger.info("########## Service arrived: " + service);
        if (service instanceof FacetsService) {
            facetsService = (FacetsService) service;
        }
    }

    @Override
    public void serviceGone(PluginService service) {
        logger.info("########## Service gone: " + service);
        if (service == facetsService) {
            facetsService = null;
        }
    }

    // ---

    @Override
    public void postUpdateHook(Topic topic, TopicModel newModel, TopicModel oldModel, ClientState clientState,
                                                                                      Directives directives) {
        if (topic.getTypeUri().equals("dm4.kiezatlas.geo_object")) {
            long topicmapId = clientState.getLong("dm4_topicmap_id");
            logger.info("### Updating geo object facets (topicmapId=" + topicmapId + ")");
            //
            if (!isGeomap(topicmapId)) {
                logger.info("Updating geo object facets ABORTED -- topicmap " + topicmapId + " is not a geomap");
                return;
            }
            //
            Topic website = getWebsite(topicmapId);
            if (website == null) {
                logger.info("Updating geo object facets ABORTED -- geomap " + topicmapId +
                    " is not part of a Kiezatlas website");
                return;
            }
            //
            for (Topic facetType : getFacetTypes(website.getId())) {
                String facetTypeUri = facetType.getUri();
                String assocDefUri = getAssocDef(facetTypeUri).getUri();
                TopicModel facet = newModel.getCompositeValue().getTopic(assocDefUri);
                logger.info("### Adding facet to topic " + topic.getId() + " (facetTypeUri=" + facetTypeUri +
                    ", facet=" + facet + ")");
                facetsService.addFacet(topic, facetTypeUri, facet, clientState, directives);
            }
        }
    }



    // **********************
    // *** Plugin Service ***
    // **********************



    @GET
    @Path("/{url}")
    @Produces("text/html")
    public InputStream launchWebclient() {
        // Note: the template parameters are evaluated at client-side
        try {
            return dms.getPlugin("de.deepamehta.webclient").getResourceAsStream("web/index.html");
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("/geomap/{geomap_id}")
    public Topic getWebsite(@PathParam("geomap_id") long geomapId) {
        try {
            return dms.getTopic(geomapId, false, null).getRelatedTopic(WEBSITE_GEOMAP,
                ROLE_TYPE_WEBSITE, ROLE_TYPE_GEOMAP, "dm4.kiezatlas.site", false, false);
        } catch (Exception e) {
            throw new RuntimeException("Finding the geomap's website topic failed (geomapId=" + geomapId + ")");
        }
    }

    @GET
    @Path("/{website_id}/facets")
    public ResultSet<RelatedTopic> getFacetTypes(@PathParam("website_id") long websiteId) {
        try {
            return dms.getTopic(websiteId, false, null).getRelatedTopics(WEBSITE_FACET_TYPES,
                ROLE_TYPE_WEBSITE, ROLE_TYPE_FACET_TYPE, "dm4.core.topic_type", false, false, 0);
        } catch (Exception e) {
            throw new RuntimeException("Finding the website's facet types failed (websiteId=" + websiteId + ")");
        }
    }

    // ------------------------------------------------------------------------------------------------- Private Methods

    private boolean isGeomap(long topicmapId) {
        Topic topicmap = dms.getTopic(topicmapId, true, null);
        String rendererUri = topicmap.getCompositeValue().getString("dm4.topicmaps.topicmap_renderer_uri");
        return rendererUri.equals("dm4.geomaps.geomap_renderer");
    }

    // ### FIXME: there is a copy in FacetsPlugin.java
    private AssociationDefinition getAssocDef(String facetTypeUri) {
        // Note: a facet type has exactly *one* association definition
        return dms.getTopicType(facetTypeUri, null).getAssocDefs().values().iterator().next();
    }
}
