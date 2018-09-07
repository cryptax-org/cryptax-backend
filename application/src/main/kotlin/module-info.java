open module cryptax.app {
    requires kotlin.stdlib;
    requires vertx.auth.jwt;
    requires vertx.auth.common;
    requires vertx.core;
    requires vertx.dropwizard.metrics;
    requires vertx.hazelcast;
    requires vertx.rx.java2;
    requires vertx.web;
    requires vertx.web.api.contract;
    requires metrics.healthchecks;
    requires kodein.di.core.jvm;
    requires kodein.di.generic.jvm;
    requires cryptax.domain;
    requires cryptax.config.properties;
    requires cryptax.config.di;
    requires cryptax.controller;
    requires cryptax.validation;
    requires cryptax.email;
    requires cryptax.cache;
    requires org.slf4j;
    requires io.reactivex.rxjava2;
}
