package imgui.demo

import glm_.f
import glm_.i
import glm_.vec2.Vec2
import imgui.*
import imgui.ImGui.alignTextToFramePadding
import imgui.ImGui.beginTable
import imgui.ImGui.bulletText
import imgui.ImGui.button
import imgui.ImGui.checkbox
import imgui.ImGui.checkboxFlags
import imgui.ImGui.collapsingHeader
import imgui.ImGui.combo
import imgui.ImGui.dragFloat
import imgui.ImGui.dragInt
import imgui.ImGui.dragVec2
import imgui.ImGui.endTable
import imgui.ImGui.indent
import imgui.ImGui.inputText
import imgui.ImGui.io
import imgui.ImGui.popButtonRepeat
import imgui.ImGui.popID
import imgui.ImGui.popItemWidth
import imgui.ImGui.popStyleColor
import imgui.ImGui.popStyleVar
import imgui.ImGui.pushButtonRepeat
import imgui.ImGui.pushID
import imgui.ImGui.pushItemWidth
import imgui.ImGui.pushStyleColor
import imgui.ImGui.pushStyleVar
import imgui.ImGui.sameLine
import imgui.ImGui.selectable
import imgui.ImGui.separator
import imgui.ImGui.setNextItemOpen
import imgui.ImGui.setNextItemWidth
import imgui.ImGui.smallButton
import imgui.ImGui.spacing
import imgui.ImGui.style
import imgui.ImGui.tableAutoHeaders
import imgui.ImGui.tableGetColumnIsSorted
import imgui.ImGui.tableGetColumnName
import imgui.ImGui.tableGetSortSpecs
import imgui.ImGui.tableHeader
import imgui.ImGui.tableNextCell
import imgui.ImGui.tableNextRow
import imgui.ImGui.tableSetColumnIndex
import imgui.ImGui.tableSetupColumn
import imgui.ImGui.text
import imgui.ImGui.textDisabled
import imgui.ImGui.textUnformatted
import imgui.ImGui.textWrapped
import imgui.ImGui.treeNodeEx
import imgui.ImGui.treePop
import imgui.ImGui.unindent
import imgui.api.demoDebugInformations.Companion.helpMarker
import imgui.classes.DrawList
import imgui.classes.ListClipper
import imgui.classes.TableSortSpecs
import imgui.dsl.indent
import imgui.dsl.table
import imgui.dsl.treeNode
import imgui.TableColumnFlag as Tcf
import imgui.TableFlag as Tf

object ShowDemoWindowTables {

    // Options
    var disableIndent = false

    operator fun invoke() {

        //ImGui::SetNextItemOpen(true, ImGuiCond_Once);
        if (!collapsingHeader("Tables & Columns"))
            return

        pushID("Tables")

        var openAction = -1
        if (button("Open all"))
            openAction = 1
        sameLine()
        if (button("Close all"))
            openAction = 0
        sameLine()

        // Options
        checkbox("Disable tree indentation", ::disableIndent)
        sameLine()
        helpMarker("Disable the indenting of tree nodes so demo tables can use the full window width.")
        separator()
        if (disableIndent)
            pushStyleVar(StyleVar.IndentSpacing, 0f)

        // About Styling of tables
        // Most settings are configured on a per-table basis via the flags passed to BeginTable() and TableSetupColumns APIs.
        // There are however a few settings that a shared and part of the ImGuiStyle structure:
        //   style.CellPadding                          // Padding within each cell
        //   style.Colors[ImGuiCol_TableHeaderBg]       // Table header background
        //   style.Colors[ImGuiCol_TableBorderStrong]   // Table outer and header borders
        //   style.Colors[ImGuiCol_TableBorderLight]    // Table inner borders
        //   style.Colors[ImGuiCol_TableRowBg]          // Table row background when ImGuiTableFlags_RowBg is enabled (even rows)
        //   style.Colors[ImGuiCol_TableRowBgAlt]       // Table row background when ImGuiTableFlags_RowBg is enabled (odds rows)

        // Demos
        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        basic()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        bordersBackground()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        resizableStretch()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        resizableFixed()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        resizableMixed()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        reorderableHideableWithHeaders()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        verticalScrollingWithClipping()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        horizontalScrolling()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        columnsFlags()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        recursive()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        sizingPoliciesCellContents()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        compactTable()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        rowHeight()


        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        treeView()

        // Demonstrate using TableHeader() calls instead of TableAutoHeaders()
        // FIXME-TABLE: Currently this doesn't get us feature-parity with TableAutoHeaders(), e.g. missing context menu.  Tables API needs some work!
        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        customHeaders()

        // This is a simplified version of the "Advanced" example, where we mostly focus on the code necessary to handle sorting.
        // Note that the "Advanced" example also showcase manually triggering a sort (e.g. if item quantities have been modified)
        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        sorting()

        if (openAction != -1)
            setNextItemOpen(openAction != 0)
        advanced()

        popID()

        ShowDemoWindowColumns()

        if (disableIndent)
            popStyleVar()
    }

    fun basic() = treeNode("Basic") {
        // Here we will showcase 4 different ways to output a table. They are very simple variations of a same thing!

        // Basic use of tables using TableNextRow() to create a new row, and TableSetColumnIndex() to select the column.
        // In many situations, this is the most flexible and easy to use pattern.
        helpMarker("Using TableNextRow() + calling TableSetColumnIndex() _before_ each cell, in a loop.")
        if (beginTable("##table1", 3)) {
            for (row in 0..3) {
                tableNextRow()
                for (column in 0..2) {
                    tableSetColumnIndex(column)
                    text("Row $row Column $column")
                }
            }
            endTable()
        }

        // This essentially the same as above, except instead of using a for loop we call TableSetColumnIndex() manually.
        // Sometimes this makes more sense.
        helpMarker("Using TableNextRow() + calling TableSetColumnIndex() _before_ each cell, manually.")
        if (beginTable("##table2", 3)) {
            for (row in 0..3) {
                tableNextRow()
                tableSetColumnIndex(0)
                text("Row $row")
                tableSetColumnIndex(1)
                text("Some contents")
                tableSetColumnIndex(2)
                text("123.456")
            }
            endTable()
        }

        // Another subtle variant, we call TableNextCell() _before_ each cell. At the end of a row, TableNextCell() will create a new row.
        // Note that we don't call TableNextRow() here!
        // If we want to call TableNextRow(), then we don't need to call TableNextCell() for the first cell.
        helpMarker("Only using TableNextCell(), which tends to be convenient for tables where every cells contains the same type of contents.\nThis is also more similar to the old NextColumn() function of the Columns API, and provided to facilitate the Columns->Tables API transition.")
        if (beginTable("##table4", 3)) {
            for (item in 0..13) {
                tableNextCell()
                text("Item $item")
            }
            endTable()
        }
    }

