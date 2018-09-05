module cryptax.config.di {
    exports com.cryptax.di;

    requires kotlin.stdlib;
    requires cryptax.usecase;
    requires cryptax.domain;
    requires cryptax.id;
    requires cryptax.db.simple;
    requires cryptax.db.cloud.datastore;
    requires cryptax.security;
    requires cryptax.controller;
    requires cryptax.health;
    requires cryptax.price;
    requires cryptax.cache;
    requires cryptax.email;
    requires cryptax.config.properties;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.module.kotlin;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires google.auth.library.oauth2.http;
    requires google.cloud.datastore;
    requires kodein.di.core.jvm;
    requires kodein.di.generic.jvm;
    requires metrics.healthchecks;
    requires okhttp3;
    requires vertx.core;
}
