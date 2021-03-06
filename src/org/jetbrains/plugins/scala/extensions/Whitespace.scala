package org.jetbrains.plugins.scala.extensions

import com.intellij.psi.{PsiElement, PsiWhiteSpace}

/**
 * Pavel Fatin
 */
object Whitespace {
  def unapply(e: PsiElement): Option[String] = Some(e) collect {
    case ws: PsiWhiteSpace => e.getText
  }
}