    // Expose a few Borders related flags interactively
    var flags0 = Tf.BordersOuter or Tf.RowBg
    var displayWidth = false
    fun bordersBackground() = treeNode("Borders, background") {
        // Expose a few Borders related flags interactively
        checkboxFlags("ImGuiTableFlags_RowBg", ::flags0, Tf.RowBg.i)
        checkboxFlags("ImGuiTableFlags_Borders", ::flags0, Tf.Borders.i)
        sameLine(); helpMarker("ImGuiTableFlags_Borders\n = ImGuiTableFlags_BordersVInner\n | ImGuiTableFlags_BordersVOuter\n | ImGuiTableFlags_BordersHInner\n | ImGuiTableFlags_BordersHOuter")
        indent()

        checkboxFlags("ImGuiTableFlags_BordersH", ::flags0, Tf.BordersH.i)
        indent()
        checkboxFlags("ImGuiTableFlags_BordersHOuter", ::flags0, Tf.BordersHOuter.i)
        checkboxFlags("ImGuiTableFlags_BordersHInner", ::flags0, Tf.BordersHInner.i)
        unindent()

        checkboxFlags("ImGuiTableFlags_BordersV", ::flags0, Tf.BordersV.i)
        indent()
        checkboxFlags("ImGuiTableFlags_BordersVOuter", ::flags0, Tf.BordersVOuter.i)
        checkboxFlags("ImGuiTableFlags_BordersVInner", ::flags0, Tf.BordersVInner.i)
        unindent()

        checkboxFlags("ImGuiTableFlags_BordersOuter", ::flags0, Tf.BordersOuter.i)
        checkboxFlags("ImGuiTableFlags_BordersInner", ::flags0, Tf.BordersInner.i)
        unindent()
        checkbox("Debug Display width", ::displayWidth)

        table("##table1", 3, flags0) {
            for (row in 0..4) {
                tableNextRow()
                for (column in 0..2) {
                    tableSetColumnIndex(column)
                    if (displayWidth) {
                        val p = ImGui.cursorScreenPos
                        val drawList = ImGui.windowDrawList
                        val x1 = p.x
                        val x2 = ImGui.windowPos.x + ImGui.contentRegionMax.x
                        val x3 = drawList.clipRectMax.x
                        val y2 = p.y + ImGui.textLineHeight
                        drawList.addLine(Vec2(x1, y2), Vec2(x3, y2), COL32(255, 255, 0, 255)) // Hard clipping limit
                        drawList.addLine(Vec2(x1, y2), Vec2(x2, y2), COL32(255, 0, 0, 255))   // Normal limit
                        text("w=%.2f", x2 - x1)
                    } else
                        text("Hello $row,$column")
                }
            }
        }
    }

    // By default, if we don't enable ScrollX the sizing policy for each columns is "Stretch"
    // Each columns maintain a sizing weight, and they will occupy all available width.
    var flags1 = Tf.Resizable or Tf.BordersOuter or Tf.BordersV
    fun resizableStretch() = treeNode("Resizable, stretch") {
        // By default, if we don't enable ScrollX the sizing policy for each columns is "Stretch"
        // Each columns maintain a sizing weight, and they will occupy all available width.
        checkboxFlags("ImGuiTableFlags_Resizable", ::flags1, Tf.Resizable.i)
        checkboxFlags("ImGuiTableFlags_BordersV", ::flags1, Tf.BordersV.i)
        sameLine(); helpMarker("Using the _Resizable flag automatically enables the _BordersV flag as well.")

        table("##table1", 3, flags1) {
            for (row in 0..4) {
                tableNextRow()
                for (column in 0..2) {
                    tableSetColumnIndex(column)
                    text("Hello $row,$column")
                }
            }
        }
    }

    var flags2 = Tf.Resizable or Tf.SizingPolicyFixedX or Tf.BordersOuter or Tf.BordersV
    fun resizableFixed() = treeNode("Resizable, fixed") {
        // Here we use ImGuiTableFlags_SizingPolicyFixedX (even though _ScrollX is not set)
        // So columns will adopt the "Fixed" policy and will maintain a fixed weight regardless of the whole available width.
        // If there is not enough available width to fit all columns, they will however be resized down.
        // FIXME-TABLE: Providing a stretch-on-init would make sense especially for tables which don't have saved settings
        helpMarker("Using _Resizable + _SizingPolicyFixedX flags.\nFixed-width columns generally makes more sense if you want to use horizontal scrolling.")

        //ImGui::CheckboxFlags("ImGuiTableFlags_ScrollX", (unsigned int*)&flags, ImGuiTableFlags_ScrollX); // FIXME-TABLE: Explain or fix the effect of enable Scroll on outer_size
        table("##table1", 3, flags2) {
            for (row in 0..4) {
                tableNextRow()
                for (column in 0..2) {
                    tableSetColumnIndex(column)
                    text("Hello $row,$column")
                }
            }
        }
    }

    var flags3 = Tf.SizingPolicyFixedX or Tf.RowBg or Tf.Borders or Tf.Resizable or Tf.Reorderable or Tf.Hideable
    fun resizableMixed() = treeNode("Resizable, mixed") {
        helpMarker("Using columns flag to alter resizing policy on a per-column basis.")
        //ImGui::CheckboxFlags("ImGuiTableFlags_ScrollX", (unsigned int*)&flags, ImGuiTableFlags_ScrollX); // FIXME-TABLE: Explain or fix the effect of enable Scroll on outer_size

        if (beginTable("##table1", 3, flags3, Vec2(0f, ImGui.textLineHeightWithSpacing * 6))) {
            tableSetupColumn("AAA", Tcf.WidthFixed.i)// | ImGuiTableColumnFlags_NoResize);
            tableSetupColumn("BBB", Tcf.WidthFixed.i)
            tableSetupColumn("CCC", Tcf.WidthStretch.i)
            tableAutoHeaders()
            for (row in 0..4) {
                tableNextRow()
                for (column in 0..2) {
                    tableSetColumnIndex(column)
                    text("${if (column == 2) "Stretch" else "Fixed"} $row,$column")
                }
            }
            endTable()
        }
        table("##table2", 6, flags3, Vec2(0f, ImGui.textLineHeightWithSpacing * 6)) {
            tableSetupColumn("AAA", Tcf.WidthFixed.i)
            tableSetupColumn("BBB", Tcf.WidthFixed.i)
            tableSetupColumn("CCC", Tcf.WidthFixed or Tcf.DefaultHide)
            tableSetupColumn("DDD", Tcf.WidthStretch.i)
            tableSetupColumn("EEE", Tcf.WidthStretch.i)
            tableSetupColumn("FFF", Tcf.WidthStretch or Tcf.DefaultHide)
            tableAutoHeaders()
            for (row in 0..4) {
                tableNextRow()
                for (column in 0..5) {
                    tableSetColumnIndex(column)
                    text("${if (column >= 3) "Stretch" else "Fixed"} $row,$column")
                }
            }
        }
    }

