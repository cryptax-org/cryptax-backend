module cryptax.db.cloud.datastore {
    exports com.cryptax.db.cloud.datastore;

    requires kotlin.stdlib;
    requires cryptax.domain;
    requires io.reactivex.rxjava2;
    requires org.slf4j;
    requires google.cloud.datastore;
    requires google.cloud.core;
}
