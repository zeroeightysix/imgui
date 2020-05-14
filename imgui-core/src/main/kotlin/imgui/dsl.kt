package imgui

import glm_.f
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.ImGui.arrowButton
import imgui.ImGui.begin
import imgui.ImGui.beginChild
import imgui.ImGui.beginChildFrame
import imgui.ImGui.beginColumns
import imgui.ImGui.beginCombo
import imgui.ImGui.beginDragDropSource
import imgui.ImGui.beginDragDropTarget
import imgui.ImGui.beginGroup
import imgui.ImGui.beginMainMenuBar
import imgui.ImGui.beginMenu
import imgui.ImGui.beginMenuBar
import imgui.ImGui.beginPopup
import imgui.ImGui.beginPopupContextItem
import imgui.ImGui.beginPopupContextVoid
import imgui.ImGui.beginPopupContextWindow
import imgui.ImGui.beginPopupModal
import imgui.ImGui.beginTabBar
import imgui.ImGui.beginTabItem
import imgui.ImGui.beginTable
import imgui.ImGui.beginTooltip
import imgui.ImGui.button
import imgui.ImGui.checkbox
import imgui.ImGui.checkboxFlags
import imgui.ImGui.collapsingHeader
import imgui.ImGui.combo
import imgui.ImGui.end
import imgui.ImGui.endChild
import imgui.ImGui.endChildFrame
import imgui.ImGui.endColumns
import imgui.ImGui.endCombo
import imgui.ImGui.endDragDropSource
import imgui.ImGui.endDragDropTarget
import imgui.ImGui.endGroup
import imgui.ImGui.endMainMenuBar
import imgui.ImGui.endMenu
import imgui.ImGui.endMenuBar
import imgui.ImGui.endPopup
import imgui.ImGui.endTabBar
import imgui.ImGui.endTabItem
import imgui.ImGui.endTable
import imgui.ImGui.endTooltip
import imgui.ImGui.imageButton
import imgui.ImGui.indent
import imgui.ImGui.invisibleButton
import imgui.ImGui.listBoxFooter
import imgui.ImGui.listBoxHeader
import imgui.ImGui.menuItem
import imgui.ImGui.popAllowKeyboardFocus
import imgui.ImGui.popButtonRepeat
import imgui.ImGui.popClipRect
import imgui.ImGui.popFont
import imgui.ImGui.popID
import imgui.ImGui.popItemWidth
import imgui.ImGui.popStyleColor
import imgui.ImGui.popStyleVar
import imgui.ImGui.popTextWrapPos
import imgui.ImGui.pushAllowKeyboardFocus
import imgui.ImGui.pushButtonRepeat
import imgui.ImGui.pushClipRect
import imgui.ImGui.pushFont
import imgui.ImGui.pushID
import imgui.ImGui.pushItemWidth
import imgui.ImGui.pushStyleColor
import imgui.ImGui.pushStyleVar
import imgui.ImGui.pushTextWrapPos
import imgui.ImGui.radioButton
import imgui.ImGui.selectable
import imgui.ImGui.smallButton
import imgui.ImGui.treeNode
import imgui.ImGui.treePop
import imgui.ImGui.unindent
import imgui.font.Font
import imgui.internal.ColumnsFlag
import imgui.internal.ColumnsFlags
import kotlin.reflect.KMutableProperty0

/** twin brother of dsl_ */
object dsl {

    // Windows

    fun window(name: String, open: KMutableProperty0<Boolean>? = null, flags: WindowFlags = 0, block: () -> Unit) {
        if (begin(name, open, flags)) // ~open
            if (DEBUG) {
                block()
                end()
            } else try {
                block()
            } finally {
                end()
            }
        else
            end()
    }

    // Child Windows

    fun child(strId: String, size: Vec2 = Vec2(), border: Boolean = false, extraFlags: WindowFlags = 0, block: () -> Unit) {
        if (beginChild(strId, size, border, extraFlags)) // ~open
            if (DEBUG) {
                block()
                endChild()
            } else try {
                block()
            } finally {
                endChild()
            }
        else
            endChild()
    }

    // Parameters stacks (shared)

    fun withFont(font: Font = ImGui.defaultFont, block: () -> Unit) {
        pushFont(font)
        if (DEBUG) {
            block()
            popFont()
        } else try {
            block()
        } finally {
            popFont()
        }
    }

    fun _push(idx: Col, col: Any) {
        if (col is Int)
            pushStyleColor(idx, col)
        else
            pushStyleColor(idx, col as Vec4)
    }

    fun withStyleColor(idx: Col, col: Any, block: () -> Unit) {
        _push(idx, col)
        if (DEBUG) {
            block()
            popStyleColor()
        } else try {
            block()
        } finally {
            popStyleColor()
        }
    }

