open module cryptax.spring.app {
    requires cryptax.config.properties;
    requires kotlin.stdlib;
    requires org.slf4j;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;
    requires spring.beans;
    requires spring.web;
    requires cryptax.controller;
    requires cryptax.usecase;
    requires cryptax.domain;
    requires cryptax.db.simple;
    requires cryptax.security;
    requires cryptax.id;
    requires okhttp3;
    requires cryptax.email;
    requires cryptax.db.cloud.datastore;
    requires google.cloud.datastore;
    requires google.auth.library.oauth2.http;
    requires com.fasterxml.jackson.databind;
    requires jackson.annotations;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.module.kotlin;
    requires com.fasterxml.jackson.core;
    requires io.reactivex.rxjava2;
    requires java.validation;
}
