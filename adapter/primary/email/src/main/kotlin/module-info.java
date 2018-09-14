module cryptax.email {
    exports com.cryptax.email;

    requires cryptax.domain;
    requires cryptax.config;

    requires com.fasterxml.jackson.databind;
    requires kotlin.stdlib;
    requires okhttp3;
    requires org.slf4j;
}