    fun withStyleColor(
            idx0: Col, col0: Any,
            idx1: Col, col1: Any, block: () -> Unit
    ) {
        _push(idx0, col0)
        _push(idx1, col1)
        if (DEBUG) {
            block()
            popStyleColor(2)
        } else try {
            block()
        } finally {
            popStyleColor(2)
        }
    }

    fun withStyleColor(
            idx0: Col, col0: Any,
            idx1: Col, col1: Any,
            idx2: Col, col2: Any, block: () -> Unit
    ) {
        _push(idx0, col0)
        _push(idx1, col1)
        _push(idx2, col2)
        if (DEBUG) {
            block()
            popStyleColor(3)
        } else try {
            block()
        } finally {
            popStyleColor(3)
        }
    }

    fun withStyleColor(
            idx0: Col, col0: Any,
            idx1: Col, col1: Any,
            idx2: Col, col2: Any,
            idx3: Col, col3: Any,
            block: () -> Unit
    ) {
        _push(idx0, col0)
        _push(idx1, col1)
        _push(idx2, col2)
        _push(idx3, col3)
        if (DEBUG) {
            block()
            popStyleColor(4)
        } else try {
            block()
        } finally {
            popStyleColor(4)
        }
    }

    fun withStyleColor(
            idx0: Col, col0: Any,
            idx1: Col, col1: Any,
            idx2: Col, col2: Any,
            idx3: Col, col3: Any,
            idx4: Col, col4: Any, block: () -> Unit
    ) {
        _push(idx0, col0)
        _push(idx1, col1)
        _push(idx2, col2)
        _push(idx3, col3)
        _push(idx4, col4)
        if (DEBUG) {
            block()
            popStyleColor(5)
        } else try {
            block()
        } finally {
            popStyleColor(5)
        }
    }

    fun withStyleVar(idx: StyleVar, value: Any, block: () -> Unit) {
        pushStyleVar(idx, value)
        if (DEBUG) {
            block()
            popStyleVar()
        } else try {
            block()
        } finally {
            popStyleVar()
        }
    }

    // Parameters stacks (current window)

    fun withItemWidth(itemWidth: Int, block: () -> Unit) = withItemWidth(itemWidth.f, block)
    fun withItemWidth(itemWidth: Float, block: () -> Unit) {
        pushItemWidth(itemWidth)
        if (DEBUG) {
            block()
            popItemWidth()
        } else try {
            block()
        } finally {
            popItemWidth()
        }
    }

    fun withTextWrapPos(wrapPosX: Float = 0f, block: () -> Unit) {
        pushTextWrapPos(wrapPosX)
        if (DEBUG) {
            block()
            popTextWrapPos()
        } else try {
            block()
        } finally {
            popTextWrapPos()
        }
    }

    fun withAllowKeyboardFocus(allowKeyboardFocus: Boolean, block: () -> Unit) {
        pushAllowKeyboardFocus(allowKeyboardFocus)
        if (DEBUG) {
            block()
            popAllowKeyboardFocus()
        } else try {
            block()
        } finally {
            popAllowKeyboardFocus()
        }
    }

    fun <R> withButtonRepeat(repeat: Boolean, block: () -> R): R {
        pushButtonRepeat(repeat)
        return block().also { popButtonRepeat() }
    }


    // Cursor / Layout

    fun indent(indentW: Float = 0f, block: () -> Unit) { // TODO indented?
        indent(indentW)
        if (DEBUG) {
            block()
            unindent(indentW)
        } else try {
            block()
        } finally {
            unindent(indentW)
        }
    }

    fun group(block: () -> Unit) {
        beginGroup()
        if (DEBUG) {
            block()
            endGroup()
        } else try {
            block()
        } finally {
            endGroup()
        }
    }


    // ID stack/scopes

    fun withId(id: Int, block: () -> Unit) {
        pushID(id)
        if (DEBUG) {
            block()
            popID()
        } else try {
            block()
        } finally {
            popID()
        }
    }

    fun withId(id: String, block: () -> Unit) {
        pushID(id)
        if (DEBUG) {
            block()
            popID()
        } else try {
            block()
        } finally {
            popID()
        }
    }

    fun withId(id: Any, block: () -> Unit) {
        pushID(id)
        if (DEBUG) {
            block()
            popID()
        } else try {
            block()
        } finally {
            popID()
        }
    }


    // Widgets: Main

    fun button(label: String, sizeArg: Vec2 = Vec2(), block: () -> Unit) {
        if (button(label, sizeArg))
            block()
    }

    fun smallButton(label: String, block: () -> Unit) {
        if (smallButton(label))
            block()
    }

