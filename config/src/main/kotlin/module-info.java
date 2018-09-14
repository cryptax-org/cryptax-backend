module cryptax.config {
    exports com.cryptax.config;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.module.kotlin;

    requires kotlin.stdlib;
    requires jasypt;
    requires java.management;
}