    var flags4 = Tf.Resizable or Tf.Reorderable or Tf.Hideable or Tf.BordersOuter or Tf.BordersV
    fun reorderableHideableWithHeaders() = treeNode("Reorderable, hideable, with headers") {
        helpMarker("Click and drag column headers to reorder columns.\n\nYou can also right-click on a header to open a context menu.")
        checkboxFlags("ImGuiTableFlags_Resizable", ::flags4, Tf.Resizable.i)
        checkboxFlags("ImGuiTableFlags_Reorderable", ::flags4, Tf.Reorderable.i)
        checkboxFlags("ImGuiTableFlags_Hideable", ::flags4, Tf.Hideable.i)

        table("##table1", 3, flags4) {
            // Submit columns name with TableSetupColumn() and call TableAutoHeaders() to create a row with a header in each column.
            // (Later we will show how TableSetupColumn() has other uses, optional flags, sizing weight etc.)
            tableSetupColumn("One")
            tableSetupColumn("Two")
            tableSetupColumn("Three")
            tableAutoHeaders()
            for (row in 0..5) {
                tableNextRow()
                for (column in 0..2) {
                    tableSetColumnIndex(column)
                    text("Hello $row,$column")
                }
            }
        }

        table("##table2", 3, flags4 or Tf.SizingPolicyFixedX) {
            tableSetupColumn("One")
            tableSetupColumn("Two")
            tableSetupColumn("Three")
            tableAutoHeaders()
            for (row in 0..5) {
                tableNextRow()
                for (column in 0..2) {
                    tableSetColumnIndex(column)
                    text("Fixed $row,$column")
                }
            }
        }
    }

    var flags5 = Tf.ScrollY or Tf.ScrollFreezeTopRow or Tf.RowBg or Tf.BordersOuter or Tf.BordersV or Tf.Resizable or Tf.Reorderable or Tf.Hideable
    fun verticalScrollingWithClipping() = treeNode("Vertical scrolling, with clipping") {
        helpMarker("Here we activate ScrollY, which will create a child window container to allow hosting scrollable contents.\n\nWe also demonstrate using ImGuiListClipper to virtualize the submission of many items.")
        val size = Vec2(0f, ImGui.textLineHeightWithSpacing * 7)
        checkboxFlags("ImGuiTableFlags_ScrollY", ::flags5, Tf.ScrollY.i)
        checkboxFlags("ImGuiTableFlags_ScrollFreezeTopRow", ::flags5, Tf.ScrollFreezeTopRow.i)

        table("##table1", 3, flags5, size) {
            tableSetupColumn("One", Tcf.None.i)
            tableSetupColumn("Two", Tcf.None.i)
            tableSetupColumn("Three", Tcf.None.i)
            tableAutoHeaders()
            val clipper = ListClipper()
            clipper.begin(1000)
            while (clipper.step()) {
                for (row in clipper.display) {
                    tableNextRow()
                    for (column in 0..2) {
                        tableSetColumnIndex(column)
                        text("Hello $row,$column")
                    }
                }
            }
        }
    }

    var flags6 = Tf.ScrollX or Tf.ScrollY or Tf.ScrollFreezeTopRow or Tf.ScrollFreezeLeftColumn or Tf.RowBg or Tf.BordersOuter or Tf.BordersV or Tf.Resizable or Tf.Reorderable or Tf.Hideable
    fun horizontalScrolling() = treeNode("Horizontal scrolling") {
        helpMarker("When ScrollX is enabled, the default sizing policy becomes ImGuiTableFlags_SizingPolicyFixedX, as automatically stretching columns doesn't make much sense with horizontal scrolling.\n\nAlso note that as of the current version, you will almost always want to enable ScrollY along with ScrollX, because the container window won't automatically extend vertically to fix contents (this may be improved in future versions).")
        val size = Vec2(0f, ImGui.textLineHeightWithSpacing * 10)

        checkboxFlags("ImGuiTableFlags_ScrollY", ::flags6, Tf.ScrollY.i)
        checkboxFlags("ImGuiTableFlags_ScrollFreezeTopRow", ::flags6, Tf.ScrollFreezeTopRow.i)
        checkboxFlags("ImGuiTableFlags_ScrollFreezeLeftColumn", ::flags6, Tf.ScrollFreezeLeftColumn.i)

        table("##table1", 7, flags6, size) {
            tableSetupColumn("Line #", Tcf.NoHide.i) // Make the first column not hideable to match our use of ImGuiTableFlags_ScrollFreezeLeftColumn
            tableSetupColumn("One", Tcf.None.i)
            tableSetupColumn("Two", Tcf.None.i)
            tableSetupColumn("Three", Tcf.None.i)
            tableSetupColumn("Four", Tcf.None.i)
            tableSetupColumn("Five", Tcf.None.i)
            tableSetupColumn("Six", Tcf.None.i)
            tableAutoHeaders()
            for (row in 0..19) {
                tableNextRow()
                for (column in 0..6) {
                    // Both TableNextCell() and TableSetColumnIndex() return false when a column is not visible, which can be used for clipping.
                    if (!tableSetColumnIndex(column))
                        continue
                    if (column == 0)
                        text("Line $row")
                    else
                        text("Hello world $row,$column")
                }
            }
        }
    }

