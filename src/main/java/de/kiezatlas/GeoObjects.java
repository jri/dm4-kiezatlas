package de.kiezatlas;

import de.deepamehta.core.JSONEnabled;
import de.deepamehta.core.Topic;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;



/**
 * A collection of Geo Objects as returned by the Kiezatlas service (a data transfer object).
 */
public class GeoObjects implements JSONEnabled {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private JSONObject json = new JSONObject();
    private JSONArray items = new JSONArray();

    // ---------------------------------------------------------------------------------------------------- Constructors

    GeoObjects(long clock) {
        try {
            json.put("items", items);
            json.put("clock", clock);
        } catch (Exception e) {
            throw new RuntimeException("Constructing GeoObjects failed", e);
        }
    }

    // -------------------------------------------------------------------------------------------------- Public Methods

    void add(Topic geoObject) {
        try {
            items.put(geoObject.toJSON());
        } catch (Exception e) {
            throw new RuntimeException("Adding geo object to GeoObjects failed", e);
        }
    }

    @Override
    public JSONObject toJSON() {
        return json;
    }
}
