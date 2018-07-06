open module cryptax.app {
	requires kotlin.stdlib;
	requires vertx.core;
	requires vertx.web;
	requires vertx.auth.jwt;
	requires cryptax.config;
	requires cryptax.controller;
	requires cryptax.validation;
}
