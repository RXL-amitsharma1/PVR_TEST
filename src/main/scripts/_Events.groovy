/**
 * If you want to see available methods and refs use : binding.variables.each { println it.key }
 *
 */
eventCreateWarStart = {warname, stagingDir ->
    event("BuildInfoStart", [warname, stagingDir])

    def properties = [:]
    Ant.antProject.properties.findAll({k,v-> k.startsWith('environment')}).each { k,v->
        properties[k] = v
    }
    properties['build.version'] = getBuildInfo()
    properties['build.date'] = new Date().format("dd-MMM-yyyy HH:mm:ss zzz")
    println "Setting build info : ${properties['build.version']} @ ${properties['build.date']}"
    writeProperties(properties, "${stagingDir}/WEB-INF/classes/application.properties")

    event("BuildInfoEnd", [warname, stagingDir])

}

private void writeProperties(Map properties, String propertyFile) {
    Ant.propertyfile(file: propertyFile) {
        properties.each { k,v->
            entry(key: k, value: v)
        }
    }
}

private String getBuildInfo() {
    def buildInfo = "UNKNOWN"
    try {
        def REVISION_CMD = "git rev-list HEAD --max-count=1 --abbrev-commit"
        def BRANCH_CMD = "git rev-parse --abbrev-ref HEAD"
        def COUNT_CMD = "git rev-list HEAD | wc -l"

        def rev = executeCommand(REVISION_CMD, "revision number")
        def branch = executeCommand(BRANCH_CMD, "branch name")
        def codeCount = executeCommand(COUNT_CMD, " count commits")

        buildInfo = "${rev}.${branch}"

    } catch (Exception ignore) {
    }
    return buildInfo
}

private String executeCommand(String command, String action) {
    try {
        def proc = command.execute()
        proc.waitFor()
        if (proc.exitValue() == 0) {
            return proc.in.text.trim()
        }
    } catch (IOException e) {
        println "Exception while trying to ${action}"
    }
    return ""
}

