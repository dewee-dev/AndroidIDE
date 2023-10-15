/*******************************************************************************
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2023  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 ******************************************************************************/

package io.github.rosemoe.sora.editor.ts

import com.itsaky.androidide.treesitter.TSNode
import com.itsaky.androidide.treesitter.TSQueryCursor
import com.itsaky.androidide.treesitter.TSTree
import com.itsaky.androidide.treesitter.api.TreeSitterNode
import com.itsaky.androidide.treesitter.api.TreeSitterQueryCapture
import com.itsaky.androidide.treesitter.api.TreeSitterQueryMatch
import com.itsaky.androidide.utils.ILogger
import io.github.rosemoe.sora.lang.brackets.BracketsProvider
import io.github.rosemoe.sora.lang.brackets.PairedBracket
import io.github.rosemoe.sora.text.Content
import kotlin.math.max

class TsBracketPairs(private val tree: TSTree, private val languageSpec: TsLanguageSpec) :
  BracketsProvider {

  companion object {
    private val log = ILogger.newInstance("TsBracketPairs")

    val OPEN_NAME = "editor.brackets.open"
    val CLOSE_NAME = "editor.brackets.close"

  }

  override fun getPairedBracketAt(text: Content, index: Int): PairedBracket? {
    if (languageSpec.bracketsQuery.patternCount <= 0
      || !languageSpec.bracketsQuery.canAccess()
      || !tree.canAccess()
    ) {
      return null
    }

    TSQueryCursor.create().use { cursor ->
      cursor.setByteRange(max(0, index - 1) * 2, index * 2 + 1)
      val rootNode = tree.rootNode
      cursor.exec(languageSpec.bracketsQuery, rootNode)
      (rootNode as? TreeSitterNode?)?.recycle()
      var match = cursor.nextMatch()
      var matched = false
      while (match != null && !matched) {
        if (languageSpec.bracketsPredicator.doPredicate(languageSpec.predicates, text, match)) {
          var startNode: TSNode? = null
          var endNode: TSNode? = null

          for (capture in match.captures) {
            val captureName = languageSpec.bracketsQuery.getCaptureNameForId(capture.index)
            if (captureName == OPEN_NAME || captureName == CLOSE_NAME) {
              val node = capture.node
              if (index >= node.startByte / 2 && index <= node.endByte / 2) {
                matched = true
              }
              if (captureName == OPEN_NAME) {
                startNode = node
              } else {
                endNode = node
              }
            }

            (capture as? TreeSitterQueryCapture?)?.recycle()
          }
          if (matched && startNode != null && endNode != null) {
            return PairedBracket(startNode.startByte / 2,
              (startNode.endByte - startNode.startByte) / 2, endNode.startByte / 2,
              (endNode.endByte - endNode.startByte) / 2)
          }
        }
        (match as? TreeSitterQueryMatch?)?.recycle()

        if (!tree.canAccess() || rootNode.hasChanges()) {
          val hasChanges = rootNode.hasChanges()
          (rootNode as? TreeSitterNode?)?.recycle()
          log.info("Tree closed or edited while querying, rootNode.hasChanges=$hasChanges")
          return null
        }
        match = cursor.nextMatch()
      }
    }
    return null
  }

}