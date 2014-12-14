import AssemblyKeys._ // put this at the top of the file

import com.typesafe.sbt.SbtStartScript

import com.typesafe.sbt.SbtAspectj._

aspectjSettings

assemblySettings

seq(SbtStartScript.startScriptForClassesSettings: _*)

seq(Revolver.settings: _*)


name := "openkitchen"

organization := "com.xebia"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.4"

scalacOptions := Seq("-encoding", "utf8",
                     "-target:jvm-1.7",
                     "-feature",
                     "-language:implicitConversions",
                     "-language:postfixOps",
                     "-unchecked",
                     "-deprecation",
                     "-Xlog-reflective-calls"
                    )

javaOptions <++= AspectjKeys.weaverOptions in Aspectj

javaOptions += "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

parallelExecution in Test := false

fork in Test := true

fork in run := true

unmanagedResourceDirectories in Compile <++= baseDirectory { base =>
    Seq( base / "src/main/webapp" )
}

mainClass := Some("com.xebia.openkitchen.Boot")

resolvers ++= Seq("Sonatype Releases"   at "http://oss.sonatype.org/content/repositories/releases",
                  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
                  "Sonatype OSS Maven Repository" at "https://oss.sonatype.org/content/groups/public",
                  "Spray Repository"    at "http://repo.spray.io/",
                  "Spray Nightlies"     at "http://nightlies.spray.io/",
                  "Base64 Repo"         at "http://dl.bintray.com/content/softprops/maven")

libraryDependencies ++= {
  val akkaVersion  = "2.3.7"
  val sprayVersion = "1.3.2"
  Seq(
    "com.typesafe.akka"       %% "akka-actor"                     % akkaVersion,
    "com.typesafe.akka"       %% "akka-slf4j"                     % akkaVersion,
    "com.typesafe.akka" 	    %% "akka-persistence-experimental"  % akkaVersion,
    "com.typesafe.akka"       %% "akka-cluster"                   % akkaVersion,
    "com.typesafe.akka"       %% "akka-contrib"                   % akkaVersion,
    "io.spray"                %% "spray-can"                      % sprayVersion,
    "io.spray"                %% "spray-client"                   % sprayVersion,
    "io.spray"                %% "spray-routing"                  % sprayVersion,
    "io.spray"                %% "spray-json"                     % "1.3.1",
    "ch.qos.logback"          %  "logback-classic"                % "1.1.2",
    "io.kamon" %% "kamon-core" % "0.3.6-SNAPSHOT",
    "io.kamon" %% "kamon-log-reporter" % "0.3.6-SNAPSHOT",
    "io.kamon" %% "kamon-system-metrics" % "0.3.6-SNAPSHOT",
    "io.kamon" %% "kamon-spray" % "0.3.6-SNAPSHOT",
    "io.kamon" %% "kamon-statsd" % "0.3.6-SNAPSHOT",
    "io.kamon" %% "kamon-dashboard" % "0.3.6-SNAPSHOT",
    "com.typesafe.akka"       %% "akka-testkit"                   % akkaVersion    % "test",
    "io.spray"                %% "spray-testkit"                  % sprayVersion   % "test",
    "org.scalatest"           %% "scalatest"                      % "2.2.2"        % "test",
    "commons-io"              %  "commons-io"                     % "2.4"          % "test"
)}

//EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

//EclipseKeys.withSource := true

site.settings

site.sphinxSupport()
