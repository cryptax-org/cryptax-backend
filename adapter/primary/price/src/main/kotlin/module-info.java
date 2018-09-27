module cryptax.price {
    exports com.cryptax.price;

    requires cryptax.domain;
    requires cryptax.cache;

    requires com.fasterxml.jackson.databind;
    requires java.sql;
    requires io.reactivex.rxjava2;
    requires kotlin.stdlib;
    requires okhttp3;
    requires org.slf4j;
}
