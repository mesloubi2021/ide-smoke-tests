/**
 * Generates instructions on how to execute manual IDE smoke tests.
 *
 * The script iterates through all (Java, Groovy, Kotlin and Gradle) source files, grabs the comments starting with
 * `// TODO (scenario)` and outputs the list of scenarios along with instructions on how to perform the
 * tests.
 *
 * The output uses the GitHub Markup format, so the result can be copy-pasted into a GitHub issue.
 */

import groovy.transform.Canonical
import static groovy.io.FileType.FILES

renderTestPlan(collectAllSamples(new File(System.getProperty('user.dir'))))

List<Sample> collectAllSamples(File rootDir) {
    rootDir.listFiles()
        .findAll { File file -> file.directory && file.name.matches('\\d+-.*') }
        .sort { a, b -> a.name.compareTo(b.name) }
        .collect { File sampleDir ->
            String sampleName = sampleDir.name.replaceFirst('\\d+-', '')
            List<Scenario> scenarios = findAndReadAllScenarios(sampleDir)
            new Sample(sampleName, scenarios)
        }
}

List<Scenario> findAndReadAllScenarios(File sampleDir) {
    List<Scenario> scenarios = []
    sampleDir.eachFileRecurse(FILES) { File file ->
        scenarios += readScenarios(file)
    }
    scenarios
}

List<Scenario> readScenarios(File file) {
    List<Scenario> scenarios = []
    if (isSourceFile(file)) {
        file.text.eachLine { line ->
            if (line.contains("TODO (scenario)")) {
                scenarios += new Scenario(line.replaceFirst('TODO \\(scenario\\)', '').replaceFirst('//', '').replaceFirst('\\s+', '') as String)
            }
        }
    }
    scenarios
}

boolean isSourceFile(File file) {
    ['.gradle.kts', '.gradle', '.groovy', '.kt', '.java'].find { file.name.endsWith(it) }
}

void renderTestPlan(List<Sample> samples) {
    Calendar calendar = Calendar.getInstance()
    def year = calendar.get(Calendar.YEAR)
    def weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
    println """
        ---
        name: Test Iteration ${year}.${weekOfYear}
        about: Instructions to execute manual IDE smoke tests
        title: Test Iteration ${year}.${weekOfYear}
        labels: test-iteration
        ---

        # Test Plan

        ## 1. Set up local test environment

        - [ ] Assing this ticket to yourself and move it to the _In Progress_ column.
        - [ ] Install IntelliJ IDEA (if you don't already have it installed), both the EAP and latest stable releases
          - Tip: you can use the [Toolbox App](https://www.jetbrains.com/toolbox-app/) to manage multiple installations
        - [ ] Clone [this](https://github.com/gradle/ide-smoke-tests) repository
        - [ ] Download [jq](https://jqlang.github.io/jq/download/)  
        - [ ] Update Gradle wrappers in all sample projects to the latest snapshot
          - One-liner bash/zsh update command: `for sample in \$(ls | grep -e '^[0-9][0-9].*'); do cd \$sample; ./gradlew wrapper --gradle-version nightly; cd -; done`
        - [ ] Run `git clean -fdx` to remove build artifacts from all sample projects
        - [ ] Make sure [`Android Home`](https://developer.android.com/studio/command-line/variables#envar) is configured in IDEA
          -  Go Preferences > Build, Execution, Deployment > Android > Android Project Structure > SDK Location
        - [ ] Make sure that for scenario `03-multiproject` JDK 17 is used (Spring Boot plugin requirement)

        ## 2. Document components
        
        - [ ] Operating System:
          - Click the Apple icon, About This Mac
        - [ ] Gradle version: 
          - run `gradle --version`
        - [ ] IntelliJ Idea EAP version (with build number):
          - Open JetBrains Toolbox, click the Settings gear icon next to an install, go to the About tab
        - [ ] IntelliJ Idea Stable version (with build number): 

        ## 3. Verify scenarios

        These scenarios are automatically generated from the lines starting with `TODO (scenario)` in the build script and code files.

        The sample projects are located in the numbered folders in the repository root. For each project, follow the steps below:

        - Launch IDEA EAP
        - Import the sample project into IDEA and wait for the project sync to finish
        - Use text search to locate the scenario (listed below) in the build and follow the instructions
        - Check off the verified scenario in the list below

        In case of a failure:

        - Verify the same scenario in the latest released IDEA version
        - Open an issue at https://youtrack.jetbrains.com
        - Link the issue in the sample project:
          -  Add the following comment to the failing line `// Known issue: https://youtrack.jetbrains.com/issue/IDEA-123456`
        - add an X (red cross emoji) to the list below and link the created issue there as well
    """.stripIndent(8).strip()

    samples.each { Sample sample ->
        // omit sample with no scenarios
        if (sample.scenarios) {
            println ""
            println "### Scenarios in `$sample.name`"
            println ""
            sample.scenarios.each { Scenario scenario ->
                println "- [ ] ${scenario.name}"
            }
        }
    }
    println """
        ## 4. Finalize
        
        - [ ] If you encountered a scenario that has been fixed, then remove the `Known issue` reference from the sources.
        - [ ] Commit and push all changes (including the wrapper update)
        - [ ] Tag the issue with the `new-regresssion-reported` label if you reported something to JetBrains
        - [ ] Close this issue
        - [ ] Consider improving the test plan
          - If you find that some scenarios are incorrect, redundant, missing, etc. please provide a PR and ask the @bt-ide-experience-team for a review
          - You can update the issue template here: https://github.com/gradle/ide-smoke-tests/blob/main/generateTestPlan.groovy
    """.stripIndent(8)
}

@Canonical
class Sample {
    final String name
    final List<Scenario> scenarios
}

@Canonical
class Scenario {
    final String name
}
