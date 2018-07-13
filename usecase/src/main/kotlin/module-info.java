module cryptax.usecase {
    exports com.cryptax.usecase.user;
    exports com.cryptax.usecase.transaction;

    requires kotlin.stdlib;
    requires cryptax.domain;
}
