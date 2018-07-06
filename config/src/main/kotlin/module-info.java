module cryptax.config {
	exports com.cryptax.config;

	requires cryptax.usecase;
	requires cryptax.domain;
	requires cryptax.id;
	requires cryptax.db.simple;
	requires cryptax.encoder;
	requires cryptax.controller;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.module.kotlin;
	requires com.fasterxml.jackson.annotation;
	requires vertx.auth.jwt;
	requires vertx.auth.common;
}
