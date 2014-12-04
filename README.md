
DM4 Kiezatlas
=============

A geographical content management system as a DeepaMehta 4 module.  
A rewrite of Kiezatlas 1.6.


Requirements
------------

* A DeepaMehta 4.4 installation  
  <https://github.com/jri/deepamehta>


Installation
------------

1. Download the DM4 Kiezatlas plugin:  
   <http://download.deepamehta.de/dm44-kiezatlas-2.1.6.jar>

2. Move the DM4 Kiezatlas plugin to the `deepamehta-4.4/bundle` folder.

3. Restart DeepaMehta.


Usage
-----

* Create a Kiezatlas Website and associate it to a Geomap.
* Create Facet types and associate them to a Kiezatlas Website.


Version History
---------------

**2.1.6** -- Dec 4, 2014

* Add getAllCriteria() to Kiezatlas service.
* Compatible with DeepaMehta 4.4

**2.1.5** -- Jun 8, 2014

* Extended Kiezatlas service: search Geo Objects by name/category, searching categories.
* Kiezatlas service is published as an OSGi service.
* Logical clock for tracking asynchronous requests.
* Bug fix: accessing Geo Objects via REST API when there is no current topicmap.
* Compatible with DeepaMehta 4.3

**2.1.4** -- Feb 18, 2014

* Compatible with DeepaMehta 4.2

**2.1.3** -- Dec 14, 2013

* Compatible with DeepaMehta 4.1.3

**2.1.2** -- Nov 20, 2013

* Compatible with DeepaMehta 4.1.2

**2.1.1** -- Mar 11, 2013

* Compatible with DeepaMehta 4.1

**2.1** -- Jan 13, 2013

* Basic Access Control with 2 Roles: Admin and User.
* Categorization of Geo Objects via multiple-selectable category systems.
* Compatible with DeepaMehta 4.0.13

**2.0.1** -- May 19, 2012

* Compatible with DeepaMehta 4.0.11

**2.0** -- Jan 19, 2012

* Create thematic Kiezatlas Websites: geomaps presenting geo objects.
* Configure which geo object facets are presented at which Kiezatlas Website.
* Compatible with DeepaMehta 4.0.7


------------
Jörg Richter  
Dec 4, 2014