    val columnFlags = intArrayOf(Tcf.DefaultSort.i, Tcf.None.i, Tcf.DefaultHide.i)
    var flags7 = Tf.SizingPolicyFixedX or Tf.RowBg or Tf.BordersOuter or Tf.BordersV or Tf.Resizable or Tf.Reorderable or Tf.Hideable or Tf.Sortable
    fun columnsFlags() = treeNode("Columns flags") {
        // Create a first table just to show all the options/flags we want to make visible in our example!
        val columnCount = 3
        val columnNames = listOf("One", "Two", "Three")

        table("##flags", columnCount, Tf.None.i) {
            for (column in 0 until columnCount) {
                // Make the UI compact because there are so many fields
                tableNextCell()
                pushStyleVar(StyleVar.FramePadding, Vec2(style.framePadding.x, 2))
                pushStyleVar(StyleVar.ItemSpacing, Vec2(style.itemSpacing.x, 2))
                pushID(column)
                alignTextToFramePadding() // FIXME-TABLE: Workaround for wrong text baseline propagation
                text("Flags for '${columnNames[column]}'")
                checkboxFlags("_NoResize", columnFlags, column, Tcf.NoResize.i)
                checkboxFlags("_NoClipX", columnFlags, column, Tcf.NoClipX.i)
                checkboxFlags("_NoHide", columnFlags, column, Tcf.NoHide.i)
                checkboxFlags("_NoReorder", columnFlags, column, Tcf.NoReorder.i)
                checkboxFlags("_DefaultSort", columnFlags, column, Tcf.DefaultSort.i)
                checkboxFlags("_DefaultHide", columnFlags, column, Tcf.DefaultHide.i)
                checkboxFlags("_NoSort", columnFlags, column, Tcf.NoSort.i)
                checkboxFlags("_NoSortAscending", columnFlags, column, Tcf.NoSortAscending.i)
                checkboxFlags("_NoSortDescending", columnFlags, column, Tcf.NoSortDescending.i)
                checkboxFlags("_PreferSortAscending", columnFlags, column, Tcf.PreferSortAscending.i)
                checkboxFlags("_PreferSortDescending", columnFlags, column, Tcf.PreferSortDescending.i)
                checkboxFlags("_IndentEnable", columnFlags, column, Tcf.IndentEnable.i); sameLine(); helpMarker("Default for column 0")
                checkboxFlags("_IndentDisable", columnFlags, column, Tcf.IndentDisable.i); sameLine(); helpMarker("Default for column >0")
                popID()
                popStyleVar(2)
            }
        }

        // Create the real table we care about for the example!
        table("##table", columnCount, flags7) {
            for (column in 0 until columnCount)
                tableSetupColumn(columnNames[column], columnFlags[column])
            tableAutoHeaders()
            for (row in 0..7) {
                indent(2f) // Add some indentation to demonstrate usage of per-column IndentEnable/IndentDisable flags.
                tableNextRow()
                for (column in 0 until columnCount) {
                    tableSetColumnIndex(column)
                    text("${if (column == 0) "Indented" else "Hello"} ${tableGetColumnName(column)}")
                }
            }
            unindent(2f * 8f)
        }
    }

    fun recursive() = treeNode("Recursive") {
        helpMarker("This demonstrate embedding a table into another table cell.")

        table("recurse1", 2, Tf.Borders or Tf.BordersVFullHeight or Tf.Resizable or Tf.Reorderable) {
            tableSetupColumn("A0")
            tableSetupColumn("A1")
            tableAutoHeaders()

            tableNextRow(); text("A0 Cell 0")
            run {
                val rowsHeight = ImGui.textLineHeightWithSpacing * 2
                table("recurse2", 2, Tf.Borders or Tf.BordersVFullHeight or Tf.Resizable or Tf.Reorderable) {
                    tableSetupColumn("B0")
                    tableSetupColumn("B1")
                    tableAutoHeaders()

                    tableNextRow(TableRowFlag.None.i, rowsHeight)
                    text("B0 Cell 0")
                    tableNextCell()
                    text("B0 Cell 1")
                    tableNextRow(TableRowFlag.None.i, rowsHeight)
                    text("B1 Cell 0")
                    tableNextCell()
                    text("B1 Cell 1")
                }
            }
            tableNextCell(); text("A0 Cell 1")
            tableNextRow(); text("A1 Cell 0")
            tableNextCell(); text("A1 Cell 1")
        }
    }

    enum class ContentsType0 { ShortText, LongText, Button, StretchButton, InputText }

    var contentsType0 = ContentsType0.StretchButton.ordinal
    var flags8 = Tf.ScrollY or Tf.BordersOuter or Tf.RowBg
    val textBuf0 = ByteArray(32)
    fun sizingPoliciesCellContents() = treeNode("Sizing policies, cell contents") {
        helpMarker("This section allows you to interact and see the effect of StretchX vs FixedX sizing policies depending on whether Scroll is enabled and the contents of your columns.")

        setNextItemWidth(ImGui.fontSize * 12)
        combo("Contents", ::contentsType0, "Short Text\u0000Long Text\u0000Button\u0000Stretch Button\u0000InputText\u0000")

        checkboxFlags("ImGuiTableFlags_BordersHInner", ::flags8, Tf.BordersHInner.i)
        checkboxFlags("ImGuiTableFlags_BordersHOuter", ::flags8, Tf.BordersHOuter.i)
        checkboxFlags("ImGuiTableFlags_BordersVInner", ::flags8, Tf.BordersVInner.i)
        checkboxFlags("ImGuiTableFlags_BordersVOuter", ::flags8, Tf.BordersVOuter.i)
        checkboxFlags("ImGuiTableFlags_ScrollX", ::flags8, Tf.ScrollX.i)
        checkboxFlags("ImGuiTableFlags_ScrollY", ::flags8, Tf.ScrollY.i)
        if (checkboxFlags("ImGuiTableFlags_SizingPolicyStretchX", ::flags8, Tf.SizingPolicyStretchX.i))
            flags8 = flags8 wo (Tf.SizingPolicyMaskX_ xor Tf.SizingPolicyStretchX)  // Can't specify both sizing polices so we clear the other
        sameLine(); helpMarker("Default if _ScrollX if disabled.")
        if (checkboxFlags("ImGuiTableFlags_SizingPolicyFixedX", ::flags8, Tf.SizingPolicyFixedX.i))
            flags8 = flags8 wo (Tf.SizingPolicyMaskX_ xor Tf.SizingPolicyFixedX)    // Can't specify both sizing polices so we clear the other
        sameLine(); helpMarker("Default if _ScrollX if enabled.")
        checkboxFlags("ImGuiTableFlags_Resizable", ::flags8, Tf.Resizable.i)
        checkboxFlags("ImGuiTableFlags_NoClipX", ::flags8, Tf.NoClipX.i)

        table("##3ways", 3, flags8, Vec2(0, 100)) {
            for (row in 0..9) {
                tableNextRow()
                for (column in 0..2) {
                    tableSetColumnIndex(column)
                    val label = "Hello $row,$column"
                    when (ContentsType0.values()[contentsType0]) {
                        ContentsType0.ShortText -> textUnformatted(label)
                        ContentsType0.LongText -> text("Some longer text $row,$column\nOver two lines..")
                        ContentsType0.Button -> button(label)
                        ContentsType0.StretchButton -> button(label, Vec2(-Float.MIN_VALUE, 0f))
                        ContentsType0.InputText -> {
                            setNextItemWidth(-Float.MIN_VALUE)
                            inputText("##", textBuf0)
                        }
                    }
                }
            }
        }
    }

