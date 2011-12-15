function kiezatlas_plugin() {

    // extend REST client
    dm4c.restc.get_website = function(geomap_id) {
        return this.request("GET", "/site/geomap/" + geomap_id)
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
        var topicmap = get_topicmap()
        if (is_geomap(topicmap)) {
            var website = dm4c.restc.get_website(topicmap.get_id())
            alert("Kiezatlas pre_render_page: website ID=" + website.id)
        }
    }

    // ----------------------------------------------------------------------------------------------- Private Functions

    function get_topicmap() {
        return dm4c.get_plugin("topicmaps_plugin").get_topicmap()
    }

    function is_geomap(topicmap) {
        return topicmap.get_renderer_uri() == "dm4.geomaps.geomap_renderer"
    }
}
