function kiezatlas_plugin() {

    dm4c.load_stylesheet("/de.deepamehta.kiezatlas/style/kiezatlas.css")

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

    // === Webclient Listeners ===

    dm4c.register_listener("init", function() {
        // set window title
        document.title = "Kiezatlas ${project.version} / " + document.title
        // site launcher ### TODO: not yet functional
        var match = location.pathname.match(/\/site\/(.+)/)
        if (match) {
            alert("Kiezatlas Site \"" + match[1] + "\"")
        }
    })

    dm4c.register_listener("pre_render_page", function(topic, page_model) {
        extend_page(topic, page_model, "viewable")
        // display "Show All" button
        if (is_geomap(get_topicmap())) {
            create_show_all_button()
        }
    })

    dm4c.register_listener("pre_render_form", function(topic, page_model) {
        extend_page(topic, page_model, "editable")
    })

    dm4c.register_listener("default_page_rendering", function() {
        var topicmap = get_topicmap()
        // If we're not on a geomap we display no list
        if (!is_geomap(topicmap)) {
            return
        }
        // fetch geo objects
        var geo_objects = dm4c.restc.get_geo_objects(topicmap.get_id())
        // render list
        var list = dm4c.render.topic_list(geo_objects, click_handler, render_handler)
        dm4c.render.page(list)
        //
        return false    // suppress webclient's default rendering (splash screen)

        function click_handler(topic) {
            var geo_facet = dm4c.get_plugin("geomaps_plugin").get_geo_facet(topic)
            // alert("topic=" + JSON.stringify(topic) + "\n\ngeo_facet=" + JSON.stringify(geo_facet))
            dm4c.do_select_topic(geo_facet.id)
        }

        function render_handler(topic) {
            var address = topic.find_child_topic("dm4.contacts.address")
            if (address) {
                var street      = address.get("dm4.contacts.street")
                var postal_code = address.get("dm4.contacts.postal_code")
                var city        = address.get("dm4.contacts.city")
                return street + ", " + postal_code + " " + city
            } else {
                // ### FIXME: should not happen anymore. If the topic has no address it shoudn't be on a geomap.
                return "Address unknown"
            }
        }
    })

    // ----------------------------------------------------------------------------------------------- Private Functions

    /**
     * Extends the page model by the website-specific facets.
     */
    function extend_page(topic, page_model, setting) {
        // If we're not a geo object we display no facets
        if (topic.type_uri != "dm4.kiezatlas.geo_object") {
            return
        }
        var topicmap = get_topicmap()
        // If we're not on a geomap we display no facets
        if (!is_geomap(topicmap)) {
            return
        }
        // extend page model
        var website = dm4c.restc.get_website(topicmap.get_id())
        //
        if (!website) {
            alert("WARNING: topicmap \"" + topicmap.get_name() + "\" (" + topicmap.get_id() + ") is not associated " +
                "to a Kiezatlas website.\n\nNo geo object facets will be displayed.\n\nTo get rid of this warning " +
                "associate the topicmap to a Kiezatlas website.")
            return
        }
        //
        var facet_types = dm4c.restc.get_facet_types(website.id).items
        dm4c.get_plugin("facets_plugin").add_facets_to_page_model(topic, facet_types, page_model, setting)
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
