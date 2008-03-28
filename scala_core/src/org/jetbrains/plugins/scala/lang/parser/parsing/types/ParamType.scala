package org.jetbrains.plugins.scala.lang.parser.parsing.types

import com.intellij.lang.PsiBuilder, org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes
import org.jetbrains.plugins.scala.lang.parser.bnf.BNF
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.tree.IElementType
import org.jetbrains.plugins.scala.lang.parser.util.ParserUtils
import org.jetbrains.plugins.scala.lang.lexer.ScalaElementType
import org.jetbrains.plugins.scala.ScalaBundle

/** 
* @author Alexander Podkhalyuzin
* Date: 08.02.2008
*/

/*
 * ParamType ::= Type |
 *               '=>' Type |
 *               Type '*'
 */

object ParamType {
  def parse(builder: PsiBuilder): Boolean = {
    val paramTypeMarker = builder.mark
    builder.getTokenType match {
      case ScalaTokenTypes.tFUNTYPE => {
        builder.advanceLexer //Ate '=>'
        if (!Type.parse(builder)) {
          paramTypeMarker.rollbackTo
          return false
        }
        else {
          paramTypeMarker.done(ScalaElementTypes.PARAM_TYPE)
          return true
        }
      }
      case _ => {
        if (!Type.parse(builder,true)) {
          paramTypeMarker.rollbackTo
          return false
        }
        else {
          builder.getTokenText match {
            case "*" => builder.advanceLexer // Ate '*'
            case _ => {/*nothing needs to do*/}
          }
          paramTypeMarker.done(ScalaElementTypes.PARAM_TYPE)
          return true
        }
      }
    }
  }
}