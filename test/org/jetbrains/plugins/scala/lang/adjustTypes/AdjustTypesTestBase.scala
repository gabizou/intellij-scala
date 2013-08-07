package org.jetbrains.plugins.scala
package lang.adjustTypes

import org.jetbrains.plugins.scala.base.ScalaLightPlatformCodeInsightTestCaseAdapter
import com.intellij.openapi.vfs.{CharsetToolkit, LocalFileSystem}
import java.io.File
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.util.ScalaUtils
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.PsiElement

/**
 * Nikolay.Tropin
 * 7/11/13
 */
abstract class AdjustTypesTestBase extends ScalaLightPlatformCodeInsightTestCaseAdapter {
  private val startMarker = "/*start*/"
  private val endMarker = "/*end*/"

  protected def folderPath = baseRootPath() + "adjustTypes/"

  protected override def rootPath(): String = folderPath

  protected def doTest() {
    import _root_.junit.framework.Assert._
    val filePath = folderPath + getTestName(false) + ".scala"
    val file = LocalFileSystem.getInstance.findFileByPath(filePath.replace(File.separatorChar, '/'))
    assert(file != null, "file " + filePath + " not found")
    val fileText = StringUtil.convertLineSeparators(FileUtil.loadFile(new File(file.getCanonicalPath), CharsetToolkit.UTF8))
    configureFromFileTextAdapter(getTestName(false) + ".scala", fileText)
    val scalaFile = getFileAdapter.asInstanceOf[ScalaFile]
    val offset = fileText.indexOf(startMarker)
    val startOffset = offset + startMarker.length

    assert(offset != -1, "Not specified start marker in test case. Use /*start*/ in scala file for this.")
    val endOffset = fileText.indexOf(endMarker)
    assert(endOffset != -1, "Not specified end marker in test case. Use /*end*/ in scala file for this.")
    val element = PsiTreeUtil.findElementOfClassAtRange(scalaFile, startOffset, endOffset, classOf[PsiElement])

    var res: String = null
    val lastPsi = scalaFile.findElementAt(scalaFile.getText.length - 1)

    try {
      ScalaUtils.runWriteAction(new Runnable {
        def run() {
          ScalaPsiUtil.adjustTypes(element)
        }
      }, getProjectAdapter, "Test")
      res = scalaFile.getText.substring(0, lastPsi.getTextOffset).trim
    }
    catch {
      case e: Exception =>
        println(e)
        assert(assertion = false, message = e.getMessage + "\n" + e.getStackTrace)
    }

    val text = lastPsi.getText
    val output = lastPsi.getNode.getElementType match {
      case ScalaTokenTypes.tLINE_COMMENT => text.substring(2).trim
      case ScalaTokenTypes.tBLOCK_COMMENT | ScalaTokenTypes.tDOC_COMMENT =>
        text.substring(2, text.length - 2).trim
      case _ => {
        assertTrue("Test result must be in last comment statement.", false)
        ""
      }
    }
    assertEquals(output, res)
  }
}