    var flags9 = Tf.Borders or Tf.RowBg
    var noWidgetFrame = false
    val textBuf1 = ByteArray(32)
    fun compactTable() = treeNode("Compact table") {
        // FIXME-TABLE: Vertical border not overridden the same way as horizontal one
        helpMarker("Setting style.CellPadding to (0,0).")

        checkboxFlags("ImGuiTableFlags_BordersOuter", ::flags9, Tf.BordersOuter.i)
        checkboxFlags("ImGuiTableFlags_BordersH", ::flags9, Tf.BordersH.i)
        checkboxFlags("ImGuiTableFlags_BordersV", ::flags9, Tf.BordersV.i)
        checkboxFlags("ImGuiTableFlags_RowBg", ::flags9, Tf.RowBg.i)
        checkboxFlags("ImGuiTableFlags_Resizable", ::flags9, Tf.Resizable.i)

        checkbox("no_widget_frame", ::noWidgetFrame)

        pushStyleVar(StyleVar.CellPadding, Vec2())
        table("##3ways", 3, flags9) {
            for (row in 0..9) {
                tableNextRow()
                for (column in 0..2) {
                    tableSetColumnIndex(column)
                    setNextItemWidth(-Float.MIN_VALUE)
                    pushID(row * 3 + column)
                    if (noWidgetFrame)
                        pushStyleColor(Col.FrameBg, 0)
                    inputText("##cell", textBuf1)
                    if (noWidgetFrame)
                        popStyleColor()
                    popID()
                }
            }
        }
        popStyleVar()
    }

    fun rowHeight() = treeNode("Row height") {
        helpMarker("You can pass a 'min_row_height' to TableNextRow().\n\nRows are padded with 'style.CellPadding.y' on top and bottom, so effectively the minimum row height will always be >= 'style.CellPadding.y * 2.0f'.\n\nWe cannot honor a _maximum_ row height as that would requires a unique clipping rectangle per row.")
        table("##2ways", 2, Tf.Borders.i) {
            var minRowHeight = ImGui.fontSize + style.cellPadding.y * 2f
            tableNextRow(TableRowFlag.None.i, minRowHeight)
            text("min_row_height = %.2f", minRowHeight)
            for (row in 0..9) {
                minRowHeight = (ImGui.fontSize * 0.3f * row).i.f
                tableNextRow(TableRowFlag.None.i, minRowHeight)
                text("min_row_height = %.2f", minRowHeight)
            }
        }
    }

    var flags10 = Tf.BordersV or Tf.BordersHOuter or Tf.Resizable or Tf.RowBg

    // Simple storage to output a dummy file-system.
    class MyTreeNode(val name: String, val type: String, val size: Int, val childIdx: Int, val childCount: Int) {
        fun displayNode() {
            tableNextRow()
            val isFolder = childCount > 0
            if (isFolder) {
                val open = treeNodeEx(name, TreeNodeFlag.SpanFullWidth.i)
                tableNextCell()
                textDisabled("--")
                tableNextCell()
                textUnformatted(type)
                if (open) {
                    for (childN in 0 until childCount)
                        nodes[childIdx + childN].displayNode()
                    treePop()
                }
            } else {
                treeNodeEx(name, TreeNodeFlag.Leaf or TreeNodeFlag.Bullet or TreeNodeFlag.NoTreePushOnOpen or TreeNodeFlag.SpanFullWidth)
                tableNextCell()
                text("$size")
                tableNextCell()
                textUnformatted(type)
            }
        }
    }

    val nodes = arrayOf(
            MyTreeNode("Root", "Folder", -1, 1, 3), // 0
            MyTreeNode("Music", "Folder", -1, 4, 2), // 1
            MyTreeNode("Textures", "Folder", -1, 6, 3), // 2
            MyTreeNode("desktop.ini", "System file", 1024, -1, -1), // 3
            MyTreeNode("File1_a.wav", "Audio file", 123000, -1, -1), // 4
            MyTreeNode("File1_b.wav", "Audio file", 456000, -1, -1), // 5
            MyTreeNode("Image001.png", "Image file", 203128, -1, -1), // 6
            MyTreeNode("Copy of Image001.png", "Image file", 203256, -1, -1), // 7
            MyTreeNode("Copy of Image001 (Final2).png", "Image file", 203512, -1, -1)) // 8

    fun treeView() = treeNode("Tree view") {

        //ImGui::CheckboxFlags("ImGuiTableFlags_Scroll", (unsigned int*)&flags, ImGuiTableFlags_Scroll);
        //ImGui::CheckboxFlags("ImGuiTableFlags_ScrollFreezeLeftColumn", (unsigned int*)&flags, ImGuiTableFlags_ScrollFreezeLeftColumn);

        table("##3ways", 3, flags10) {
            // The first column will use the default _WidthStretch when ScrollX is Off and _WidthFixed when ScrollX is On
            tableSetupColumn("Name", Tcf.NoHide.i)
            tableSetupColumn("Size", Tcf.WidthFixed.i, ImGui.fontSize * 6)
            tableSetupColumn("Type", Tcf.WidthFixed.i, ImGui.fontSize * 10)
            tableAutoHeaders()

            nodes[0].displayNode()
        }
    }


    val COLUMNS_COUNT = 3
    val columnSelected = BooleanArray(COLUMNS_COUNT)
    fun customHeaders() = treeNode("Custom headers") {

        table("##table1", COLUMNS_COUNT, Tf.Borders or Tf.Reorderable) {
            tableSetupColumn("Apricot")
            tableSetupColumn("Banana")
            tableSetupColumn("Cherry")

            // Dummy entire-column selection storage
            // FIXME: It would be nice to actually demonstrate full-featured selection using those checkbox.


            // Instead of calling TableAutoHeaders() we'll submit custom headers ourselves
            tableNextRow(TableRowFlag.Headers.i)
            for (column in 0 until COLUMNS_COUNT) {
                tableSetColumnIndex(column)
                val columnName = tableGetColumnName(column)!! // Retrieve name passed to TableSetupColumn()
                pushID(column)
                pushStyleVar(StyleVar.FramePadding, Vec2())
                checkbox("##checkall", columnSelected, column)
                popStyleVar()
                sameLine(0f, style.itemInnerSpacing.x)
                tableHeader(columnName)
                popID()
            }

            for (row in 0..4) {
                tableNextRow()
                for (column in 0 until COLUMNS_COUNT) {
                    val buf = "Cell $row,$column"
                    tableSetColumnIndex(column)
                    selectable(buf, columnSelected, column)
                }
            }
        }
    }


    val templateItemsNames = arrayOf(
            "Banana", "Apple", "Cherry", "Watermelon", "Grapefruit", "Strawberry", "Mango",
            "Kiwi", "Orange", "Pineapple", "Blueberry", "Plum", "Coconut", "Pear", "Apricot")

