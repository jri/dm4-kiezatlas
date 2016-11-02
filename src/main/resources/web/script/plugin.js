dm4c.add_plugin("de.kiezatlas", function() {

    // === REST Client Extension ===

    dm4c.restc.get_website = function(geomap_id) {
        return this.request("GET", "/site/geomap/" + geomap_id)
    }
    dm4c.restc.get_facet_types = function(website_id) {
        return this.request("GET", "/site/" + website_id + "/facets")
    }
    dm4c.restc.get_geo_objects = function(geomap_id, include_childs) {
        var params = this.queryParams({include_childs: include_childs})
        return dm4c.build_topics(this.request("GET", "/site/geomap/" + geomap_id + "/objects" + params))
    }

    // === Webclient Listeners ===

    dm4c.add_listener("init", function() {
        // set window title
        document.title = "Kiezatlas ${project.version} / " + document.title
        // site launcher ### TODO: not yet functional
        var match = location.pathname.match(/\/site\/(.+)/)
        if (match) {
            alert("Kiezatlas Site \"" + match[1] + "\"")
        }
    })

    dm4c.add_listener("pre_render_page", function(topic, page_model) {
        extend_page(topic, page_model, dm4c.render.page_model.mode.INFO)
        // display "Show All" button
        if (is_geomap(get_topicmap())) {
            create_show_all_button()
        }
    })

    dm4c.add_listener("pre_render_form", function(topic, page_model) {
        extend_page(topic, page_model, dm4c.render.page_model.mode.FORM)
    })

    dm4c.add_listener("default_page_rendering", function() {
        var topicmap = get_topicmap()
        // If we're not on a geomap we display no list
        if (!is_geomap(topicmap)) {
            return
        }
        // fetch geo objects
        var geo_objects = dm4c.restc.get_geo_objects(topicmap.get_id(), true)   // include_childs=true
        // render list
        var list = dm4c.render.topic_list(geo_objects, click_handler, render_handler)
        dm4c.render.page(list)
        //
        return false    // suppress webclient's default rendering (splash screen)

        function click_handler(topic) {
            var geo_coordinate = dm4c.get_plugin("de.deepamehta.geomaps").get_geo_coordinate(topic)
            dm4c.do_select_topic(geo_coordinate.id)
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
     *
     * @param   topic           the topic to be rendered.
     * @param   page_model      the page model to be extended.
     * @param   render_mode     dm4c.render.page_model.mode.INFO or
     *                          dm4c.render.page_model.mode.FORM
     */
    function extend_page(topic, page_model, render_mode) {
        // If we're not a geo object we display no facets
        if (topic.type_uri != "ka2.geo_object") {
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
        var facet_types = dm4c.restc.get_facet_types(website.id)
        dm4c.get_plugin("de.deepamehta.facets").add_facets_to_page_model(topic, facet_types, page_model, render_mode)
    }

    function create_show_all_button() {
        var show_all_button = dm4c.ui.button({on_click: do_show_all, label: "Show All"}).attr("id", "ka-showall-button")
        dm4c.render.page(show_all_button)

        function do_show_all() {
            dm4c.do_reset_selection()
        }
    }

    // ---

    function get_topicmap() {
        return dm4c.get_plugin("de.deepamehta.topicmaps").get_topicmap()
    }

    function is_geomap(topicmap) {
        return topicmap.get_renderer_uri() == "dm4.geomaps.geomap_renderer"
    }
})
// Enable debugging for dynamically loaded scripts:
//# sourceURL=kiezatlas_plugin.js