    fun invisibleButton(strId: String, sizeArg: Vec2, block: () -> Unit) {
        if (invisibleButton(strId, sizeArg))
            block()
    }

    fun arrowButton(id: String, dir: Dir, block: () -> Unit) {
        if (arrowButton(id, dir))
            block()
    }

    fun imageButton(
            userTextureId: TextureID, size: Vec2, uv0: Vec2 = Vec2(), uv1: Vec2 = Vec2(),
            framePadding: Int = -1, bgCol: Vec4 = Vec4(), tintCol: Vec4 = Vec4(1), block: () -> Unit
    ) {
        if (imageButton(userTextureId, size, uv0, uv1, framePadding, bgCol, tintCol))
            block()
    }

    fun checkbox(label: String, vPtr: KMutableProperty0<Boolean>, block: () -> Unit) {
        if (checkbox(label, vPtr))
            block()
    }

    fun checkboxFlags(label: String, vPtr: KMutableProperty0<Int>, flagsValue: Int, block: () -> Unit) {
        if (checkboxFlags(label, vPtr, flagsValue))
            block()
    }

    fun radioButton(label: String, active: Boolean, block: () -> Unit) {
        if (radioButton(label, active))
            block()
    }

    fun radioButton(label: String, v: KMutableProperty0<Int>, vButton: Int, block: () -> Unit) {
        if (radioButton(label, v, vButton))
            block()
    }


    // Widgets: Combo Box


    fun useCombo(label: String, previewValue: String?, flags: ComboFlags = 0, block: () -> Unit) {
        if (beginCombo(label, previewValue, flags))
            if (DEBUG) {
                block()
                endCombo()
            } else try {
                block()
            } finally {
                endCombo()
            }
        else
            endCombo()
    }

    fun combo(
            label: String, currentItem: KMutableProperty0<Int>, itemsSeparatedByZeros: String, heightInItems: Int = -1,
            block: () -> Unit
    ) {
        if (combo(label, currentItem, itemsSeparatedByZeros, heightInItems))
            block()
    }


    // Widgets: Trees

    fun treeNode(label: String, block: () -> Unit) {
        if (treeNode(label))
            if (DEBUG) {
                block()
                treePop()
            } else try {
                block()
            } finally {
                treePop()
            }
    }

    fun treeNode(strId: String, fmt: String, block: () -> Unit) {
        if (treeNode(strId, fmt))
            if (DEBUG) {
                block()
                treePop()
            } else try {
                block()
            } finally {
                treePop()
            }
    }

    fun treeNode(intPtr: Long, fmt: String, block: () -> Unit) {
        if (treeNode(intPtr, fmt))
            if (DEBUG) {
                block()
                treePop()
            } else try {
                block()
            } finally {
                treePop()
            }
    }

//     fun treePushed(intPtr: Long?, block: () -> Unit) { TODO check me
//        treePush(intPtr)
//        try { block() } finally { treePop() }
//    }

    fun collapsingHeader(label: String, flags: TreeNodeFlags = 0, block: () -> Unit) {
        if (collapsingHeader(label, flags))
            block()
    }

    fun collapsingHeader(label: String, open: KMutableProperty0<Boolean>, flags: TreeNodeFlags = 0, block: () -> Unit) {
        if (collapsingHeader(label, open, flags))
            block()
    }


    // Widgets: Selectables

    fun selectable(label: String, selected: Boolean = false, flags: Int = 0, sizeArg: Vec2 = Vec2(), block: () -> Unit) {
        if (selectable(label, selected, flags, sizeArg))
            block()
    }


    // Widgets: Menus

    fun mainMenuBar(block: () -> Unit) {
        if (beginMainMenuBar())
            if (DEBUG) {
                block()
                endMainMenuBar()
            } else try {
                block()
            } finally {
                endMainMenuBar()
            }
    }

    fun menuBar(block: () -> Unit) {
        if (beginMenuBar())
            if (DEBUG) {
                block()
                endMenuBar()
            } else try {
                block()
            } finally {
                endMenuBar()
            }
    }

    fun menu(label: String, enabled: Boolean = true, block: () -> Unit) {
        if (beginMenu(label, enabled))
            if (DEBUG) {
                block()
                endMenu()
            } else try {
                block()
            } finally {
                endMenu()
            }
    }

    fun menuItem(label: String, shortcut: String = "", selected: Boolean = false, enabled: Boolean = true, block: () -> Unit) {
        if (menuItem(label, shortcut, selected, enabled))
            block()
    }


    // Tooltips

    fun tooltip(block: () -> Unit) {
        beginTooltip()
        if (DEBUG) {
            block()
            endTooltip()
        } else try {
            block()
        } finally {
            endTooltip()
        }
    }


    // Popups, Modals

