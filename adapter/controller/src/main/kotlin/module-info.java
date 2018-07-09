module cryptax.controller {
	exports com.cryptax.controller;
	exports com.cryptax.controller.model;

	requires kotlin.stdlib;
	requires cryptax.usecase;
	requires cryptax.domain;

	opens com.cryptax.controller.model to com.fasterxml.jackson.databind;
}
