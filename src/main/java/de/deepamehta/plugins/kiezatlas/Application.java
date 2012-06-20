package de.deepamehta.plugins.kiezatlas;

import de.deepamehta.core.osgi.Activator;
import de.deepamehta.plugins.webservice.provider.JSONEnabledProvider;
import de.deepamehta.plugins.webservice.provider.JSONEnabledCollectionProvider;

import java.util.HashSet;
import java.util.Set;



public class Application extends javax.ws.rs.core.Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(JSONEnabledProvider.class);
        classes.add(JSONEnabledCollectionProvider.class);
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<Object>();
        singletons.add(Activator.getService().getPlugin("de.deepamehta.kiezatlas"));
        return singletons;
    }
}
