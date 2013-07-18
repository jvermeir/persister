name := "persister"

version := "1.0"

scalaVersion := "2.10.1"

resolvers ++= Seq ("Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
                   "Maven central" at "http://repo1.maven.org/‎",
                   "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
                   )

scalacOptions ++= Seq ( "-deprecation", "-feature" )

libraryDependencies ++= Seq (
  "org.scala-lang"                          %   "scala-reflect"               % "2.10.1",
  "commons-io"                              % "commons-io"                    % "2.4",
  "org.scalatest"                           %   "scalatest_2.10"              % "1.9.1"       % "test",
  "org.specs2"                              %%  "specs2"                      % "1.14"        % "test",
  "junit"                                   %   "junit"                       % "4.8.2"       % "test"
)
