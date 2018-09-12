module cryptax.controller {
    exports com.cryptax.controller;
    exports com.cryptax.controller.model;
    exports com.cryptax.controller.validation;

    requires cryptax.domain;
    requires cryptax.parser;
    requires cryptax.usecase;
    requires io.reactivex.rxjava2;
    requires java.validation;
    requires kotlin.stdlib;
}
