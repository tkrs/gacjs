import Dependencies._

name := "gacjs"
organization := "com.github.tkrs"
homepage := Some(url("https://github.com/tkrs/acjs"))
licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))
developers := List(
  Developer(
    "tkrs",
    "Takeru Sato",
    "type.in.type@gmail.com",
    url("https://github.com/tkrs")
  )
)
scalaVersion := "2.13.3"
crossScalaVersions := Seq("2.13.3", "2.12.12")
scalafmtOnCompile := true
scalafixOnCompile := true
ThisBuild / scalafixDependencies += organizeImports
semanticdbEnabled := true
semanticdbVersion := scalafixSemanticdb.revision
libraryDependencies ++= Seq(googleApiCommon, catsEffect, munit % Test).map(_.withSources())
testFrameworks += new TestFramework("munit.Framework")
fork := true
ThisBuild / scalacOptions ++= compilerOptions ++ warnCompilerOptions ++ {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n >= 13 => Nil
    case _                       => Seq("-Xfuture", "-Ypartial-unification", "-Yno-adapted-args")
  }
}
Compile / console / scalacOptions --= warnCompilerOptions
Compile / console / scalacOptions += "-Yrepl-class-based"

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-feature",
  "-language:_"
)
lazy val warnCompilerOptions = Seq(
  "-Xlint",
  "-Xfatal-warnings",
  "-Ywarn-extra-implicit",
  "-Ywarn-unused:_",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen"
)
