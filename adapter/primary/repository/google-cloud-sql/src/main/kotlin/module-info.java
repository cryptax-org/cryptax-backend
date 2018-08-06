module cryptax.db.google {
    exports com.cryptax.db.google;

    requires kotlin.stdlib;
    requires cryptax.domain;
    requires io.reactivex.rxjava2;
    requires java.sql;
    requires google.http.client;
    requires java.xml.bind;
    requires org.jooq;
    requires org.slf4j;
}
