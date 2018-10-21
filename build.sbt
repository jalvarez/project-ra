name := "extract-sbt"
version := "0.1"
scalaVersion := "2.12.3"
libraryDependencies ++= Seq(
    "org.dispatchhttp"      %% "dispatch-core"    % "1.0.0",
    "com.typesafe"          % "config"            % "1.3.2",
    "ch.qos.logback"        %  "logback-classic"  % "1.2.3",
    "io.spray"              %%  "spray-json"      % "1.3.4",
    "org.scalatest"         %% "scalatest"        % "3.0.5" % "test",
    "org.scalamock"         %% "scalamock"        % "4.1.0" % "test"
)
scalacOptions := Seq("-feature", "-language:reflectiveCalls", "-deprecation", "-language:postfixOps",
                     "-language:implicitConversions"),
