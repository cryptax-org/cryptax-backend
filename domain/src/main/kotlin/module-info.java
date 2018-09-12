module cryptax.domain {
    exports com.cryptax.domain.entity;
    exports com.cryptax.domain.exception;
    exports com.cryptax.domain.port;

    requires io.reactivex.rxjava2;
    requires kotlin.stdlib;
    requires org.slf4j;
}
