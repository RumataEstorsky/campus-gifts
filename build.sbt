name := "gifts"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.1",
  cache
)     

play.Project.playScalaSettings
