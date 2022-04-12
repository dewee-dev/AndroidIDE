/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.itsaky.lsp.java.actions

import com.itsaky.androidide.actions.ActionData
import com.itsaky.lsp.java.R

/** @author Akash Yadav */
class CommentAction : BaseCodeAction() {
    override val id: String = "lsp_java_commentLine"
    override var label: String = ""

    override val titleTextRes: Int = R.string.action_comment_line

    override fun execAction(data: ActionData): Boolean {
        val editor = requireEditor(data)
        val text = editor.text
        val cursor = editor.cursor
        var line = cursor.leftLine

        text.beginBatchEdit()
        while (line >= cursor.leftLine && line <= cursor.rightLine) {
            if (!text.getLineString(line).trim { it <= ' ' }.startsWith("//")) {
                text.insert(line, 0, "//")
            }
            line++
        }
        text.endBatchEdit()

        return true
    }
}
