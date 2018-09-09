module cryptax.controller {
    exports com.cryptax.controller;
    exports com.cryptax.controller.model;
    exports com.cryptax.controller.validation;

    requires kotlin.stdlib;
    requires cryptax.usecase;
    requires cryptax.domain;
    requires cryptax.parser;
    requires io.reactivex.rxjava2;
    requires java.validation;
}
