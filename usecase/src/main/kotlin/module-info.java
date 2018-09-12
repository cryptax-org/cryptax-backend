module cryptax.usecase {
    exports com.cryptax.usecase.user;
    exports com.cryptax.usecase.transaction;
    exports com.cryptax.usecase.report;

    requires cryptax.domain;
    requires io.reactivex.rxjava2;
    requires kotlin.stdlib;
    requires org.slf4j;
}
