open module cryptax.app {
	requires kotlin.stdlib;
	requires vertx.core;
	requires vertx.web;
	requires cryptax.config;
	requires cryptax.controller;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.module.kotlin;
	requires vertx.auth.common;
	requires vertx.auth.jwt;
	requires com.fasterxml.jackson.annotation;
	requires vertx.web.api.contract;
}
