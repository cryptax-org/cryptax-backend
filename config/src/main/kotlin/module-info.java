module cryptax.config {
    exports com.cryptax.config;

    requires kotlin.stdlib;
    requires cryptax.usecase;
    requires cryptax.domain;
    requires cryptax.id;
    requires cryptax.db.simple;
    requires cryptax.security;
    requires cryptax.controller;
    requires cryptax.email;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.module.kotlin;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires vertx.auth.jwt;
    requires vertx.auth.common;
    requires jasypt;
    requires java.management;
    requires metrics.healthchecks;
    requires cryptax.health;
}
