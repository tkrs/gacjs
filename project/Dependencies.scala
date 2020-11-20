import sbt._

object Dependencies {
  lazy val kindProjector   = ("org.typelevel"       %% "kind-projector"   % "0.11.0").cross(CrossVersion.full)
  lazy val organizeImports = "com.github.liancheng" %% "organize-imports" % "0.4.4"

  lazy val googleApiCommon = "com.google.api" % "api-common"  % "1.10.1"
  lazy val catsEffect      = "org.typelevel" %% "cats-effect" % "2.2.0"
  lazy val munit           = "org.scalameta" %% "munit"       % "0.7.18"
}
