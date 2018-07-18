module cryptax.controller {
    exports com.cryptax.controller;
    exports com.cryptax.controller.model;

    requires kotlin.stdlib;
    requires cryptax.usecase;
    requires cryptax.domain;
    requires cryptax.parser;
    requires io.reactivex.rxjava2;
}
