module cryptax.config {
    exports com.cryptax.config;
    exports com.cryptax.config.dto;

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
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.module.kotlin;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires kodein.di.core.jvm;
    requires kodein.di.generic.jvm;
    requires jasypt;
    requires java.management;
    requires metrics.healthchecks;
    requires vertx.auth.jwt;
    requires vertx.auth.common;
    requires vertx.mail.client;
    requires vertx.core;
    requires vertx.rx.java2;
    requires okhttp3;
    requires google.auth.library.oauth2.http;
    requires google.cloud.datastore;
    requires org.apache.commons.io;
}
