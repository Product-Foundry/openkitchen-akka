import AssemblyKeys._ // put this at the top of the file

import com.typesafe.sbt.SbtStartScript

assemblySettings

seq(SbtStartScript.startScriptForClassesSettings: _*)

seq(Revolver.settings: _*)


name := "openkitchen" 

organization := "com.xebia"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions := Seq("-encoding", "utf8",
                     "-target:jvm-1.6",
                     "-feature",
                     "-language:implicitConversions",
                     "-language:postfixOps",
                     "-unchecked",
                     "-deprecation",
                     "-Xlog-reflective-calls"
                    )

unmanagedBase <<= baseDirectory { base => base / "lib" }

unmanagedResourceDirectories in Compile <++= baseDirectory { base =>
    Seq( base / "src/main/webapp" )
}

parallelExecution in Test := false

mainClass := Some("com.xebia.openkitchen.Boot")

resolvers ++= Seq("Sonatype Releases"   at "http://oss.sonatype.org/content/repositories/releases",
                  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
                  "Sonatype OSS Maven Repository" at "https://oss.sonatype.org/content/groups/public",
                  "Spray Repository"    at "http://repo.spray.io/",
                  "Spray Nightlies"     at "http://nightlies.spray.io/",
                  "Base64 Repo"         at "http://dl.bintray.com/content/softprops/maven")

libraryDependencies ++= {
  val akkaVersion  = "2.3.5"
  val sprayVersion = "1.3.1"
  Seq(
    "com.typesafe.akka"       %%  "akka-actor"             % akkaVersion,
    "com.typesafe.akka"       %%  "akka-slf4j"             % akkaVersion,
    "com.typesafe.akka" 	  %% "akka-persistence-experimental" % akkaVersion,
    "com.typesafe.akka"       %%  "akka-cluster"             % akkaVersion,
    "com.typesafe.akka"       %%  "akka-contrib"             % akkaVersion,
    "io.spray"                %   "spray-caching"          % sprayVersion,
    "io.spray"                %   "spray-can"              % sprayVersion,
    "io.spray"                %   "spray-client"           % sprayVersion,
    "io.spray"                %   "spray-routing"          % sprayVersion,
    "io.spray"                %%  "spray-json"             % "1.2.5",
    "org.json4s" 	      %%  "json4s-native" 	   % "3.2.9",
    "me.lessis"               %%  "base64"                 % "0.1.0",
    "com.github.nscala-time"  %%  "nscala-time"            % "0.4.2",
    "ch.qos.logback"          %   "logback-classic"        % "1.0.12",
    "org.apache.commons"      %   "commons-email"          % "1.2",
    "com.github.ddevore" %% "akka-persistence-mongo-casbah" % "0.7.4-SNAPSHOT",
    "joda-time" 	      %   "joda-time" 	           % "2.0",   
    "org.joda"  	      %   "joda-convert"           % "1.1",
    "junit"                   %   "junit"                  % "4.7" % "test",
    "com.typesafe.akka"       %%  "akka-testkit"           % akkaVersion    % "test",
    "io.spray"                %   "spray-testkit"          % sprayVersion   % "test",
    "org.specs2"              %%  "specs2"                 % "2.1.1"        % "test",
   "org.scalatest"           %  "scalatest_2.10"                 % "2.2.0"        % "test",
    "commons-io"              %  "commons-io"                     % "2.4"          % "test"
)
}

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.withSource := true

site.settings

site.sphinxSupport()