    // We are passing our own identifier to TableSetupColumn() to facilitate identifying columns in the sorting code.
    // This identifier will be passed down into ImGuiTableSortSpec::ColumnUserID.
    // But it is possible to omit the user id parameter of TableSetupColumn() and just use the column index instead! (ImGuiTableSortSpec::ColumnIndex)
    // If you don't use sorting, you will generally never care about giving column an ID!
    enum class MyItemColumnID { ID, Name, Action, Quantity, Description }

    data class MyItem(val id: Int, val name: String, var quantity: Int)

    // We have a problem which is affecting _only this demo_ and should not affect your code:
    // As we don't rely on std:: or other third-party library to compile dear imgui, we only have reliable access to qsort(),
    // however qsort doesn't allow passing user data to comparing function.
    // As a workaround, we are storing the sort specs in a static/global for the comparing function to access.
    // In your own use case you would probably pass the sort specs to your sorting/comparing functions directly and not use a global.
    var sCurrentSortSpecs: TableSortSpecs? = null
    val compareWithSortSpecs = Comparator<MyItem> { a, b ->

        var result = a.id - b.id
        // Compare function to be used by qsort()
        for (sortSpec in sCurrentSortSpecs!!.specs!!) {
            // Here we identify columns using the ColumnUserID value that we ourselves passed to TableSetupColumn()
            // We could also choose to identify columns based on their index (sort_spec->ColumnIndex), which is simpler!
            val delta = when (MyItemColumnID.values()[sortSpec.columnUserID]) {
                MyItemColumnID.ID -> a.id - b.id
                MyItemColumnID.Name -> a.name.compareTo(b.name)
                MyItemColumnID.Quantity -> a.quantity - b.quantity
                MyItemColumnID.Description -> a.name.compareTo(b.name)
                else -> error("")
            }
            if (delta > 0) {
                result = if (sortSpec.sortDirection == SortDirection.Ascending) +1 else -1
                break
            }
            if (delta < 0) {
                result = if (sortSpec.sortDirection == SortDirection.Ascending) -1 else +1
                break
            }
        }

        // qsort() is instable so always return a way to differenciate items.
        // Your own compare function may want to avoid fallback on implicit sort specs e.g. a Name compare if it wasn't already part of the sort specs.
        result
    }
//    const ImGuiTableSortSpecs* MyItem::s_current_sort_specs = NULL;

    // Create item list
    var items0 = Array(50) { n ->
        val templateN = n % templateItemsNames.size
        MyItem(id = n,
                name = templateItemsNames[templateN],
                quantity = (n * n - n) % 20) // Assign default quantities
    }
    var flags11 = Tf.Resizable or Tf.Reorderable or Tf.Hideable or Tf.MultiSortable or Tf.RowBg or Tf.BordersOuter or Tf.BordersV or Tf.ScrollY or Tf.ScrollFreezeTopRow

    fun sorting() = treeNode("Sorting") {

        helpMarker("Use Shift+Click to sort on multiple columns")

        table("##table", 4, flags11, Vec2(0f, 250f), 0f) {
            // Declare columns
            // We use the "user_id" parameter of TableSetupColumn() to specify a user id that will be stored in the sort specifications.
            // This is so our sort function can identify a column given our own identifier. We could also identify them based on their index!
            // Demonstrate using a mixture of flags among available sort-related flags:
            // - ImGuiTableColumnFlags_DefaultSort
            // - ImGuiTableColumnFlags_NoSort / ImGuiTableColumnFlags_NoSortAscending / ImGuiTableColumnFlags_NoSortDescending
            // - ImGuiTableColumnFlags_PreferSortAscending / ImGuiTableColumnFlags_PreferSortDescending
            tableSetupColumn("ID", Tcf.DefaultSort or Tcf.WidthFixed, -1f, MyItemColumnID.ID.ordinal)
            tableSetupColumn("Name", Tcf.WidthFixed.i, -1f, MyItemColumnID.Name.ordinal)
            tableSetupColumn("Action", Tcf.NoSort or Tcf.WidthFixed, -1f, MyItemColumnID.Action.ordinal)
            tableSetupColumn("Quantity", Tcf.PreferSortDescending or Tcf.WidthStretch, -1f, MyItemColumnID.Quantity.ordinal)

            // Sort our data if sort specs have been changed!
            tableGetSortSpecs()?.let { sortsSpecs ->
                if (sortsSpecs.specsChanged && items0.size > 1) {
                    sCurrentSortSpecs = sortsSpecs // Store in variable accessible by the sort function.
                    items0.sortWith(compareWithSortSpecs)
                    sCurrentSortSpecs = null
                }
            }

            // Display data
            tableAutoHeaders()
            val clipper = ListClipper()
            clipper.begin(items0.size)
            while (clipper.step())
                for (rowN in clipper.display) {
                    val item = items0[rowN]
                    pushID(item.id)
                    tableNextRow()
                    tableSetColumnIndex(0)
                    text("%04d".format(item.id))
                    tableSetColumnIndex(1)
                    textUnformatted(item.name)
                    tableSetColumnIndex(2)
                    smallButton("None")
                    tableSetColumnIndex(3)
                    text("%d", item.quantity)
                    popID()
                }
        }
    }

    var flags12 = Tf.Resizable or Tf.Reorderable or Tf.Hideable or Tf.MultiSortable or Tf.RowBg or Tf.Borders or
            Tf.ScrollX or Tf.ScrollY or Tf.ScrollFreezeTopRow or Tf.ScrollFreezeLeftColumn or Tf.SizingPolicyFixedX

    enum class ContentsType1 {
        Text, Button, SmallButton, Selectable;

        companion object {
            val names = values().map { it.name }
        }
    }

    var contentsType1 = ContentsType1.Button.ordinal
    var itemsCount = templateItemsNames.size
    val outerSizeValue = Vec2(0f, 250f)
    var rowMinHeight = 0f // Auto
    var innerWidthWithScroll = 0f // Auto-extend
    var outerSizeEnabled = true
    var lockFirstColumnVisibility = false
    var showHeaders = true
    var showWrappedText = false
    fun advanced() = treeNode("Advanced") {
        advanced_Options()
        advanced_Table()
    }

