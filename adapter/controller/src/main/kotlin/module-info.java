module cryptax.controller {
    exports com.cryptax.controller;
    exports com.cryptax.controller.model;

    requires kotlin.stdlib;
    requires cryptax.usecase;
    requires cryptax.domain;
}
