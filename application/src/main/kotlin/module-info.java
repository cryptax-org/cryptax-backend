open module cryptax.app {
    requires kotlin.stdlib;
    requires vertx.core;
    requires vertx.web;
    requires vertx.auth.jwt;
    requires vertx.auth.common;
    requires vertx.web.api.contract;
    requires vertx.rx.java2;
    requires vertx.dropwizard.metrics;
    requires vertx.mail.client;
    requires io.reactivex.rxjava2;
    requires metrics.healthchecks;
    requires kodein.di.core.jvm;
    requires kodein.di.generic.jvm;
    requires cryptax.domain;
    requires cryptax.config;
    requires cryptax.controller;
    requires cryptax.validation;
    requires cryptax.email;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires vertx.hazelcast;
}
