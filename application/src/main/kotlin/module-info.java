open module cryptax.app {
	requires kotlin.stdlib;
	requires vertx.core;
	requires vertx.web;
	requires cryptax.config;
	requires cryptax.controller;
	requires jackson.annotations;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.module.kotlin;
}
