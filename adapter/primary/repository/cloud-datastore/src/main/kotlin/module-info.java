module cryptax.db.cloud.datastore {
    exports com.cryptax.db.cloud.datastore;

    requires cryptax.domain;

    requires google.cloud.datastore;
    requires google.cloud.core;

    requires io.reactivex.rxjava2;
    requires kotlin.stdlib;
    requires org.slf4j;
}
