module cryptax.email {
    exports com.cryptax.email;

    requires kotlin.stdlib;
    requires cryptax.domain;
    requires vertx.core;
    requires java.management;
    requires jasypt;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.module.kotlin;
}
