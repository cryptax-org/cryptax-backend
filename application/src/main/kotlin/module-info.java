open module cryptax.spring.app {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.module.kotlin;
    requires com.fasterxml.jackson.annotation;

    requires com.hazelcast.core;

    requires cryptax.controller;
    requires cryptax.config;
    requires cryptax.cache;
    requires cryptax.domain;
    requires cryptax.db.simple;
    requires cryptax.db.cloud.datastore;
    requires cryptax.email;
    requires cryptax.health;
    requires cryptax.id;
    requires cryptax.price;
    requires cryptax.security;
    requires cryptax.usecase;

    requires google.cloud.datastore;
    requires google.auth.library.oauth2.http;

    requires io.reactivex.rxjava2;
    requires java.validation;
    requires jjwt;
    requires kotlin.stdlib;
    requires metrics.healthchecks;
    requires okhttp3;
    requires org.slf4j;
    requires reactor.core;
    requires reactor.adapter;

    requires spring.core;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;
    requires spring.beans;
    requires spring.security.core;
    requires spring.security.config;
    requires spring.security.web;
    requires spring.web;
    requires spring.webflux;
}
