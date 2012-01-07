function kiezatlas_plugin() {

    dm4c.register_css_stylesheet("/de.deepamehta.kiezatlas/style/kiezatlas.css")

    // === REST Client Extension ===

    dm4c.restc.get_website = function(geomap_id) {
        return this.request("GET", "/site/geomap/" + geomap_id)
    }
    dm4c.restc.get_facet_types = function(website_id) {
        return this.request("GET", "/site/" + website_id + "/facets")
    }
    dm4c.restc.get_geo_objects = function(geomap_id) {
        return dm4c.build_topics(this.request("GET", "/site/geomap/" + geomap_id + "/objects"))
    }

    // === Webclient Handler ===

    dm4c.register_plugin_handler("init", function() {
        // site launcher ### TODO: not yet functional
        var match = location.pathname.match(/\/site\/(.+)/)
        if (match) {
            alert("Kiezatlas Site \"" + match[1] + "\"")
        }
    })

    dm4c.register_plugin_handler("pre_render_page", function(topic, page_model) {
        extend_page(topic, page_model, "viewable")
    })

    dm4c.register_plugin_handler("pre_render_form", function(topic, page_model) {
        extend_page(topic, page_model, "editable")
    })

    dm4c.register_plugin_handler("default_page_rendering", function() {
        var topicmap = get_topicmap()
        // If we're not on a geomap we display no list
        if (!is_geomap(topicmap)) {
            return
        }
        //
        var geo_objects = dm4c.restc.get_geo_objects(topicmap.get_id())
        var listing = dm4c.render.topic_list(geo_objects, click_handler, render_handler)
        dm4c.render.page(listing)
        //
        return false    // suppress webclient's default rendering (splash screen)

        function click_handler(topic) {
            dm4c.do_select_topic(topic.id)
        }

        function render_handler(topic) {
            var address = topic.get("dm4.contacts.address")
            if (address) {
                var street      = address.get("dm4.contacts.street")
                var postal_code = address.get("dm4.contacts.postal_code")
                var city        = address.get("dm4.contacts.city")
                return street + ", " + postal_code + " " + city
            } else {
                return "Address unknown"
            }
        }
    })

    // ----------------------------------------------------------------------------------------------- Private Functions

    /**
     * Extends the page model by the website-specific facets.
     * Displays "Show All" button.
     */
    function extend_page(topic, page_model, setting) {
        var topicmap = get_topicmap()
        // If we're not on a geomap we display no facets
        if (!is_geomap(topicmap)) {
            return
        }
        // 1) extend page model
        var website = dm4c.restc.get_website(topicmap.get_id())
        //
        if (!website) {
            alert("This geomap (" + topicmap.get_id() + ") is not part of a Kiezatlas website.")
            return
        }
        //
        var facet_types = dm4c.restc.get_facet_types(website.id).items
        for (var i = 0; i < facet_types.length; i++) {
            var facet_type = dm4c.get_topic_type(facet_types[i].uri)
            var assoc_def = facet_type.assoc_defs[0]
            var topic_type = dm4c.get_topic_type(assoc_def.part_topic_type_uri)
            var field_uri = dm4c.COMPOSITE_PATH_SEPARATOR + assoc_def.uri
            var value_topic = topic.composite[assoc_def.uri]
            var fields = TopicRenderer.create_fields(topic_type, assoc_def, field_uri, value_topic, topic, setting)
            page_model[assoc_def.uri] = fields
        }
        // 2) display "Show All" button
        if (setting == "viewable") {
            create_show_all_button()
        }
    }

    function create_show_all_button() {
        var show_all_button = dm4c.ui.button(do_show_all, "Show All").attr("id", "ka-showall-button")
        dm4c.render.page(show_all_button)

        function do_show_all() {
            dm4c.do_reset_selection()
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
