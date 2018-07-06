open module cryptax.app {
	requires kotlin.stdlib;
	requires vertx.core;
	requires vertx.web;
	requires vertx.auth.common;
	requires vertx.auth.jwt;
	requires cryptax.config;
	requires cryptax.controller;
	requires cryptax.validation;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.module.kotlin;
}
