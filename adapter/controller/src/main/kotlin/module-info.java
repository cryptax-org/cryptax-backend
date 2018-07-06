module cryptax.controller {
	exports com.cryptax.controller;
	exports com.cryptax.controller.model;

	requires kotlin.stdlib;
	requires cryptax.usecase;
	requires cryptax.domain;
	requires vertx.web;
	requires vertx.core;
	//requires jackson.annotations;
	requires vertx.auth.jwt;
	requires vertx.auth.common;

	opens com.cryptax.controller.model to com.fasterxml.jackson.databind;
}
