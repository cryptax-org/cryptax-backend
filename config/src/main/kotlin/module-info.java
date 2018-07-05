module cryptax.config {
	exports com.cryptax.config;

	requires cryptax.usecase;
	requires cryptax.domain;
	requires cryptax.id;
	requires cryptax.db.simple;
	requires cryptax.encoder;
	requires cryptax.controller;
}
