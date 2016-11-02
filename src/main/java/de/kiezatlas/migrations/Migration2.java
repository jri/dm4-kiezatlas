package de.kiezatlas.migrations;

import de.deepamehta.core.model.IndexMode;
import de.deepamehta.core.service.Migration;



public class Migration2 extends Migration {

    @Override
    public void run() {
        dm4.getTopicType("ka2.geo_object.name").addIndexMode(IndexMode.FULLTEXT_KEY);
    }
}
