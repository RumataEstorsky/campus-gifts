name := "gifts"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  cache,
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.1",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "com.typesafe.slick" %% "slick-codegen" % "2.1.0",
  "com.typesafe.play" %% "play-slick" % "0.6.1"
)

play.Project.playScalaSettings
