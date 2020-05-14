package imgui.demo

import glm_.f
import glm_.i
import glm_.vec2.Vec2
import imgui.*
import imgui.ImGui.alignTextToFramePadding
import imgui.ImGui.beginTable
import imgui.ImGui.button
import imgui.ImGui.checkbox
import imgui.ImGui.checkboxFlags
import imgui.ImGui.collapsingHeader
import imgui.ImGui.combo
import imgui.ImGui.endTable
import imgui.ImGui.indent
import imgui.ImGui.inputText
import imgui.ImGui.popID
import imgui.ImGui.popStyleColor
import imgui.ImGui.popStyleVar
import imgui.ImGui.pushID
import imgui.ImGui.pushStyleColor
import imgui.ImGui.pushStyleVar
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.setNextItemOpen
import imgui.ImGui.setNextItemWidth
import imgui.ImGui.style
import imgui.ImGui.tableAutoHeaders
import imgui.ImGui.tableGetColumnName
import imgui.ImGui.tableNextCell
import imgui.ImGui.tableNextRow
import imgui.ImGui.tableSetColumnIndex
import imgui.ImGui.tableSetupColumn
import imgui.ImGui.text
import imgui.ImGui.textUnformatted
import imgui.ImGui.unindent
import imgui.api.demoDebugInformations.Companion.helpMarker
import imgui.classes.ListClipper
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