    fun popup(strId: String, flags: WindowFlags = 0, block: () -> Unit) {
        if (beginPopup(strId, flags))
            if (DEBUG) {
                block()
                endPopup()
            } else try {
                block()
            } finally {
                endPopup()
            }
    }

    fun popupContextItem(strId: String = "", mouseButton: MouseButton = MouseButton.Right, block: () -> Unit) {
        if (beginPopupContextItem(strId, mouseButton)) {
            if (DEBUG) {
                block()
                endPopup()
            } else try {
                block()
            } finally {
                endPopup()
            }
        }
    }

    fun popupContextWindow(
            strId: String = "", mouseButton: MouseButton = MouseButton.Right,
            alsoOverItems: Boolean = true, block: () -> Unit
    ) {
        if (beginPopupContextWindow(strId, mouseButton, alsoOverItems))
            if (DEBUG) {
                block()
                endPopup()
            } else try {
                block()
            } finally {
                endPopup()
            }
    }

    fun popupContextVoid(strId: String = "", mouseButton: MouseButton = MouseButton.Right, block: () -> Unit) {
        if (beginPopupContextVoid(strId, mouseButton))
            if (DEBUG) {
                block()
                endPopup()
            } else try {
                block()
            } finally {
                endPopup()
            }
    }

    fun popupModal(name: String, pOpen: KMutableProperty0<Boolean>? = null, extraFlags: WindowFlags = 0, block: () -> Unit) {
        if (beginPopupModal(name, pOpen, extraFlags))
            if (DEBUG) {
                block()
                endPopup()
            } else try {
                block()
            } finally {
                endPopup()
            }
    }


    // Tab Bars, Tabs

    fun tabBar(strId: String, flags: TabBarFlags = 0, block: () -> Unit) {
        if (beginTabBar(strId, flags))
            if (DEBUG) {
                block()
                endTabBar()
            } else try {
                block()
            } finally {
                endTabBar()
            }
    }

    fun tabItem(label: String, pOpen: KMutableProperty0<Boolean>? = null, flags: TabItemFlags = 0, block: () -> Unit) {
        if (beginTabItem(label, pOpen, flags))
            if (DEBUG) {
                block()
                endTabItem()
            } else try {
                block()
            } finally {
                endTabItem()
            }
    }


    // Drag and Drop

    fun dragDropSource(flags: DragDropFlags = 0, block: () -> Unit) {
        if (beginDragDropSource(flags))
            if (DEBUG) {
                block()
                endDragDropSource()
            } else try {
                block()
            } finally {
                endDragDropSource()
            }
    }

    fun dragDropTarget(block: () -> Unit) {
        if (beginDragDropTarget())
            if (DEBUG) {
                block()
                endDragDropTarget()
            } else try {
                block()
            } finally {
                endDragDropTarget()
            }
    }


    // Clipping

    fun withClipRect(clipRectMin: Vec2, clipRectMax: Vec2, intersectWithCurrentClipRect: Boolean, block: () -> Unit) {
        pushClipRect(clipRectMin, clipRectMax, intersectWithCurrentClipRect)
        if (DEBUG) {
            block()
            popClipRect()
        } else try {
            block()
        } finally {
            popClipRect()
        }
    }


    // Miscellaneous Utilities

    fun childFrame(id: ID, size: Vec2, extraFlags: WindowFlags = 0, block: () -> Unit) {
        beginChildFrame(id, size, extraFlags)
        if (DEBUG) {
            block()
            endChildFrame()
        } else try {
            block()
        } finally {
            endChildFrame()
        }
    }

    // Columns TODO -> jDsl

    fun columns(
            strId: String = "", columnsCount: Int,
            flags: ColumnsFlags = ColumnsFlag.None.i, block: () -> Unit
    ) {
        beginColumns(strId, columnsCount, flags)
        if (DEBUG) {
            block()
            endColumns()
        } else try {
            block()
        } finally {
            endColumns()
        }
    }

    // Columns TODO -> jDsl, TODO others

    fun listBox(label: String, sizeArg: Vec2 = Vec2(), block: () -> Unit) {
        if (listBoxHeader(label, sizeArg))
            if (DEBUG) {
                block()
                listBoxFooter()
            } else try {
                block()
            } finally {
                listBoxFooter()
            }
    }

    // tables

    fun table(
            strId: String, columnsCount: Int, flags: TableFlags = TableFlag.None.i, outerSize: Vec2 = Vec2(),
            innerWidth: Float = 0f, block: () -> Unit
    ) {
        if (beginTable(strId, columnsCount, flags, outerSize, innerWidth))
            if (DEBUG) {
                block()
                endTable()
            } else
                try {
                    block()
                } finally {
                    endTable()
                }
    }
}