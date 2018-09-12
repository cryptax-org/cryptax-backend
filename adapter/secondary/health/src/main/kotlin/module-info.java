module cryptax.health {
    exports com.cryptax.health;

    requires cryptax.domain;
    requires metrics.healthchecks;
    requires kotlin.stdlib;
}
