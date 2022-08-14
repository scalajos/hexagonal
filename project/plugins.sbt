// https://github.com/rtimush/sbt-updates
// check latest dependencies versions
// dependencyUpdates
// sbt-dependency-updates is a gem, and is now included in the base sbt distribution AFAIK.
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.1")

// https://github.com/albuch/sbt-dependency-check
// monitor dependent libraries for known, published vulnerabilities (e.g. CVEs)
// dependencyCheck
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "3.4.0")

// https://github.com/cb372/sbt-explicit-dependencies
// check that your libraryDependencies accurately reflects direct or transitive libraries 
// that your code depends on in order to compile
// undeclaredCompileDependencies
// undeclaredCompileDependenciesTest
// unusedCompileDependencies
// unusedCompileDependenciesTest
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.2.16")

// https://github.com/xerial/sbt-pack
// sbt pack creates a distributable package in target/pack folder.
//    - All dependent jars including scala-library.jar are collected in target/pack/lib folder.
//    This process is much faster than creating a single-jar as in sbt-assembly or proguard plugins.
//    - Generates program launch scripts target/pack/bin/{program name}
//    To run the program no need exists to install Scala, since it is included in the lib folder.
//    Only java command needs to be found in the system.
// sbt packArchive generates tar.gz archive that is ready to distribute.
//    The archive name is target/{project name}-{version}.tar.gz
addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.14")

// https://github.com/sbt/sbt-native-packager
// https://www.scala-sbt.org/sbt-native-packager/index.html
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.9")

// https://github.com/sbt/sbt-assembly
// https://www.baeldung.com/scala/sbt-fat-jar
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.2.0")

// https://github.com/DavidGregory084/sbt-tpolecat
// automagically configuring scalac options according to the project Scala version, inspired by Rob // Norris (@tpolecat)'s excellent series of blog posts providing recommended options to get the most // out of the compiler.
// 2.13.5, 2.13.4, 2.13.3, 2.12.12, 2.11.12, 2.10.7 and Dotty versions 3.0.0-M1, 0.27.0-RC1, 0.26.0
//addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.17")

// https://github.com/geekity/sbt-embedded-postgres

// https://github.com/djspiewak/sbt-github-actions

// https://github.com/ktoso/sbt-jmh

// addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.26")
