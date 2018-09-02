module cryptax.config.properties {
    exports com.cryptax.config;

    requires kotlin.stdlib;
    requires jasypt;
    requires java.management;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.module.kotlin;
}