/*
            if (openAction != -1)
                ImGui::SetNextItemOpen(open_action != 0)
            if (ImGui::TreeNode("Tree view")) {
                static ImGuiTableFlags flags = ImGuiTableFlags_BordersV | ImGuiTableFlags_BordersHOuter | ImGuiTableFlags_Resizable | ImGuiTableFlags_RowBg
                //ImGui::CheckboxFlags("ImGuiTableFlags_Scroll", (unsigned int*)&flags, ImGuiTableFlags_Scroll);
                //ImGui::CheckboxFlags("ImGuiTableFlags_ScrollFreezeLeftColumn", (unsigned int*)&flags, ImGuiTableFlags_ScrollFreezeLeftColumn);

                if (ImGui::BeginTable("##3ways", 3, flags)) {
                    // The first column will use the default _WidthStretch when ScrollX is Off and _WidthFixed when ScrollX is On
                    ImGui::TableSetupColumn("Name", ImGuiTableColumnFlags_NoHide)
                    ImGui::TableSetupColumn("Size", ImGuiTableColumnFlags_WidthFixed, ImGui::GetFontSize() * 6)
                    ImGui::TableSetupColumn("Type", ImGuiTableColumnFlags_WidthFixed, ImGui::GetFontSize() * 10)
                    ImGui::TableAutoHeaders()

                    // Simple storage to output a dummy file-system.
                    struct MyTreeNode
                            {
                                const char * Name
                                        const char * Type
                                        int Size
                                        int ChildIdx
                                        int ChildCount
                                        static void DisplayNode(const MyTreeNode * node, const MyTreeNode * all_nodes)
                                {
                                    ImGui::TableNextRow()
                                    const bool is_folder = (node->ChildCount > 0)
                                    if (is_folder) {
                                        bool open = ImGui ::TreeNodeEx(node->Name, ImGuiTreeNodeFlags_SpanFullWidth)
                                        ImGui::TableNextCell()
                                        ImGui::TextDisabled("--")
                                        ImGui::TableNextCell()
                                        ImGui::TextUnformatted(node->Type)
                                        if (open) {
                                            for (int child_n = 0; child_n < node->ChildCount; child_n++)
                                            DisplayNode(& all_nodes [node->ChildIdx+child_n], all_nodes)
                                            ImGui::TreePop()
                                        }
                                    } else {
                                        ImGui::TreeNodeEx(node->Name, ImGuiTreeNodeFlags_Leaf | ImGuiTreeNodeFlags_Bullet | ImGuiTreeNodeFlags_NoTreePushOnOpen | ImGuiTreeNodeFlags_SpanFullWidth)
                                        ImGui::TableNextCell()
                                        ImGui::Text("%d", node->Size)
                                        ImGui::TableNextCell()
                                        ImGui::TextUnformatted(node->Type)
                                    }
                                }
                            }
                    static const MyTreeNode nodes [] =
                            {
                                { "Root", "Folder", -1, 1, 3 }, // 0
                                { "Music", "Folder", -1, 4, 2 }, // 1
                                { "Textures", "Folder", -1, 6, 3 }, // 2
                                { "desktop.ini", "System file", 1024, -1, -1 }, // 3
                                { "File1_a.wav", "Audio file", 123000, -1, -1 }, // 4
                                { "File1_b.wav", "Audio file", 456000, -1, -1 }, // 5
                                { "Image001.png", "Image file", 203128, -1, -1 }, // 6
                                { "Copy of Image001.png", "Image file", 203256, -1, -1 }, // 7
                                { "Copy of Image001 (Final2).png", "Image file", 203512, -1, -1 }, // 8
                            }

                    MyTreeNode::DisplayNode(& nodes [0], nodes)

                    ImGui::EndTable()
                }
                ImGui::TreePop()
            }

            // Demonstrate using TableHeader() calls instead of TableAutoHeaders()
            // FIXME-TABLE: Currently this doesn't get us feature-parity with TableAutoHeaders(), e.g. missing context menu.  Tables API needs some work!
            if (openAction != -1)
                ImGui::SetNextItemOpen(open_action != 0)
            if (ImGui::TreeNode("Custom headers")) {
                const int COLUMNS_COUNT = 3
                if (ImGui::BeginTable("##table1", COLUMNS_COUNT, ImGuiTableFlags_Borders | ImGuiTableFlags_Reorderable))
                {
                    ImGui::TableSetupColumn("Apricot")
                    ImGui::TableSetupColumn("Banana")
                    ImGui::TableSetupColumn("Cherry")

                    // Dummy entire-column selection storage
                    // FIXME: It would be nice to actually demonstrate full-featured selection using those checkbox.
                    static bool column_selected[3] = {}

                    // Instead of calling TableAutoHeaders() we'll submit custom headers ourselves
                    ImGui::TableNextRow(ImGuiTableRowFlags_Headers)
                    for (int column = 0; column < COLUMNS_COUNT; column++)
                    {
                        ImGui::TableSetColumnIndex(column)
                        const char * column_name = ImGui ::TableGetColumnName(column) // Retrieve name passed to TableSetupColumn()
                        ImGui::PushID(column)
                        ImGui::PushStyleVar(ImGuiStyleVar_FramePadding, ImVec2(0, 0))
                        ImGui::Checkbox("##checkall", & column_selected [column])
                        ImGui::PopStyleVar()
                        ImGui::SameLine(0.0f, ImGui::GetStyle().ItemInnerSpacing.x)
                        ImGui::TableHeader(column_name)
                        ImGui::PopID()
                    }

                    for (int row = 0; row < 5; row++)
                    {
                        ImGui::TableNextRow()
                        for (int column = 0; column < 3; column++)
                        {
                            char buf [32]
                            sprintf(buf, "Cell %d,%d", row, column)
                            ImGui::TableSetColumnIndex(column)
                            ImGui::Selectable(buf, column_selected[column])
                        }
                    }
                    ImGui::EndTable()
                }
                ImGui::TreePop()
            }

            static const char * template_items_names[] =
                    {
                        "Banana", "Apple", "Cherry", "Watermelon", "Grapefruit", "Strawberry", "Mango",
                        "Kiwi", "Orange", "Pineapple", "Blueberry", "Plum", "Coconut", "Pear", "Apricot"
                    }

            // This is a simplified version of the "Advanced" example, where we mostly focus on the code necessary to handle sorting.
            // Note that the "Advanced" example also showcase manually triggering a sort (e.g. if item quantities have been modified)
            if (openAction != -1)
                ImGui::SetNextItemOpen(open_action != 0)
            if (ImGui::TreeNode("Sorting")) {
                HelpMarker("Use Shift+Click to sort on multiple columns")

                // Create item list
                static ImVector < MyItem > items
                        if (items.Size == 0) {
                            items.resize(50, MyItem())
                            for (int n = 0; n < items.Size; n++)
                            {
                                const int template_n = n % IM_ARRAYSIZE(template_items_names)
                                MyItem& item = items[n]
                                item.ID = n
                                item.Name = template_items_names[template_n]
                                item.Quantity = (n * n - n) % 20 // Assign default quantities
                            }
                        }

                static ImGuiTableFlags flags =
                        ImGuiTableFlags_Resizable | ImGuiTableFlags_Reorderable | ImGuiTableFlags_Hideable | ImGuiTableFlags_MultiSortable
                | ImGuiTableFlags_RowBg | ImGuiTableFlags_BordersOuter | ImGuiTableFlags_BordersV
                | ImGuiTableFlags_ScrollY | ImGuiTableFlags_ScrollFreezeTopRow
                if (ImGui::BeginTable("##table", 4, flags, ImVec2(0, 250), 0.0f)) {
                    // Declare columns
                    // We use the "user_id" parameter of TableSetupColumn() to specify a user id that will be stored in the sort specifications.
                    // This is so our sort function can identify a column given our own identifier. We could also identify them based on their index!
                    // Demonstrate using a mixture of flags among available sort-related flags:
                    // - ImGuiTableColumnFlags_DefaultSort
                    // - ImGuiTableColumnFlags_NoSort / ImGuiTableColumnFlags_NoSortAscending / ImGuiTableColumnFlags_NoSortDescending
                    // - ImGuiTableColumnFlags_PreferSortAscending / ImGuiTableColumnFlags_PreferSortDescending
                    ImGui::TableSetupColumn("ID", ImGuiTableColumnFlags_DefaultSort          | ImGuiTableColumnFlags_WidthFixed, -1.0f, MyItemColumnID_ID)
                    ImGui::TableSetupColumn("Name", ImGuiTableColumnFlags_WidthFixed, -1.0f, MyItemColumnID_Name)
                    ImGui::TableSetupColumn("Action", ImGuiTableColumnFlags_NoSort               | ImGuiTableColumnFlags_WidthFixed, -1.0f, MyItemColumnID_Action)
                    ImGui::TableSetupColumn("Quantity", ImGuiTableColumnFlags_PreferSortDescending | ImGuiTableColumnFlags_WidthStretch, -1.0f, MyItemColumnID_Quantity)

                    // Sort our data if sort specs have been changed!
                    if (const ImGuiTableSortSpecs * sorts_specs = ImGui ::TableGetSortSpecs())
                        if (sorts_specs->SpecsChanged && items.Size > 1)
                    {
                        MyItem::s_current_sort_specs = sorts_specs // Store in variable accessible by the sort function.
                        qsort(& items [0], (size_t)items.Size, sizeof(items[0]), MyItem::CompareWithSortSpecs)
                        MyItem::s_current_sort_specs = NULL
                    }

                    // Display data
                    ImGui::TableAutoHeaders()
                    ImGuiListClipper clipper
                            clipper.Begin(items.Size)
                    while (clipper.Step())
                        for (int row_n = clipper.DisplayStart; row_n < clipper.DisplayEnd; row_n++)
                    {
                        MyItem * item = & items [row_n]
                        ImGui::PushID(item->ID)
                        ImGui::TableNextRow()
                        ImGui::TableSetColumnIndex(0)
                        ImGui::Text("%04d", item->ID)
                        ImGui::TableSetColumnIndex(1)
                        ImGui::TextUnformatted(item->Name)
                        ImGui::TableSetColumnIndex(2)
                        ImGui::SmallButton("None")
                        ImGui::TableSetColumnIndex(3)
                        ImGui::Text("%d", item->Quantity)
                        ImGui::PopID()
                    }
                    ImGui::EndTable()
                }
                ImGui::TreePop()
            }

            if (openAction != -1)
                ImGui::SetNextItemOpen(open_action != 0)
            if (ImGui::TreeNode("Advanced")) {
                static ImGuiTableFlags flags =
                        ImGuiTableFlags_Resizable | ImGuiTableFlags_Reorderable | ImGuiTableFlags_Hideable | ImGuiTableFlags_MultiSortable
                | ImGuiTableFlags_RowBg | ImGuiTableFlags_Borders
                | ImGuiTableFlags_ScrollX | ImGuiTableFlags_ScrollY
                | ImGuiTableFlags_ScrollFreezeTopRow | ImGuiTableFlags_ScrollFreezeLeftColumn
                | ImGuiTableFlags_SizingPolicyFixedX

                enum ContentsType { CT_Text, CT_Button, CT_SmallButton, CT_Selectable }
                static int contents_type = CT_Button
                const char * contents_type_names [] = { "Text", "Button", "SmallButton", "Selectable" }

                static int items_count = IM_ARRAYSIZE(template_items_names)
                static ImVec2 outer_size_value = ImVec2(0, 250)
                static float row_min_height = 0.0f // Auto
                static float inner_width_without_scroll = 0.0f // Fill
                static float inner_width_with_scroll = 0.0f // Auto-extend
                static bool outer_size_enabled = true
                static bool lock_first_column_visibility = false
                static bool show_headers = true
                static bool show_wrapped_text = false
                //static ImGuiTextFilter filter;
                //ImGui::SetNextItemOpen(true, ImGuiCond_Once); // FIXME-TABLE: Enabling this results in initial clipped first pass on table which affects sizing
                if (ImGui::TreeNodeEx("Options")) {
                    // Make the UI compact because there are so many fields
                    ImGuiStyle& style = ImGui::GetStyle()
                    ImGui::PushStyleVar(ImGuiStyleVar_FramePadding, ImVec2(style.FramePadding.x, 1))
                    ImGui::PushStyleVar(ImGuiStyleVar_ItemSpacing, ImVec2(style.ItemSpacing.x, 2))
                    ImGui::PushItemWidth(200)

                    ImGui::BulletText("Features:")
                    ImGui::Indent()
                    ImGui::CheckboxFlags("ImGuiTableFlags_Resizable", (unsigned int *)& flags, ImGuiTableFlags_Resizable)
                    ImGui::CheckboxFlags("ImGuiTableFlags_Reorderable", (unsigned int *)& flags, ImGuiTableFlags_Reorderable)
                    ImGui::CheckboxFlags("ImGuiTableFlags_Hideable", (unsigned int *)& flags, ImGuiTableFlags_Hideable)
                    ImGui::CheckboxFlags("ImGuiTableFlags_Sortable", (unsigned int *)& flags, ImGuiTableFlags_Sortable)
                    ImGui::CheckboxFlags("ImGuiTableFlags_MultiSortable", (unsigned int *)& flags, ImGuiTableFlags_MultiSortable)
                    ImGui::CheckboxFlags("ImGuiTableFlags_NoSavedSettings", (unsigned int *)& flags, ImGuiTableFlags_NoSavedSettings)
                    ImGui::Unindent()

                    ImGui::BulletText("Decoration:")
                    ImGui::Indent()
                    ImGui::CheckboxFlags("ImGuiTableFlags_RowBg", (unsigned int *)& flags, ImGuiTableFlags_RowBg)
                    ImGui::CheckboxFlags("ImGuiTableFlags_BordersV", (unsigned int *)& flags, ImGuiTableFlags_BordersV)
                    ImGui::CheckboxFlags("ImGuiTableFlags_BordersVOuter", (unsigned int *)& flags, ImGuiTableFlags_BordersVOuter)
                    ImGui::CheckboxFlags("ImGuiTableFlags_BordersVInner", (unsigned int *)& flags, ImGuiTableFlags_BordersVInner)
                    ImGui::CheckboxFlags("ImGuiTableFlags_BordersH", (unsigned int *)& flags, ImGuiTableFlags_BordersH)
                    ImGui::CheckboxFlags("ImGuiTableFlags_BordersHOuter", (unsigned int *)& flags, ImGuiTableFlags_BordersHOuter)
                    ImGui::CheckboxFlags("ImGuiTableFlags_BordersHInner", (unsigned int *)& flags, ImGuiTableFlags_BordersHInner)
                    ImGui::CheckboxFlags("ImGuiTableFlags_BordersVFullHeight", (unsigned int *)& flags, ImGuiTableFlags_BordersVFullHeight)
                    ImGui::Unindent()

                    ImGui::BulletText("Padding, Sizing:")
                    ImGui::Indent()
                    ImGui::CheckboxFlags("ImGuiTableFlags_NoClipX", (unsigned int *)& flags, ImGuiTableFlags_NoClipX)
                    if (ImGui::CheckboxFlags("ImGuiTableFlags_SizingPolicyStretchX", (unsigned int *)& flags, ImGuiTableFlags_SizingPolicyStretchX))
                    flags & = ~(ImGuiTableFlags_SizingPolicyMaskX_ ^ ImGuiTableFlags_SizingPolicyStretchX)  // Can't specify both sizing polices so we clear the other
                    ImGui::SameLine(); HelpMarker("[Default if ScrollX is off]\nFit all columns within available width (or specified inner_width). Fixed and Stretch columns allowed.")
                    if (ImGui::CheckboxFlags("ImGuiTableFlags_SizingPolicyFixedX", (unsigned int *)& flags, ImGuiTableFlags_SizingPolicyFixedX))
                    flags & = ~(ImGuiTableFlags_SizingPolicyMaskX_ ^ ImGuiTableFlags_SizingPolicyFixedX)    // Can't specify both sizing polices so we clear the other
                    ImGui::SameLine(); HelpMarker("[Default if ScrollX is on]\nEnlarge as needed: enable scrollbar if ScrollX is enabled, otherwise extend parent window's contents rectangle. Only Fixed columns allowed. Stretched columns will calculate their width assuming no scrolling.")
                    ImGui::CheckboxFlags("ImGuiTableFlags_NoHeadersWidth", (unsigned int *)& flags, ImGuiTableFlags_NoHeadersWidth)
                    ImGui::CheckboxFlags("ImGuiTableFlags_NoHostExtendY", (unsigned int *)& flags, ImGuiTableFlags_NoHostExtendY)
                    ImGui::Unindent()

                    ImGui::BulletText("Scrolling:")
                    ImGui::Indent()
                    ImGui::CheckboxFlags("ImGuiTableFlags_ScrollX", (unsigned int *)& flags, ImGuiTableFlags_ScrollX)
                    ImGui::CheckboxFlags("ImGuiTableFlags_ScrollY", (unsigned int *)& flags, ImGuiTableFlags_ScrollY)

                    // For the purpose of our "advanced" demo, we expose the 3 freezing variants on both axises instead of only exposing the most common flag.
                    //ImGui::CheckboxFlags("ImGuiTableFlags_ScrollFreezeTopRow", (unsigned int*)&flags, ImGuiTableFlags_ScrollFreezeTopRow);
                    //ImGui::CheckboxFlags("ImGuiTableFlags_ScrollFreezeLeftColumn", (unsigned int*)&flags, ImGuiTableFlags_ScrollFreezeLeftColumn);
                    int freeze_row_count =(flags & ImGuiTableFlags_ScrollFreezeRowsMask_) >> ImGuiTableFlags_ScrollFreezeRowsShift_
                    int freeze_col_count =(flags & ImGuiTableFlags_ScrollFreezeColumnsMask_) >> ImGuiTableFlags_ScrollFreezeColumnsShift_
                    ImGui::SetNextItemWidth(ImGui::GetFrameHeight())
                    if (ImGui::DragInt("ImGuiTableFlags_ScrollFreezeTopRow/2Rows/3Rows", & freeze_row_count, 0.2f, 0, 3))
                    if (freeze_row_count >= 0 && freeze_row_count <= 3)
                        flags = (flags & ~ImGuiTableFlags_ScrollFreezeRowsMask_) | (freeze_row_count << ImGuiTableFlags_ScrollFreezeRowsShift_)
                    ImGui::SetNextItemWidth(ImGui::GetFrameHeight())
                    if (ImGui::DragInt("ImGuiTableFlags_ScrollFreezeLeftColumn/2Columns/3Columns", & freeze_col_count, 0.2f, 0, 3))
                    if (freeze_col_count >= 0 && freeze_col_count <= 3)
                        flags = (flags & ~ImGuiTableFlags_ScrollFreezeColumnsMask_) | (freeze_col_count << ImGuiTableFlags_ScrollFreezeColumnsShift_)

                    ImGui::Unindent()

                    ImGui::BulletText("Other:")
                    ImGui::Indent()
                    ImGui::DragFloat2("##OuterSize", & outer_size_value . x)
                    ImGui::SameLine(0.0f, ImGui::GetStyle().ItemInnerSpacing.x)
                    ImGui::Checkbox("outer_size", & outer_size_enabled)
                    ImGui::SameLine()
                    HelpMarker("If scrolling is disabled (ScrollX and ScrollY not set), the table is output directly in the parent window. OuterSize.y then becomes the minimum size for the table, which will extend vertically if there are more rows (unless NoHostExtendV is set).")

                    // From a user point of view we will tend to use 'inner_width' differently depending on whether our table is embedding scrolling.
                    // To facilitate experimentation we expose two values and will select the right one depending on active flags.
                    if (flags & ImGuiTableFlags_ScrollX)
                    ImGui::DragFloat("inner_width (when ScrollX active)", & inner_width_with_scroll, 1.0f, 0.0f, FLT_MAX)
                    else
                    ImGui::DragFloat("inner_width (when ScrollX inactive)", & inner_width_without_scroll, 1.0f, 0.0f, FLT_MAX)
                    ImGui::DragFloat("row_min_height", & row_min_height, 1.0f, 0.0f, FLT_MAX)
                    ImGui::SameLine(); HelpMarker("Specify height of the Selectable item.")
                    ImGui::DragInt("items_count", & items_count, 0.1f, 0, 5000)
                    ImGui::Combo("contents_type (first column)", & contents_type, contents_type_names, IM_ARRAYSIZE(contents_type_names))
                    //filter.Draw("filter");
                    ImGui::Checkbox("show_headers", & show_headers)
                    ImGui::Checkbox("show_wrapped_text", & show_wrapped_text)
                    ImGui::Checkbox("lock_first_column_visibility", & lock_first_column_visibility)
                    ImGui::Unindent()

                    ImGui::PopItemWidth()
                    ImGui::PopStyleVar(2)
                    ImGui::Spacing()
                    ImGui::TreePop()
                }

                // Recreate/reset item list if we changed the number of items
                static ImVector < MyItem > items
                        static ImVector < int > selection
                        static bool items_need_sort = false
                if (items.Size != items_count) {
                    items.resize(items_count, MyItem())
                    for (int n = 0; n < items_count; n++)
                    {
                        const int template_n = n % IM_ARRAYSIZE(template_items_names)
                        MyItem& item = items[n]
                        item.ID = n
                        item.Name = template_items_names[template_n]
                        item.Quantity = (template_n == 3) ? 10 : (template_n == 4) ? 20 : 0 // Assign default quantities
                    }
                }

                const ImDrawList * parent_draw_list = ImGui ::GetWindowDrawList()
                const int parent_draw_list_draw_cmd_count = parent_draw_list->CmdBuffer.Size
                ImVec2 table_scroll_cur, table_scroll_max // For debug display
                const ImDrawList * table_draw_list = NULL  // "

                        const float inner_width_to_use = (flags & ImGuiTableFlags_ScrollX) ? inner_width_with_scroll : inner_width_without_scroll
                if (ImGui::BeginTable("##table", 6, flags, outer_size_enabled ? outer_size_value : ImVec2 (0, 0), inner_width_to_use))
                {
                    // Declare columns
                    // We use the "user_id" parameter of TableSetupColumn() to specify a user id that will be stored in the sort specifications.
                    // This is so our sort function can identify a column given our own identifier. We could also identify them based on their index!
                    ImGui::TableSetupColumn("ID", ImGuiTableColumnFlags_DefaultSort | ImGuiTableColumnFlags_WidthFixed |(lock_first_column_visibility ? ImGuiTableColumnFlags_NoHide : 0), -1.0f, MyItemColumnID_ID)
                    ImGui::TableSetupColumn("Name", ImGuiTableColumnFlags_WidthFixed, -1.0f, MyItemColumnID_Name)
                    ImGui::TableSetupColumn("Action", ImGuiTableColumnFlags_NoSort | ImGuiTableColumnFlags_WidthFixed, -1.0f, MyItemColumnID_Action)
                    ImGui::TableSetupColumn("Quantity Long Label", ImGuiTableColumnFlags_PreferSortDescending | ImGuiTableColumnFlags_WidthStretch, 1.0f, MyItemColumnID_Quantity)// , ImGuiTableColumnFlags_None | ImGuiTableColumnFlags_WidthAlwaysAutoResize);
                    ImGui::TableSetupColumn("Description", ImGuiTableColumnFlags_WidthStretch, 1.0f, MyItemColumnID_Description)// , ImGuiTableColumnFlags_WidthAlwaysAutoResize);
                    ImGui::TableSetupColumn("Hidden", ImGuiTableColumnFlags_DefaultHide | ImGuiTableColumnFlags_NoSort)

                    // Sort our data if sort specs have been changed!
                    const ImGuiTableSortSpecs * sorts_specs = ImGui ::TableGetSortSpecs()
                    if (sorts_specs && sorts_specs->SpecsChanged)
                    items_need_sort = true
                    if (sorts_specs && items_need_sort && items.Size > 1) {
                        MyItem::s_current_sort_specs = sorts_specs // Store in variable accessible by the sort function.
                        qsort(& items [0], (size_t)items.Size, sizeof(items[0]), MyItem::CompareWithSortSpecs)
                        MyItem::s_current_sort_specs = NULL
                    }
                    items_need_sort = false

                    // Take note of whether we are currently sorting based on the Quantity field,
                    // we will use this to trigger sorting when we know the data of this column has been modified.
                    const bool sorts_specs_using_quantity = ImGui::TableGetColumnIsSorted(3)

                    // Show headers
                    if (show_headers)
                        ImGui::TableAutoHeaders()

                    // Show data
                    // FIXME-TABLE FIXME-NAV: How we can get decent up/down even though we have the buttons here?
                    ImGui::PushButtonRepeat(true)
                    #if 1
                    ImGuiListClipper clipper
                            clipper.Begin(items.Size)
                    while (clipper.Step()) {
                        for (int row_n = clipper.DisplayStart; row_n < clipper.DisplayEnd; row_n++)
                        #else
                        {
                            for (int row_n = 0; row_n < items_count; n++)
                            #endif
                            {
                                MyItem * item = & items [row_n]
                                //if (!filter.PassFilter(item->Name))
                                //    continue;

                                const bool item_is_selected = selection.contains(item->ID)
                                ImGui::PushID(item->ID)
                                ImGui::TableNextRow(ImGuiTableRowFlags_None, row_min_height)

                                // For the demo purpose we can select among different type of items submitted in the first column
                                char label [32]
                                sprintf(label, "%04d", item->ID)
                                if (contents_type == CT_Text)
                                    ImGui::TextUnformatted(label)
                                else if (contents_type == CT_Button)
                                    ImGui::Button(label)
                                else if (contents_type == CT_SmallButton)
                                    ImGui::SmallButton(label)
                                else if (contents_type == CT_Selectable) {
                                    if (ImGui::Selectable(label, item_is_selected, ImGuiSelectableFlags_SpanAllColumns | ImGuiSelectableFlags_AllowItemOverlap, ImVec2(0, row_min_height)))
                                    {
                                        if (ImGui::GetIO().KeyCtrl) {
                                            if (item_is_selected)
                                                selection.find_erase_unsorted(item->ID)
                                            else
                                            selection.push_back(item->ID)
                                        } else {
                                            selection.clear()
                                            selection.push_back(item->ID)
                                        }
                                    }
                                }

                                ImGui::TableNextCell()
                                ImGui::TextUnformatted(item->Name)

                                // Here we demonstrate marking our data set as needing to be sorted again if we modified a quantity,
                                // and we are currently sorting on the column showing the Quantity.
                                // To avoid triggering a sort while holding the button, we only trigger it when the button has been released.
                                // You will probably need a more advanced system in your code if you want to automatically sort when a specific entry changes.
                                if (ImGui::TableNextCell()) {
                                    if (ImGui::SmallButton("Chop")) { item -> Quantity += 1; }
                                    if (sorts_specs_using_quantity && ImGui::IsItemDeactivated()) {
                                        items_need_sort = true; }
                                    ImGui::SameLine()
                                    if (ImGui::SmallButton("Eat")) { item -> Quantity -= 1; }
                                    if (sorts_specs_using_quantity && ImGui::IsItemDeactivated()) {
                                        items_need_sort = true; }
                                }

                                ImGui::TableNextCell()
                                ImGui::Text("%d", item->Quantity)

                                ImGui::TableNextCell()
                                if (show_wrapped_text)
                                    ImGui::TextWrapped("Lorem ipsum dolor sit amet")
                                else
                                    ImGui::Text("Lorem ipsum dolor sit amet")

                                ImGui::TableNextCell()
                                ImGui::Text("1234")

                                ImGui::PopID()
                            }
                        }
                        ImGui::PopButtonRepeat()

                        table_scroll_cur = ImVec2(ImGui::GetScrollX(), ImGui::GetScrollY())
                        table_scroll_max = ImVec2(ImGui::GetScrollMaxX(), ImGui::GetScrollMaxY())
                        table_draw_list = ImGui::GetWindowDrawList()
                        ImGui::EndTable()
                    }
                    static bool show_debug_details = false
                    ImGui::Checkbox("Debug details", & show_debug_details)
                    if (show_debug_details && table_draw_list) {
                        ImGui::SameLine(0.0f, 0.0f)
                        const int table_draw_list_draw_cmd_count = table_draw_list->CmdBuffer.Size
                        if (table_draw_list == parent_draw_list)
                            ImGui::Text(": DrawCmd: +%d (in same window)", table_draw_list_draw_cmd_count - parent_draw_list_draw_cmd_count)
                        else
                            ImGui::Text(": DrawCmd: +%d (in child window), Scroll: (%.f/%.f) (%.f/%.f)",
                                    table_draw_list_draw_cmd_count - 1, table_scroll_cur.x, table_scroll_max.x, table_scroll_cur.y, table_scroll_max.y)
                    }
                    ImGui::TreePop()
                } */

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
                checkboxFlags("_IndentEnable", columnFlags, column, Tcf.IndentEnable.i)
                checkboxFlags("_IndentDisable", columnFlags, column, Tcf.IndentDisable.i)
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

    enum class ContentsType { ShortText, LongText, Button, StretchButton, InputText }

    var contentsType = ContentsType.StretchButton.ordinal
    var flags8 = Tf.ScrollY or Tf.BordersOuter or Tf.RowBg
    val textBuf0 = ByteArray(32)
    fun sizingPoliciesCellContents() = treeNode("Sizing policies, cell contents") {
        helpMarker("This section allows you to interact and see the effect of StretchX vs FixedX sizing policies depending on whether Scroll is enabled and the contents of your columns.")

        setNextItemWidth(ImGui.fontSize * 12)
        combo("Contents", ::contentsType, "Short Text\u0000Long Text\u0000Button\u0000Stretch Button\u0000InputText\u0000")

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
                    when (ContentsType.values()[contentsType]) {
                        ContentsType.ShortText -> textUnformatted(label)
                        ContentsType.LongText -> text("Some longer text $row,$column\nOver two lines..")
                        ContentsType.Button -> button(label)
                        ContentsType.StretchButton -> button(label, Vec2(-Float.MIN_VALUE, 0f))
                        ContentsType.InputText -> {
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
}