    //static ImGuiTextFilter filter;
    //ImGui::SetNextItemOpen(true, ImGuiCond_Once); // FIXME-TABLE: Enabling this results in initial clipped first pass on table which affects sizing
    fun advanced_Options() {
        if (treeNodeEx("Options")) {
            // Make the UI compact because there are so many fields
            pushStyleVar(StyleVar.FramePadding, Vec2(style.framePadding.x, 1))
            pushStyleVar(StyleVar.ItemSpacing, Vec2(style.itemSpacing.x, 2))
            pushItemWidth(200)

            bulletText("Features:")
            dsl.indent {
                checkboxFlags("ImGuiTableFlags_Resizable", ::flags12, Tf.Resizable.i)
                checkboxFlags("ImGuiTableFlags_Reorderable", ::flags12, Tf.Reorderable.i)
                checkboxFlags("ImGuiTableFlags_Hideable", ::flags12, Tf.Hideable.i)
                checkboxFlags("ImGuiTableFlags_Sortable", ::flags12, Tf.Sortable.i)
                checkboxFlags("ImGuiTableFlags_MultiSortable", ::flags12, Tf.MultiSortable.i)
                checkboxFlags("ImGuiTableFlags_NoSavedSettings", ::flags12, Tf.NoSavedSettings.i)
            }

            bulletText("Decoration:")
            indent {
                checkboxFlags("ImGuiTableFlags_RowBg", ::flags12, Tf.RowBg.i)
                checkboxFlags("ImGuiTableFlags_BordersV", ::flags12, Tf.BordersV.i)
                checkboxFlags("ImGuiTableFlags_BordersVOuter", ::flags12, Tf.BordersVOuter.i)
                checkboxFlags("ImGuiTableFlags_BordersVInner", ::flags12, Tf.BordersVInner.i)
                checkboxFlags("ImGuiTableFlags_BordersH", ::flags12, Tf.BordersH.i)
                checkboxFlags("ImGuiTableFlags_BordersHOuter", ::flags12, Tf.BordersHOuter.i)
                checkboxFlags("ImGuiTableFlags_BordersHInner", ::flags12, Tf.BordersHInner.i)
                checkboxFlags("ImGuiTableFlags_BordersVFullHeight", ::flags12, Tf.BordersVFullHeight.i)
            }

            bulletText("Padding, Sizing:")
            indent {
                checkboxFlags("ImGuiTableFlags_NoClipX", ::flags12, Tf.NoClipX.i)
                if (checkboxFlags("ImGuiTableFlags_SizingPolicyStretchX", ::flags12, Tf.SizingPolicyStretchX.i))
                    flags12 = flags12 wo (Tf.SizingPolicyMaskX_ xor Tf.SizingPolicyStretchX)  // Can't specify both sizing polices so we clear the other
                sameLine(); helpMarker("[Default if ScrollX is off]\nFit all columns within available width (or specified inner_width). Fixed and Stretch columns allowed.")
                if (checkboxFlags("ImGuiTableFlags_SizingPolicyFixedX", ::flags12, Tf.SizingPolicyFixedX.i))
                    flags12 = flags12 wo (Tf.SizingPolicyMaskX_ xor Tf.SizingPolicyFixedX)    // Can't specify both sizing polices so we clear the other
                sameLine(); helpMarker("[Default if ScrollX is on]\nEnlarge as needed: enable scrollbar if ScrollX is enabled, otherwise extend parent window's contents rectangle. Only Fixed columns allowed. Stretched columns will calculate their width assuming no scrolling.")
                checkboxFlags("ImGuiTableFlags_NoHeadersWidth", ::flags12, Tf.NoHeadersWidth.i)
                checkboxFlags("ImGuiTableFlags_NoHostExtendY", ::flags12, Tf.NoHostExtendY.i)
            }

            bulletText("Scrolling:")
            indent {
                checkboxFlags("ImGuiTableFlags_ScrollX", ::flags12, Tf.ScrollX.i)
                checkboxFlags("ImGuiTableFlags_ScrollY", ::flags12, Tf.ScrollY.i)

                // For the purpose of our "advanced" demo, we expose the 3 freezing variants on both axises instead of only exposing the most common flag.
                //ImGui::CheckboxFlags("ImGuiTableFlags_ScrollFreezeTopRow", (unsigned int*)&flags, ImGuiTableFlags_ScrollFreezeTopRow);
                //ImGui::CheckboxFlags("ImGuiTableFlags_ScrollFreezeLeftColumn", (unsigned int*)&flags, ImGuiTableFlags_ScrollFreezeLeftColumn);
                val freezeRowCount = intArrayOf((flags12 and Tf.ScrollFreezeRowsMask_) shr Tf.ScrollFreezeRowsShift_.i)
                val freezeColCount = intArrayOf((flags12 and Tf.ScrollFreezeColumnsMask_) shr Tf.ScrollFreezeColumnsShift_.i)
                setNextItemWidth(ImGui.frameHeight)
                if (dragInt("ImGuiTableFlags_ScrollFreezeTopRow/2Rows/3Rows", freezeRowCount, 0, 0.2f, 0, 3))
                    if (freezeRowCount[0] in 0..3)
                        flags12 = (flags12 wo Tf.ScrollFreezeRowsMask_) or (freezeRowCount[0] shl Tf.ScrollFreezeRowsShift_.i)
                setNextItemWidth(ImGui.frameHeight)
                if (dragInt("ImGuiTableFlags_ScrollFreezeLeftColumn/2Columns/3Columns", freezeColCount, 0, 0.2f, 0, 3))
                    if (freezeColCount[0] in 0..3)
                        flags12 = (flags12 wo Tf.ScrollFreezeColumnsMask_) or (freezeColCount[0] shl Tf.ScrollFreezeColumnsShift_.i)

            }

            bulletText("Other:")
            indent {
                dragVec2("##OuterSize", outerSizeValue)
                sameLine(0f, style.itemInnerSpacing.x)
                checkbox("outer_size", ::outerSizeEnabled)
                sameLine()
                helpMarker("If scrolling is disabled (ScrollX and ScrollY not set), the table is output directly in the parent window. OuterSize.y then becomes the minimum size for the table, which will extend vertically if there are more rows (unless NoHostExtendV is set).")

                // From a user point of view we will tend to use 'inner_width' differently depending on whether our table is embedding scrolling.
                // To facilitate experimentation we expose two values and will select the right one depending on active flags.
                dragFloat("inner_width (when ScrollX active)", ::innerWidthWithScroll, 1f, 0f, Float.MAX_VALUE)
                dragFloat("row_min_height", ::rowMinHeight, 1f, 0f, Float.MAX_VALUE)
                sameLine(); helpMarker("Specify height of the Selectable item.")
                dragInt("items_count", ::itemsCount, 0.1f, 0, 5000)
                combo("contents_type (first column)", ::contentsType1, ContentsType1.names)
                //filter.Draw("filter");
                checkbox("show_headers", ::showHeaders)
                checkbox("show_wrapped_text", ::showWrappedText)
                checkbox("lock_first_column_visibility", ::lockFirstColumnVisibility)
            }

            popItemWidth()
            popStyleVar(2)
            spacing()
            treePop()
        }
    }

