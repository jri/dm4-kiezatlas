package de.deepamehta.plugins.kiezatlas;

import de.deepamehta.core.JSONEnabled;
import de.deepamehta.core.Topic;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.util.DeepaMehtaUtils;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.util.List;



/**
 * A search result of Geo Objects, grouped by category, then grouped by criteria
 */
class SearchResult implements JSONEnabled {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private JSONObject result = new JSONObject();
    private JSONArray criteriaArray = new JSONArray();
    private JSONArray categoriesArray;
    private long currentCriteriaId;

    // ---------------------------------------------------------------------------------------------------- Constructors

    SearchResult() {
        try {
            result.put("search_result", criteriaArray);
        } catch (Exception e) {
            throw new RuntimeException("Constructing a SearchResult failed", e);
        }
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    void add(Topic criteria, Topic category, List<RelatedTopic> geoObjects) {
        try {
            // start new criteria
            if (criteria.getId() != currentCriteriaId) {
                categoriesArray = new JSONArray();
                criteriaArray.put(new JSONObject()
                    .put("criteria", criteria.toJSON())
                    .put("categories", categoriesArray)
                );
                currentCriteriaId = criteria.getId();
            }
            //
            categoriesArray.put(new JSONObject()
                .put("category", category.toJSON())
                .put("geo_objects", DeepaMehtaUtils.objectsToJSON(geoObjects))
            );
        } catch (Exception e) {
            throw new RuntimeException("Adding items to a SearchResult failed", e);
        }
    }

    @Override
    public JSONObject toJSON() {
        return result;
    }
}
