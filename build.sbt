val scala3Version = "3.1.3" // "3.1.2"

// "dev.zio" %% "zio-metrics-connectors" % "2.0.0" exclude("io.d11", "zhttp_2.13")

lazy val root = project
  .in(file("."))
  .settings(
    name := "hexagonal",
    version := "0.1.0",
    scalaVersion := scala3Version
  )

javacOptions ++= Seq("-source", "17", "-target", "17", "-Xlint")

Compile / unmanagedSourceDirectories += baseDirectory.value / "generated/src/main/scala"

libraryDependencies ++= Seq(
  // ZIO https://github.com/zio
  "dev.zio" %% "zio-prelude" % "1.0.0-RC15",
  "dev.zio" %% "zio" % "2.0.0",
  "dev.zio" %% "zio-json" % "0.3.0-RC10",     // https://zio.github.io/zio-json/
  "dev.zio" %% "zio-json-yaml" % "0.3.0-RC10",
  "dev.zio" %% "zio-logging" % "2.1.0",
  "dev.zio" %% "zio-logging-slf4j" % "2.1.0",
  "dev.zio" %% "izumi-reflect" % "2.1.5",
  "dev.zio" %% "zio-config" % "3.0.2",    // https://zio.github.io/zio-config/
  "dev.zio" %% "zio-config-yaml" % "3.0.2",

  "nl.vroste" %% "rezilience" % "0.9.0",  // https://github.com/svroonland/rezilience/

  "io.d11" %% "zhttp" % "2.0.0-RC10",
                                                       // https://sttp.softwaremill.com/en/latest/
  "com.softwaremill.sttp.client3" %% "core" % "3.7.4", // https://github.com/softwaremill/sttp
  "com.softwaremill.sttp.client3" %% "zio" % "3.7.4",
  "com.softwaremill.sttp.client3" %% "armeria-backend-zio" % "3.7.4",
  "com.softwaremill.sttp.client3" %% "zio-json" % "3.7.4",

  "ch.qos.logback" % "logback-classic" % "1.2.11",

  //libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  //"com.novocode" % "junit-interface" % "0.11" % "test",
  "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
  // http://www.scalacheck.org/
  "org.scalacheck" %% "scalacheck" % "1.16.0" % Test,
  "org.scalactic" %% "scalactic" % "3.2.13" % Test,
  "org.scalatest" %% "scalatest" % "3.2.13" % Test,
  "dev.zio" %% "zio-test" % "2.0.0" % Test,
  "dev.zio" %% "zio-test-intellij" % "1.0.16" % Test,
  "dev.zio" %% "zio-test-scalacheck" % "2.0.0" % Test,
  //"dev.zio" %% "zio-test-magnolia" % "2.0.0" % Test,
  "dev.zio" %% "zio-mock" % "1.0.0-RC8" % Test,
  "dev.zio" %% "zio-test-sbt" % "2.0.0" % Test
  )

enablePlugins(PackPlugin)
