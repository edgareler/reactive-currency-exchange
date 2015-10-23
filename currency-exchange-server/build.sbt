name := """currency-exchange-server"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "uk.co.panaxiom" %% "play-jongo" % "0.9.0-jongo1.2",
  "org.json" % "json" % "20141113"
)

// libraryDependencies += "uk.co.panaxiom" % "play-jongo_2.10" % "0.9.0-jongo1.2"
//  "uk.co.panaxiom" % "play-jongo" % "0.9.0-jongo1.2"



// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

EclipseKeys.projectFlavor := EclipseProjectFlavor.Java
EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
EclipseKeys.preTasks := Seq(compile in Compile)
