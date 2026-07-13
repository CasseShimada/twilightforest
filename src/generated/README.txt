The files under this directory should **not** be edited by hand. Add or update the appropriate
provider under src/datagen/java, then run the `runDatagen` Gradle task to regenerate its files.

src/main/resources contains hand-edited resources. src/generated/resources contains legacy output
that is retained while providers are migrated. New Fabric providers write to src/generated/fabric.
