open module cryptax.spring.app {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.module.kotlin;

    requires cryptax.controller;
    requires cryptax.config.properties;
    requires cryptax.domain;
    requires cryptax.db.simple;
    requires cryptax.db.cloud.datastore;
    requires cryptax.email;
    requires cryptax.id;
    requires cryptax.security;
    requires cryptax.usecase;

    requires google.cloud.datastore;
    requires google.auth.library.oauth2.http;

    requires io.reactivex.rxjava2;
    requires java.validation;
    requires jjwt;
    requires kotlin.stdlib;
    requires okhttp3;
    requires org.slf4j;
    requires reactor.core;

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
    requires reactor.adapter;
}
