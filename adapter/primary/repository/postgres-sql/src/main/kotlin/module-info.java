module cryptax.db.postgres {
    exports com.cryptax.db.postgres;

    requires kotlin.stdlib;
    requires cryptax.domain;
    requires io.reactivex.rxjava2;
    requires java.sql;
    requires org.jooq;
    requires org.slf4j;
}
