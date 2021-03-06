(ns geo.t-crs
  (:require [geo.crs :as sut]
            [midje.sweet :as m :refer [fact facts]]))

(facts "Identifying and converting EPSG and SRID identifiers"
       (fact "EPSG:2000" => sut/epsg-str?)
       (fact "pizza" => (comp not sut/epsg-str?))
       (fact "EPSG:2000abc" => (comp not sut/epsg-str?))
       (fact " EPSG:2000" => (comp not sut/epsg-str?))
       (fact "pizza EPSG:2000" => (comp not sut/epsg-str?))
       (fact "pizza EPSG:2000.0" => (comp not sut/epsg-str?))
       (fact (sut/srid->epsg-str 2000) => "EPSG:2000")
       (fact (->> sut/valid-crs-prefixes
                  (map #(str % ":2000"))
                  (every? sut/crs-name?)) => true)
       (fact "+proj=merc +lat_ts=56.5 +ellps=GRS80" => sut/proj4-str?)
       (fact "pizza" => (comp not sut/proj4-str?))
       ; Maintain alias of proj4-str? until at least version 3.0
       (fact "+proj=merc +lat_ts=56.5 +ellps=GRS80" => sut/proj4-string?)
       (fact (sut/epsg-str->srid "EPSG:4326") => 4326)
       (fact (sut/epsg-str->srid "pizza") => (m/throws AssertionError))
       (fact (sut/epsg-str->srid "EPSG:4326.0") => (m/throws AssertionError)))

(facts "Creating transformations"
       (fact "Creating transform from SRID ints"
             (let [transform (sut/create-transform 4326 2000)
                   src (.getSourceCRS transform)
                   target (.getTargetCRS transform)]
               (.getName src) => "EPSG:4326"
               (.getName target) => "EPSG:2000"))
       (fact "Creating transform from CRS Strings"
             (let [transform (sut/create-transform "ESRI:37211" "ESRI:37220")
                   src (.getSourceCRS transform)
                   target (.getTargetCRS transform)]
               (.getName src) => "ESRI:37211"
               (.getName target) => "ESRI:37220"))
       (fact "Creating transform from proj4 parameter strings"
             (let [transform (sut/create-transform "+proj=longlat +a=6378270 +b=6356794.343434343 +no_defs"
                                                   "+proj=longlat +a=6376896 +b=6355834.846687363 +no_defs")
                   src (.getSourceCRS transform)
                   target (.getTargetCRS transform)]
               (into [] (.getParameters src)) => ["+proj=longlat" "+a=6378270" "+b=6356794.343434343" "+no_defs"]
               (into [] (.getParameters target)) => ["+proj=longlat" "+a=6376896" "+b=6355834.846687363" "+no_defs"]
               (.getName src) => ""
               (.getName target) => "")))
