name := "hacking-with-argonaut"

scalaVersion := "2.10.3"

fork in console := true

fork := true

fork in run := true

libraryDependencies ++= Seq(
  "io.argonaut"             %% "argonaut"                     % "6.0.4"
, "org.scalaz"              %% "scalaz-core"                  % "7.0.6"
, "net.databinder"          %% "unfiltered"                   % "0.8.0"
, "net.databinder"          %% "unfiltered-filter"            % "0.8.0"
, "net.databinder"          %% "unfiltered-filter-async"      % "0.8.0"
, "net.databinder"          %% "unfiltered-jetty"             % "0.8.0"
, "net.databinder.dispatch" %% "dispatch-core"                % "0.11.1"
, "org.specs2"              %% "specs2-core"                  % "2.3.12"   % "test"
, "org.specs2"              %% "specs2-scalacheck"            % "2.3.12"   % "test"
)

resolvers ++= Seq(
  "oss snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
, "oss releases"  at "http://oss.sonatype.org/content/repositories/releases"
)

scalacOptions := Seq(
  "-deprecation"
, "-unchecked"
, "-Ywarn-all"
, "-Xlint"
, "-feature"
, "-language:_"
)

initialCommands := """
  |import scalaz._, Scalaz._
  |import argonaut._, Argonaut._
  |import argonaut.playground._
  """.stripMargin
