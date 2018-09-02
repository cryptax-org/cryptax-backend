module cryptax.email {
    exports com.cryptax.email;

    requires kotlin.stdlib;
    requires cryptax.domain;
    requires cryptax.config.properties;
    requires okhttp3;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
}
