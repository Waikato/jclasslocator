# RELEASE

* Use the following command to make a new release:

  ```
  mvn release:clean release:prepare release:perform
  ```

* push all changes

* go to the following URL and publish the artifact:

  ```
  https://central.sonatype.com/publishing/deployments
  ```

* Update the Maven artifact version in [README.md](README.md#maven)

