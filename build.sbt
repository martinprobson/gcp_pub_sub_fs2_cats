import com.here.bom.Bom

ThisBuild / scalaVersion     := "3.3.4"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "net.martinprobson.example"
ThisBuild / organizationName := "example"


lazy val googleCloud = Bom.dependencies("com.google.cloud" % "libraries-bom" % "26.50.0")

lazy val root = (project in file("."))
  .settings(googleCloud)
  .settings(Test / fork := true, run / fork := true)
  .settings(
    name := "gcp_pub_sub_fs2_cats",
    libraryDependencies ++= Seq(
      "com.permutive"   %%  "fs2-pubsub"            % "1.1.0",
      "com.permutive"   %%  "gcp-auth"              % "1.2.0",
      "ch.qos.logback"  %   "logback-classic"       % "1.5.12",
      "ch.qos.logback"  %   "logback-core"          % "1.5.12",
      "com.google.cloud" %  "google-cloud-pubsub"   % googleCloud.key.value.toString(),
    )
  )
  .settings(dependencyOverrides ++= googleCloud.key.value)
