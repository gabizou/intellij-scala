package org.jetbrains.plugins.scala
package config

import java.io.File
import FileAPI._
import com.intellij.openapi.roots.{ModifiableRootModel, JavadocOrderRootType, OrderRootType}
/**
 * Pavel.Fatin, 01.07.2010
 */

object ScalaDistribution {
  val VersionProperty = "version.number"

  def findHome: Option[String] = {
    Option(System.getenv("SCALA_HOME")) orElse {
      Option(System.getenv("PATH")).flatMap {
        _.split(File.pathSeparator)
                .find(_.toLowerCase.contains("scala"))
                .map(_.replaceFirst(File.separator + "?bin$", ""))
      }
    } orElse {
      optional(new File("""C:\Program Files\scala\""")).map(_.getPath)
    }
  }
  def from(home: File): ScalaDistribution = {
    val scala210 = (home / "lib" / "scala-reflect.jar").exists
    if (scala210) new Scala210Distribution(home) else new Scala28Distribution(home)
  }
}

abstract class ScalaDistribution(val home: File) {

  protected case class Pack(classes: String, sources: String, properties: String = "")
  protected def compilerClasses: Seq[File]
  protected def classes: Seq[File]
  protected def sources: Seq[File]
  protected def docs: File
  protected def compilerFile: Option[File]
  protected def libraryFile: Option[File]
  protected def compilerProperties: String

  protected def libraryProperties: String

  private def compilerVersionOption: Option[String] =
    compilerFile.flatMap(readProperty(_, compilerProperties, ScalaDistribution.VersionProperty))

  private def libraryVersionOption: Option[String] =
    libraryFile.flatMap(readProperty(_, libraryProperties, ScalaDistribution.VersionProperty))

  def hasDocs = docs.exists

  def missing: String = (classes ++ sources).filterNot(_ exists).map(_.getName).mkString(", ")

  def compilerPath: String = compilerFile.map(_.getPath).mkString

  def libraryPath: String = libraryFile.map(_.getPath).mkString

  def compilerVersion: String = compilerVersionOption.map(_.replaceFirst("\\.final", "")).mkString

  def libraryVersion: String = libraryVersionOption.map(_.replaceFirst("\\.final", "")).mkString

  def valid: Boolean = libraryFile.isDefined

  def version = compilerVersion
}

class Scala28Distribution(home: File) extends ScalaDistribution(home) {
  private val Compiler = Pack("scala-compiler.jar", "scala-compiler-src.jar", "compiler.properties")
  private val Library = Pack("scala-library.jar", "scala-library-src.jar", "library.properties")
  private val Swing = Pack("scala-swing.jar", "scala-swing-src.jar")
  private val Dbc = Pack("scala-dbc.jar", "scala-dbc-src.jar")

  private val Libs = List(Library, Swing, Dbc)
  private val Lib = home / "lib"
  private val Src = home / "src"

  def compilerClasses = Lib / Compiler.classes :: Lib / Library.classes :: Nil

  def classes = Lib / Libs.map(_.classes)
  def sources = Src / Libs.map(_.sources)
  def docs = home / "doc" / "scala-devel-docs" / "api"

  val compilerFile = optional(Lib / Compiler.classes)
  val libraryFile = optional(Lib / Library.classes)

  val compilerProperties = Compiler.properties
  val libraryProperties = Library.properties
}

class Scala210Distribution(home: File) extends ScalaDistribution(home) {
  private val Compiler = Pack("scala-compiler.jar", "scala-compiler-src.jar", "compiler.properties")
  private val Reflect = Pack("scala-reflect.jar", "scala-reflect-src.jar", "reflect.properties")
  private val Library = Pack("scala-library.jar", "scala-library-src.jar", "library.properties")
  private val Swing = Pack("scala-swing.jar", "scala-swing-src.jar")
  private val Actors = Pack("scala-actors.jar", "scala-actors-src.jar")

  private val Libs = List(Library, Swing, Actors)
  private val Lib = home / "lib"
  private val Src = home / "src"

  def compilerClasses = Lib / Compiler.classes :: Lib / Library.classes :: Lib / Reflect.classes :: Nil

  def classes = Lib / Libs.map(_.classes)
  def sources = Src / Libs.map(_.sources)
  def docs = home / "doc" / "scala-devel-docs" / "api"

  val compilerFile = optional(Lib / Compiler.classes)
  val libraryFile = optional(Lib / Library.classes)

  val compilerProperties = Compiler.properties
  val libraryProperties = Library.properties
}
