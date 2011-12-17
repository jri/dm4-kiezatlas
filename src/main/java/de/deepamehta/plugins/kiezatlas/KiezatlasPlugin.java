package de.deepamehta.plugins.kiezatlas;

import de.deepamehta.core.Association;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.ResultSet;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.AssociationModel;
import de.deepamehta.core.model.AssociationRoleModel;
import de.deepamehta.core.model.CompositeValue;
import de.deepamehta.core.model.TopicModel;
import de.deepamehta.core.model.TopicRoleModel;
import de.deepamehta.core.service.Plugin;

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

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods

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
}