    var items1: Array<MyItem>? = null
    val selection = ArrayList<Int>()
    var itemsNeedSort = false
    var showDebugDetails = false
    fun advanced_Table() {

        // Recreate/reset item list if we changed the number of items
        if (items1!!.size != itemsCount)
            items1 = Array(itemsCount) { n ->
                val templateN = n % templateItemsNames.size
                MyItem(id = n,
                        name = templateItemsNames[templateN],
                        quantity = if (templateN == 3) 10 else if (templateN == 4) 20 else 0) // Assign default quantities
            }

        val parentDrawList = ImGui.windowDrawList
        val parentDrawListDrawCmdCount = parentDrawList.cmdBuffer.size
        val tableScrollCur = Vec2()
        val tableScrollMax = Vec2() // For debug display
        var tableDrawList: DrawList? = null  // "

        val innerWidthToUse = if (flags12 has Tf.ScrollX) innerWidthWithScroll else 0f
        table("##table", 6, flags12, if (outerSizeEnabled) outerSizeValue else Vec2(), innerWidthToUse) {
            // Declare columns
            // We use the "user_id" parameter of TableSetupColumn() to specify a user id that will be stored in the sort specifications.
            // This is so our sort function can identify a column given our own identifier. We could also identify them based on their index!
            tableSetupColumn("ID", Tcf.DefaultSort or Tcf.WidthFixed or if (lockFirstColumnVisibility) Tcf.NoHide else Tcf.None, -1f, MyItemColumnID.ID.ordinal)
            tableSetupColumn("Name", Tcf.WidthFixed.i, -1f, MyItemColumnID.Name.ordinal)
            tableSetupColumn("Action", Tcf.NoSort or Tcf.WidthFixed, -1f, MyItemColumnID.Action.ordinal)
            tableSetupColumn("Quantity Long Label", Tcf.PreferSortDescending or Tcf.WidthStretch, 1f, MyItemColumnID.Quantity.ordinal)// , ImGuiTableColumnFlags_None | ImGuiTableColumnFlags_WidthAlwaysAutoResize);
            tableSetupColumn("Description", Tcf.WidthStretch.i, 1f, MyItemColumnID.Description.ordinal)// , ImGuiTableColumnFlags_WidthAlwaysAutoResize);
            tableSetupColumn("Hidden", Tcf.DefaultHide or Tcf.NoSort)

            // Sort our data if sort specs have been changed!
            val sortsSpecs = tableGetSortSpecs()
            if (sortsSpecs?.specsChanged == true)
                itemsNeedSort = true
            if (sortsSpecs != null && itemsNeedSort && items1!!.size > 1) {
                sCurrentSortSpecs = sortsSpecs // Store in variable accessible by the sort function.
                items1!!.sortWith(compareWithSortSpecs)
                sCurrentSortSpecs = null
            }
            itemsNeedSort = false

            // Take note of whether we are currently sorting based on the Quantity field,
            // we will use this to trigger sorting when we know the data of this column has been modified.
            val sortsSpecsUsingQuantity = tableGetColumnIsSorted(3)

            // Show headers
            if (showHeaders)
                tableAutoHeaders()

            // Show data
            // FIXME-TABLE FIXME-NAV: How we can get decent up/down even though we have the buttons here?
            pushButtonRepeat(true)

            val clipper = ListClipper()
            clipper.begin(items1!!.size)
            while (clipper.step()) {
                for (rowN in clipper.display) {
                    val item = items1!![rowN]
                    //if (!filter.PassFilter(item->Name))
                    //    continue;

                    val itemIsSelected = item.id in selection
                    pushID(item.id)
                    tableNextRow(TableRowFlag.None.i, rowMinHeight)

                    // For the demo purpose we can select among different type of items submitted in the first column
                    val label = "%04d".format(item.id)
                    when (ContentsType1.values()[contentsType1]) {
                        ContentsType1.Text -> textUnformatted(label)
                        ContentsType1.Button -> button(label)
                        ContentsType1.SmallButton -> smallButton(label)
                        ContentsType1.Selectable ->
                            if (selectable(label, itemIsSelected, SelectableFlag.SpanAllColumns or SelectableFlag.AllowItemOverlap, Vec2(0f, rowMinHeight)))
                                if (io.keyCtrl)
                                    if (itemIsSelected)
                                        selection -= item.id
                                    else
                                        selection += item.id
                                else {
                                    selection.clear()
                                    selection += item.id
                                }

                        // Here we demonstrate marking our data set as needing to be sorted again if we modified a quantity,
                        // and we are currently sorting on the column showing the Quantity.
                        // To avoid triggering a sort while holding the button, we only trigger it when the button has been released.
                        // You will probably need a more advanced system in your code if you want to automatically sort when a specific entry changes.
                    }

                    tableNextCell()
                    textUnformatted(item.name)

                    // Here we demonstrate marking our data set as needing to be sorted again if we modified a quantity,
                    // and we are currently sorting on the column showing the Quantity.
                    // To avoid triggering a sort while holding the button, we only trigger it when the button has been released.
                    // You will probably need a more advanced system in your code if you want to automatically sort when a specific entry changes.
                    if (tableNextCell()) {
                        if (smallButton("Chop")) item.quantity++
                        if (sortsSpecsUsingQuantity && ImGui.isItemDeactivated)
                            itemsNeedSort = true
                        sameLine()
                        if (smallButton("Eat")) item.quantity--
                        if (sortsSpecsUsingQuantity && ImGui.isItemDeactivated)
                            itemsNeedSort = true
                    }

                    tableNextCell()
                    text("${item.quantity}")

                    tableNextCell()
                    if (showWrappedText)
                        textWrapped("Lorem ipsum dolor sit amet")
                    else
                        text("Lorem ipsum dolor sit amet")

                    tableNextCell()
                    text("1234")

                    popID()
                }
            }
            popButtonRepeat()

            tableScrollCur.put(ImGui.scrollX, ImGui.scrollY)
            tableScrollMax.put(ImGui.scrollMaxX, ImGui.scrollMaxY)
            tableDrawList = ImGui.windowDrawList
        }

        checkbox("Debug details", ::showDebugDetails)
        if (showDebugDetails)
            tableDrawList?.let {
                sameLine(0f, 0f)
                val tableDrawListDrawCmdCount = it.cmdBuffer.size
                if (it === parentDrawList)
                    text(": DrawCmd: +${tableDrawListDrawCmdCount - parentDrawListDrawCmdCount} (in same window)")
                else
                    text(": DrawCmd: +${tableDrawListDrawCmdCount - 1} (in child window), Scroll: (%.f/%.f) (%.f/%.f)",
                            tableScrollCur.x, tableScrollMax.x, tableScrollCur.y, tableScrollMax.y)
            }
    }
}