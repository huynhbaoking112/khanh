How to generate JaCoCo coverage (recommended approach)

Problem:
- Current environment runs tests with JDK 22 (class file major version 66). The JaCoCo agent version previously used cannot instrument those classes.

Recommended (fastest, reliable): run the coverage build using a JDK 17 JVM (JaCoCo 0.8.x is compatible).

Steps (Windows PowerShell):

1) Install or locate a JDK 17 installation on your machine.
   - Example path: `C:\Program Files\Java\jdk-17`

2) Run the tests with the JDK 17 `java` by setting `JAVA_HOME` and updating PATH for the command run only:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
.\mvnw.cmd org.jacoco:jacoco-maven-plugin:0.8.13:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.13:report
```

3) After completion the coverage HTML report will be in `target/site/jacoco/index.html` inside each module (or project root for this single-module project).

Fallback alternatives:
- Upgrade to a JaCoCo release that explicitly supports Java 22 bytecode (if available). We attempted upgrading to `0.8.13` in the repo — if you prefer I can try later plugin versions.
- Run the build inside a container or CI runner configured with JDK 17 and capture the generated `target/site/jacoco` directory.

If you want, I can next:
- Add a set of white-box (branch/exception) unit tests addressing likely uncovered branches (Book validation, repository exception paths, transaction failure handling). Or
- Try successive JaCoCo plugin versions automatically to find one that works on JDK 22.

Tell me which follow-up you prefer, or I can start adding the white-box tests now.