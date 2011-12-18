function kiezatlas_plugin() {

    // extend REST client
    dm4c.restc.get_website = function(geomap_id) {
        return this.request("GET", "/site/geomap/" + geomap_id)
    }
    dm4c.restc.get_facet_types = function(website_id) {
        return this.request("GET", "/site/" + website_id + "/facets")
    }



    // ***********************************************************
    // *** Webclient Hooks (triggered by deepamehta-webclient) ***
    // ***********************************************************



    this.init = function() {
        var match = location.pathname.match(/\/site\/(.+)/)
        if (match) {
            alert("Kiezatlas Site \"" + match[1] + "\"")
        }
    }

    this.pre_render_page = function(topic, page_model) {
        extend_page(topic, page_model, "viewable")
    }

    this.pre_render_form = function(topic, page_model) {
        extend_page(topic, page_model, "editable")
    }

    // ----------------------------------------------------------------------------------------------- Private Functions

    /**
     * Extends the page model by the website-specific facets.
     */
    function extend_page(topic, page_model, setting) {
        var topicmap = get_topicmap()
        // If we're not on a geomap we display no facets
        if (!is_geomap(topicmap)) {
            return
        }
        var website = dm4c.restc.get_website(topicmap.get_id())
        //
        if (!website) {
            alert("This geomap (" + topicmap.get_id() + ") is not part of a Kiezatlas website.")
            return
        }
        //
        var facet_types = dm4c.restc.get_facet_types(website.id).items
        for (var i = 0; i < facet_types.length; i++) {
            var facet_type = dm4c.type_cache.get_topic_type(facet_types[i].uri)
            var assoc_def = facet_type.assoc_defs[0]
            var topic_type = dm4c.type_cache.get_topic_type(assoc_def.part_topic_type_uri)
            var field_uri = dm4c.COMPOSITE_PATH_SEPARATOR + assoc_def.uri
            var fields = TopicRenderer.create_fields(topic_type, assoc_def, field_uri, topic, topic, setting)
            page_model[assoc_def.uri] = fields
        }
    }

    // ---

    function get_topicmap() {
        return dm4c.get_plugin("topicmaps_plugin").get_topicmap()
    }

    function is_geomap(topicmap) {
        return topicmap.get_renderer_uri() == "dm4.geomaps.geomap_renderer"
    }
}
