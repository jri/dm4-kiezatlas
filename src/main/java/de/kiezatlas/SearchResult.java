package de.kiezatlas;

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
public class SearchResult implements JSONEnabled {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private JSONObject result = new JSONObject();
    private JSONArray criteriaResult = new JSONArray();
    private JSONArray categoriesResult;
    private long currentCriteriaId;

    // ---------------------------------------------------------------------------------------------------- Constructors

    SearchResult(long clock) {
        try {
            result.put("items", criteriaResult);
            result.put("clock", clock);
        } catch (Exception e) {
            throw new RuntimeException("Constructing a SearchResult failed", e);
        }
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    void add(Topic criteria, Topic category, List<RelatedTopic> geoObjects) {
        try {
            // start new criteria
            if (criteria.getId() != currentCriteriaId) {
                categoriesResult = new JSONArray();
                criteriaResult.put(new JSONObject()
                    .put("criteria", criteria.toJSON())
                    .put("categories", categoriesResult)
                );
                currentCriteriaId = criteria.getId();
            }
            //
            categoriesResult.put(new JSONObject()
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
