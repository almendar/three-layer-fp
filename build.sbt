libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "1.1.0",
  "org.typelevel" %% "cats-mtl-core" % "0.4.0",
  "com.olegpy" %% "meow-mtl" % "0.2.0" 
)

scalafmtOnCompile in ThisBuild := true

scalaVersion := "2.12.6" 

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")