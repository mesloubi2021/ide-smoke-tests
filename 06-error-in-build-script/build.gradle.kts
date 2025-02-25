// TODO (scenario) Validate broken script behavior when exception happens in script body
//   Instructions:
//   - Remove the comment for the plugins block (line 11 and line 26)
//   - Use the search everywhere dialog to execute the `Load script configurations` command (or use button hovering over the editor)
//   - Wait for the build failure output
//   - Verify that the `configTimeValidatedExtension {}` and the `execTimeValidatedExtension {}` are
//     - Not marked with compiler error
//     - Code completion and jump to definition works for `requiredProperty`
//   - Restore the comment above the plugins block and run project sync to get a clean state

/*
plugins {
    id("plugin.with.config.time.validation") version "1.0.0"
    id("plugin.with.exec.time.validation") version "1.0.0"
}

configTimeValidatedExtension {
    requiredProperty.set("foo")
}

execTimeValidatedExtension {
   requiredProperty.set("bar")
}

throw RuntimeException("Failure in build script")
*/

// TODO (scenario) Validate broken script behavior when exception happens in callback
//   Instructions:
//   - Use the search everywhere dialog to execute the `Load script configurations` command (or use button hovering over the editor)
//     this sets the script config back to where it was without the configuration above
//   - Remove the comment for the plugins block (line 38 and line 55)
//   - Use the search everywhere dialog to execute the `Load script configurations` command (or use button hovering over the editor)
//   - Wait for the build failure output
//   - Verify that the `configTimeValidatedExtension {}` and the `execTimeValidatedExtension {}` are
//     - Marked with compiler error
//     - Code completion and jump to definition does not work for `requiredProperty`
//   - Restore the comment above the plugins block and run project sync to get a clean state

/*
plugins {
    id("plugin.with.config.time.validation") version "1.0.0"
    id("plugin.with.exec.time.validation") version "1.0.0"
}

configTimeValidatedExtension {
    requiredProperty.set("foo")
}

execTimeValidatedExtension {
   requiredProperty.set("bar")
}

afterEvaluate {
    throw RuntimeException("Failure in afterEvaluate hook")
}
 */
