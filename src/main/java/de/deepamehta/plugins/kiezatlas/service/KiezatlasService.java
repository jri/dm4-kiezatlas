package de.deepamehta.plugins.kiezatlas.service;

import de.deepamehta.plugins.kiezatlas.SearchResult;

import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.core.service.ResultList;

import java.util.List;



public interface KiezatlasService extends PluginService {

    /**
     * Returns the "Kiezatlas Website" topic the given geomap is assigned to.
     */
    Topic getWebsite(long geomapId);

    /**
     * Returns the facet types assigned to the given Kiezatlas Website.
     */
    ResultList<RelatedTopic> getFacetTypes(long websiteId);

    /**
     * Returns all Geo Objects assigned to the given geomap.
     */
    List<Topic> getGeoObjects(long geomapId);

    /**
     * Returns all Geo Objects assigned to the given category.
     */
    List<RelatedTopic> getGeoObjectsByCategory(long categoryId);

    /**
     * Finds all categories that match the search term (case-insensitive substring search)
     * and returns all Geo Objects of those categories, grouped by category.
     *
     * @param   clock   The logical clock value send back to the client (as part of search result).
     *                  Allows the client to order asynchronous responses.
     */
    SearchResult searchGeoObjects(String searchTerm, long clock);
}
