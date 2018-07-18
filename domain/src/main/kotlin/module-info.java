module cryptax.domain {
    exports com.cryptax.domain.entity;
    exports com.cryptax.domain.exception;
    exports com.cryptax.domain.port;

    requires kotlin.stdlib;
    requires io.reactivex.rxjava2;
}
