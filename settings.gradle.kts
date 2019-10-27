rootProject.name = "cryptax-backend"

include(
    //":application",
    ":application-micronaut",
    ":config",
    ":domain",
    ":usecase",
    ":security",
    ":id-generator",
    ":in-memory-db-simple",
    ":cloud-datastore",
    ":controller",
    ":parser",
    ":email",
    ":health",
    ":price",
    ":cache")

project(":security").projectDir = file("adapter/primary/security")
project(":id-generator").projectDir = file("adapter/primary/id-generator")
project(":in-memory-db-simple").projectDir = file("adapter/primary/repository/in-memory-simple")
project(":cloud-datastore").projectDir = file("adapter/primary/repository/cloud-datastore")
project(":controller").projectDir = file("adapter/secondary/controller")
project(":parser").projectDir = file("adapter/secondary/parser")
project(":email").projectDir = file("adapter/primary/email")
project(":health").projectDir = file("adapter/secondary/health")
project(":price").projectDir = file("adapter/primary/price")
project(":cache").projectDir = file("adapter/secondary/cache")
