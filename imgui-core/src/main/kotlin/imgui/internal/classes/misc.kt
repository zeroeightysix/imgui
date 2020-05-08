package imgui.internal.classes

import glm_.*
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.*
import imgui.api.g
import imgui.classes.Context
import imgui.classes.DrawList
import imgui.classes.TableSortSpecs
import imgui.classes.TableSortSpecsColumn
import imgui.font.Font
import imgui.internal.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/** Helper to build a ImDrawData instance */
class DrawDataBuilder {
    /** Global layers for: regular, tooltip */
    val layers = Array(2) { ArrayList<DrawList>() }

    fun clear() = layers.forEach { it.clear() }

    fun flattenIntoSingleLayer() {
        val size = layers.map { it.size }.count()
        layers[0].ensureCapacity(size)
        for (layerN in 1 until layers.size) {
            val layer = layers[layerN]
            if (layer.isEmpty()) continue
            layers[0].addAll(layer)
            layer.clear()
        }
    }
}

// ImDrawList: Helper function to calculate a circle's segment count given its radius and a "maximum error" value.
const val DRAWLIST_CIRCLE_AUTO_SEGMENT_MIN = 12
const val DRAWLIST_CIRCLE_AUTO_SEGMENT_MAX = 512
fun DRAWLIST_CIRCLE_AUTO_SEGMENT_CALC(_RAD: Float, _MAXERROR: Float) = clamp(((glm.πf * 2f) / acos((_RAD - _MAXERROR) / _RAD)).i, DRAWLIST_CIRCLE_AUTO_SEGMENT_MIN, DRAWLIST_CIRCLE_AUTO_SEGMENT_MAX)

// ImDrawList: You may set this to higher values (e.g. 2 or 3) to increase tessellation of fast rounded corners path.
var DRAWLIST_ARCFAST_TESSELLATION_MULTIPLIER = 1

/** Data shared between all ImDrawList instances
 *  You may want to create your own instance of this if you want to use ImDrawList completely without ImGui. In that case, watch out for future changes to this structure.
 *  Data shared among multiple draw lists (typically owned by parent ImGui context, but you may create one yourself) */
class DrawListSharedData {
    /** UV of white pixel in the atlas  */
    var texUvWhitePixel = Vec2()

    /** Current/default font (optional, for simplified AddText overload) */
    var font: Font? = null

    /** Current/default font size (optional, for simplified AddText overload) */
    var fontSize = 0f

    var curveTessellationTol = 0f

    /** Number of circle segments to use per pixel of radius for AddCircle() etc */
    var circleSegmentMaxError = 0f

    /** Value for pushClipRectFullscreen() */
    var clipRectFullscreen = Vec4(-8192f, -8192f, 8192f, 8192f)

    /** Initial flags at the beginning of the frame (it is possible to alter flags on a per-drawlist basis afterwards) */
    var initialFlags = DrawListFlag.None.i

    // [Internal] Lookup tables

    // Lookup tables
    val arcFastVtx = Array(12 * DRAWLIST_ARCFAST_TESSELLATION_MULTIPLIER) {
        // FIXME: Bake rounded corners fill/borders in atlas
        val a = it * 2 * glm.PIf / 12
        Vec2(cos(a), sin(a))
    }

    /** Precomputed segment count for given radius (array index + 1) before we calculate it dynamically (to avoid calculation overhead) */
    val circleSegmentCounts = IntArray(64) // This will be set by SetCircleSegmentMaxError()

    fun setCircleSegmentMaxError_(maxError: Float) {
        if (circleSegmentMaxError == maxError)
            return
        circleSegmentMaxError = maxError
        for (i in circleSegmentCounts.indices) {
            val radius = i + 1f
            val segmentCount = DRAWLIST_CIRCLE_AUTO_SEGMENT_CALC(radius, circleSegmentMaxError)
            circleSegmentCounts[i] = segmentCount min 255
        }
    }
}

/** Stacked color modifier, backup of modified data so we can restore it    */
class ColorMod(val col: Col, value: Vec4) {
    val backupValue = Vec4(value)
}

/** Storage data for a single column */
class ColumnData {
    /** Column start offset, normalized 0f (far left) -> 1f (far right) */
    var offsetNorm = 0f
    var offsetNormBeforeResize = 0f

    /** Not exposed */
    var flags: ColumnsFlags = 0
    var clipRect = Rect()
}

/** Storage data for a columns set */
class Columns {
    var id: ID = 0
    var flags: ColumnsFlags = ColumnsFlag.None.i
    var isFirstFrame = false
    var isBeingResized = false
    var current = 0
    var count = 1

    /** Offsets from HostWorkRect.Min.x */
    var offMinX = 0f

    /** Offsets from HostWorkRect.Min.x */
    var offMaxX = 0f

    /** Backup of CursorPos at the time of BeginColumns() */
    var hostCursorPosY = 0f

    /** Backup of CursorMaxPos at the time of BeginColumns() */
    var hostCursorMaxPosX = 0f

    /** Backup of ClipRect at the time of BeginColumns() */
    var hostClipRect = Rect()

    /** Backup of WorkRect at the time of BeginColumns() */
    var hostWorkRect = Rect()
    var lineMinY = 0f
    var lineMaxY = 0f
    val columns = ArrayList<ColumnData>()
    val splitter = DrawListSplitter()

    fun destroy() = splitter.clearFreeMemory(destroy = true)

    fun clear() {
        id = 0
        flags = ColumnsFlag.None.i
        isFirstFrame = false
        isBeingResized = false
        current = 0
        count = 1
        offMaxX = 0f
        offMinX = 0f
        hostCursorPosY = 0f
        hostCursorMaxPosX = 0f
        lineMaxY = 0f
        lineMinY = 0f
        columns.clear()
    }

    /** ~GetColumnOffsetFromNorm    */
    infix fun getOffsetFrom(offsetNorm: Float): Float = offsetNorm * (offMaxX - offMinX)

    /** ~GetColumnNormFromOffset    */
    fun getNormFrom(offset: Float): Float = offset / (offMaxX - offMinX)
}

/** Type information associated to one ImGuiDataType. Retrieve with DataTypeGetInfo(). */
//class DataTypeInfo {
//    /** Size in byte */
//    var size = 0
//    /** Default printf format for the type */
//    lateinit var printFmt: String
//    /** Default scanf format for the type */
//    lateinit var scanFmt: String
//}

/* Stacked storage data for BeginGroup()/EndGroup() */
class GroupData {
    var backupCursorPos = Vec2()
    var backupCursorMaxPos = Vec2()
    var backupIndent = 0f
    var backupGroupOffset = 0f
    var backupCurrLineSize = Vec2()
    var backupCurrLineTextBaseOffset = 0f
    var backupActiveIdIsAlive = 0
    var backupActiveIdPreviousFrameIsAlive = false
    var emitItem = false
}

/** Backup and restore just enough data to be able to use isItemHovered() on item A after another B in the same window
 *  has overwritten the data.
 *  ¬ItemHoveredDataBackup, we optimize by using a function accepting a lambda */
fun itemHoveredDataBackup(block: () -> Unit) {
    // backup
    var window = g.currentWindow!!
    val lastItemId = window.dc.lastItemId
    val lastItemStatusFlags = window.dc.lastItemStatusFlags
    val lastItemRect = Rect(window.dc.lastItemRect)
    val lastItemDisplayRect = Rect(window.dc.lastItemDisplayRect)

    block()

    // restore
    window = g.currentWindow!!
    window.dc.lastItemId = lastItemId
    window.dc.lastItemRect put lastItemRect
    window.dc.lastItemStatusFlags = lastItemStatusFlags
    window.dc.lastItemDisplayRect = lastItemDisplayRect
}

/** Simple column measurement, currently used for MenuItem() only.. This is very short-sighted/throw-away code and NOT a generic helper. */
class MenuColumns {

    var spacing = 0f
    var width = 0f
    var nextWidth = 0f
    val pos = FloatArray(3)
    var nextWidths = FloatArray(3)

    fun update(count: Int, spacing: Float, clear: Boolean) {
        assert(count == pos.size)
        nextWidth = 0f
        width = 0f
        this.spacing = spacing
        if (clear)
            nextWidths.fill(0f)
        for (i in pos.indices) {
            if (i > 0 && nextWidths[i] > 0f)
                width += spacing
            pos[i] = floor(width)
            width += nextWidths[i]
            nextWidths[i] = 0f
        }
    }

    fun declColumns(w0: Float, w1: Float, w2: Float): Float {
        nextWidth = 0f
        nextWidths[0] = nextWidths[0] max w0
        nextWidths[1] = nextWidths[1] max w1
        nextWidths[2] = nextWidths[2] max w2
        for (i in pos.indices)
            nextWidth += nextWidths[i] + (if (i > 0 && nextWidths[i] > 0f) spacing else 0f)
        return width max nextWidth.i.f // JVM only TODO why?
    }


    fun calcExtraSpace(availW: Float) = glm.max(0f, availW - width)
}

/** Result of a gamepad/keyboard directional navigation move query result */
class NavMoveResult {
    /** Best candidate window   */
    var window: Window? = null

    /** Best candidate ID  */
    var id: ID = 0

    /** Best candidate focus scope ID */
    var focusScopeId: ID = 0

    /** Best candidate box distance to current NavId    */
    var distBox = Float.MAX_VALUE

    /** Best candidate center distance to current NavId */
    var distCenter = Float.MAX_VALUE

    var distAxial = Float.MAX_VALUE

    /** Best candidate bounding box in window relative space    */
    var rectRel = Rect()

    fun clear() {
        id = 0
        window = null
        distBox = Float.MAX_VALUE
        distCenter = Float.MAX_VALUE
        distAxial = Float.MAX_VALUE
        rectRel = Rect()
    }
}

/** Storage for SetNextWindow** functions    */
class NextWindowData {
    var flags = NextWindowDataFlag.None.i
    var posCond = Cond.None
    var sizeCond = Cond.None
    var collapsedCond = Cond.None
    val posVal = Vec2()
    val posPivotVal = Vec2()
    val sizeVal = Vec2()
    val contentSizeVal = Vec2()
    val scrollVal = Vec2()
    var collapsedVal = false

    /** Valid if 'SetNextWindowSizeConstraint' is true  */
    val sizeConstraintRect = Rect()
    var sizeCallback: SizeCallback? = null
    var sizeCallbackUserData: Any? = null

    /** Override background alpha */
    var bgAlphaVal = Float.MAX_VALUE

    /** *Always on* This is not exposed publicly, so we don't clear it. */
    var menuBarOffsetMinVal = Vec2()

    fun clearFlags() {
        flags = NextWindowDataFlag.None.i
    }
}

class NextItemData {
    var flags: NextItemDataFlags = 0

    /** Set by SetNextItemWidth() */
    var width = 0f

    /** Set by SetNextItemMultiSelectData() (!= 0 signify value has been set, so it's an alternate version of HasSelectionData, we don't use Flags for this because they are cleared too early. This is mostly used for debugging) */
    var focusScopeId: ID = 0

    var openCond = Cond.None

    /** Set by SetNextItemOpen() function. */
    var openVal = false

    /** Also cleared manually by ItemAdd()! */
    fun clearFlags() {
        flags = NextItemDataFlag.None.i
    }
}

/* Storage for current popup stack  */
class PopupData(
        /** Set on OpenPopup()  */
        var popupId: ID = 0,
        /** Resolved on BeginPopup() - may stay unresolved if user never calls OpenPopup()  */
        var window: Window? = null,
        /** Set on OpenPopup() copy of NavWindow at the time of opening the popup  */
        var sourceWindow: Window? = null,
        /** Set on OpenPopup()  */
        var openFrameCount: Int = -1,
        /** Set on OpenPopup(), we need this to differentiate multiple menu sets from each others
         *  (e.g. inside menu bar vs loose menu items)    */
        var openParentId: ID = 0,
        /** Set on OpenPopup(), preferred popup position (typically == OpenMousePos when using mouse)   */
        var openPopupPos: Vec2 = Vec2(),
        /** Set on OpenPopup(), copy of mouse position at the time of opening popup */
        var openMousePos: Vec2 = Vec2()
)

/** Read: Called when entering into a new ini entry e.g. "[Window][Name]" */
typealias ReadOpenFn = (ctx: Context, handler: SettingsHandler, name: String) -> Any

/** Read: Called for every line of text within an ini entry */
typealias ReadLineFn = (ctx: Context, handler: SettingsHandler, entry: Any, line: String) -> Unit

/** Write: Output every entries into 'out_buf' */
typealias WriteAllFn = (ctx: Context, handler: SettingsHandler, outBuf: StringBuilder) -> Unit

/** Storage for one type registered in the .ini file */
class SettingsHandler {
    /** Short description stored in .ini file. Disallowed characters: '[' ']' */
    var typeName = ""

    /** == ImHashStr(TypeName) */
    var typeHash: ID = 0

    lateinit var readOpenFn: ReadOpenFn
    lateinit var readLineFn: ReadLineFn
    lateinit var writeAllFn: WriteAllFn
    var userData: Any? = null
}

/** Stacked style modifier, backup of modified data so we can restore it. Data type inferred from the variable. */
class StyleMod(val idx: StyleVar) {
    var ints = IntArray(2)
    val floats = FloatArray(2)
}

/** Storage for one active tab item (sizeof() 26~32 bytes) */
class TabItem {
    var id: ID = 0
    var flags = TabItemFlag.None.i
    var lastFrameVisible = -1

    /** This allows us to infer an ordered list of the last activated tabs with little maintenance */
    var lastFrameSelected = -1

    /** When Window==NULL, offset to name within parent ImGuiTabBar::TabsNames */
    var nameOffset = -1

    /** Position relative to beginning of tab */
    var offset = 0f

    /** Width currently displayed */
    var width = 0f

    /** Width of actual contents, stored during BeginTabItem() call */
    var contentWidth = 0f
}


/** Special sentinel code */
val COL32_DISABLE = COL32(0, 0, 0, 1)

/** sizeof(ImU64) * 8. This is solely because we frequently encode columns set in a ImU64. */
val TABLE_MAX_COLUMNS = 64

/** Storage for a table */
class Table {

    var id: ID = 0
    var flags = TableFlag.None.i
    val rawData = BitSet()

    /** Point within RawData[] */
    var columns = Array<TableColumn?>(0) { null }

    /** Point within RawData[]. Store display order of columns (when not reordered, the values are 0...Count-1) */
    var displayOrderToIndex = ByteArray(0)

    /** Column Index -> IsActive map (Active == not hidden by user/api) in a format adequate for iterating column without touching cold data */
    var activeMaskByIndex = 0L

    /** Column DisplayOrder -> IsActive map */
    var activeMaskByDisplayOrder = 0L

    /** Visible (== Active and not Clipped) */
    var visibleMaskByIndex = 0L

    /** Pre-compute which data we are going to save into the .ini file (e.g. when order is not altered we won't save order) */
    var settingsSaveFlags = TableFlag.None.i

    /** Offset in g.SettingsTables */
    var settingsOffset = 0
    var lastFrameActive = 0

    /** Number of columns declared in BeginTable() */
    var columnsCount = 0

    /** Number of non-hidden columns (<= ColumnsCount) */
    var columnsActiveCount = 0

    var currentColumn = 0

    var currentRow = 0

    /*
    [0] =   ImS16                   InstanceCurrent
            ImS16                   InstanceInteracted
            S16                     RowFlags
            S16                     LastRowFlags

    [1] =   ImS8                    SortSpecsCount
            ImS8                    DeclColumnsCount
            ImS8                    HoveredColumnBody
            ImS8                    HoveredColumnBorder
            ImS8                    ResizedColumn
            ImS8                    LastResizedColumn
            ImS8                    HeldHeaderColumn
            ImS8                    ReorderColumn

    [2] =   ImS8                        ReorderColumnDir
            ImS8                        RightMostActiveColumn
            ImS8                        LeftMostStretchedColumnDisplayOrder
            ImS8                        ContextPopupColumn
            ImS8                        DummyDrawChannel
            ImS8                        FreezeRowsRequest
            ImS8                        FreezeRowsCount
            ImS8                        FreezeColumnsRequest

    [3] =   ImS8                        FreezeColumnsCount
            bool                        IsLayoutLocked
            bool                        IsInsideRow
            bool                        IsInitializing
            bool                        IsSortSpecsDirty
            bool                        IsUsingHeaders
            bool                        IsContextPopupOpen
            bool                        IsSettingsRequestLoad
            bool                        IsSettingsLoaded
            bool                        IsSettingsDirty
            bool                        IsDefaultDisplayOrder
            bool                        IsResetDisplayOrderRequest
            bool                        IsFreezeRowsPassed
            bool                        HostSkipItems
     */
    private val longs = LongArray(4)

    /** Count of BeginTable() calls with same ID in the same frame (generally 0). This is a little bit similar to BeginCount for a window, but multiple table with same ID look are multiple tables, they are just synched. */
    var instanceCurrent: Int
        get() = (longs[0] shr 48).s.i
        set(value) {
            longs[0] = longs[0] or ((value.L and 0xffff) shl 48)
        }

    /** Mark which instance (generally 0) of the same ID is being interacted with */
    var instanceInteracted: Int
        get() = (longs[0] shr 32).s.i
        set(value) {
            longs[0] = longs[0] or ((value.L and 0xffff) shl 32)
        }
    var rowPosY1 = 0f
    var rowPosY2 = 0f

    /** Height submitted to TableNextRow() */
    var rowMinHeight = 0f
    var rowTextBaseline = 0f
    var rowIndentOffsetX = 0f

    /** Current row flags, see ImGuiTableRowFlags_ */
    var rowFlags: TableRowFlags
        get() = (longs[0] shr 16).s.i
        set(value) {
            longs[0] = longs[0] or ((value.L and 0xffff) shl 16)
        }
    var lastRowFlags: TableRowFlags
        get() = longs[0].s.i
        set(value) {
            longs[0] = longs[0] or (value.L and 0xffff)
        }

    /** Counter for alternating background colors (can be fast-forwarded by e.g clipper) */
    var rowBgColorCounter = 0

    /** Request for current row background color */
    var rowBgColor = 0//u
    var borderColorStrong = 0//u
    var borderColorLight = 0//u
    var borderX1 = 0f
    var borderX2 = 0f
    var hostIndentX = 0f

    /** Padding from each borders */

    var cellPaddingX1 = 0f
    var cellPaddingX2 = 0f
    var cellPaddingY = 0f

    /** Spacing between non-bordered cells */
    var cellSpacingX = 0f

    /** Outer height from last frame */
    var lastOuterHeight = 0f

    /** Height of first row from last frame */
    var lastFirstRowHeight = 0f
    var columnsTotalWidth = 0f
    var innerWidth = 0f
    var resizedColumnNextWidth = 0f

    // Note: OuterRect.Max.y is often FLT_MAX until EndTable(), unless a height has been specified in BeginTable().
    val outerRect = Rect()
    val workRect = Rect()
    val innerClipRect = Rect()

    /**  We use this to cpu-clip cell background color fill */
    val backgroundClipRect = Rect()

    /**  This is used to check if we can eventually merge our columns draw calls into the current draw call of the current window. */
    val hostClipRect = Rect()

    /**  Backup of InnerWindow->WorkRect at the end of BeginTable() */
    val hostWorkRect = Rect()

    /** Backup of InnerWindow->DC.CursorMaxPos at the end of BeginTable() */
    val hostCursorMaxPos = Vec2()

    /** Parent window for the table */
    var outerWindow: Window? = null

    /** Window holding the table data (== OuterWindow or a child window) */
    var innerWindow: Window? = null

    /** Contiguous buffer holding columns names */
    val columnsNames = ArrayList<String>()

    /** We carry our own ImDrawList splitter to allow recursion (FIXME: could be stored outside, worst case we need 1 splitter per recursing table) */
    val drawSplitter = DrawListSplitter()

    /** FIXME-OPT: Fixed-size array / small-vector pattern, optimize for single sort spec */
    lateinit var sortSpecsData: Array<TableSortSpecsColumn>

    /** Public facing sorts specs, this is what we return in TableGetSortSpecs() */
    val sortSpecs = TableSortSpecs()

    var sortSpecsCount: Int
        get() = (longs[1] shr 56).b.i
        set(value) {
            longs[1] = longs[1] or ((value.L and 0xff) shl 56)
        }

    /** Count calls to TableSetupColumn() */
    var declColumnsCount: Int
        get() = (longs[1] shr 48).b.i
        set(value) {
            longs[1] = longs[1] or ((value.L and 0xff) shl 48)
        }

    /** [DEBUG] Unlike HoveredColumnBorder this doesn't fulfill all Hovering rules properly. Used for debugging/tools for now. */
    var hoveredColumnBody: Int
        get() = (longs[1] shr 40).b.i
        set(value) {
            longs[1] = longs[1] or ((value.L and 0xff) shl 40)
        }

    /** Index of column whose right-border is being hovered (for resizing). */
    var hoveredColumnBorder: Int
        get() = (longs[1] shr 32).b.i
        set(value) {
            longs[1] = longs[1] or ((value.L and 0xff) shl 32)
        }

    /** Index of column being resized. Reset when InstanceCurrent==0. */
    var resizedColumn: Int
        get() = (longs[1] shr 24).b.i
        set(value) {
            longs[1] = longs[1] or ((value.L and 0xff) shl 24)
        }

    /** Index of column being resized from previous frame. */
    var lastResizedColumn: Int
        get() = (longs[1] shr 16).b.i
        set(value) {
            longs[1] = longs[1] or ((value.L and 0xff) shl 16)
        }

    /** Index of column header being held. */
    var heldHeaderColumn: Int
        get() = (longs[1] shr 8).b.i
        set(value) {
            longs[1] = longs[1] or ((value.L and 0xff) shl 8)
        }

    // Index of column being reordered. (not cleared)
    var reorderColumn: Int
        get() = longs[1].b.i
        set(value) {
            longs[1] = longs[1] or (value.L and 0xff)
        }

    /** -1 or +1 */
    var reorderColumnDir: Int
        get() = (longs[2] shr 56).b.i
        set(value) {
            longs[2] = longs[2] or ((value.L and 0xff) shl 56)
        }

    /** Index of right-most non-hidden column. */
    var rightMostActiveColumn: Int
        get() = (longs[2] shr 48).b.i
        set(value) {
            longs[2] = longs[2] or ((value.L and 0xff) shl 48)
        }

    /** Display order of left-most stretched column. */
    var leftMostStretchedColumnDisplayOrder: Int
        get() = (longs[2] shr 40).b.i
        set(value) {
            longs[2] = longs[2] or ((value.L and 0xff) shl 40)
        }

    /** Column right-clicked on, of -1 if opening context menu from a neutral/empty spot */
    var contextPopupColumn: Int
        get() = (longs[2] shr 32).b.i
        set(value) {
            longs[2] = longs[2] or ((value.L and 0xff) shl 32)
        }

    /** Redirect non-visible columns here. */
    var dummyDrawChannel: Int
        get() = (longs[2] shr 24).b.i
        set(value) {
            longs[2] = longs[2] or ((value.L and 0xff) shl 24)
        }

    /** Requested frozen rows count */
    var freezeRowsRequest: Int
        get() = (longs[2] shr 16).b.i
        set(value) {
            longs[2] = longs[2] or ((value.L and 0xff) shl 16)
        }

    /** Actual frozen row count (== FreezeRowsRequest, or == 0 when no scrolling offset) */
    var freezeRowsCount: Int
        get() = (longs[2] shr 8).b.i
        set(value) {
            longs[2] = longs[2] or ((value.L and 0xff) shl 8)
        }

    /** Requested frozen columns count */
    var freezeColumnsRequest: Int
        get() = longs[2].b.i
        set(value) {
            longs[2] = longs[2] or (value.L and 0xff)
        }

    /** Actual frozen columns count (== FreezeColumnsRequest, or == 0 when no scrolling offset) */
    var freezeColumnsCount: Int
        get() = (longs[3] shr 56).b.i
        set(value) {
            longs[3] = longs[3] or ((value.L and 0xff) shl 56)
        }

    /** Set by TableUpdateLayout() which is called when beginning the first row. */
    var isLayoutLocked: Boolean
        get() {
            val b: Long = 0b00000000_10000000_00000000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_10000000_00000000_00000000_00000000_00000000_00000000_00000000
            longs[1] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }

    /** Set when inside TableBeginRow()/TableEndRow(). */
    var isInsideRow: Boolean
        get() {
            val b: Long = 0b00000000_01000000_00000000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_01000000_00000000_00000000_00000000_00000000_00000000_00000000
            longs[3] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }
    var isInitializing: Boolean
        get() {
            val b: Long = 0b00000000_00100000_00000000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00100000_00000000_00000000_00000000_00000000_00000000_00000000
            longs[3] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }
    var isSortSpecsDirty: Boolean
        get() {
            val b: Long = 0b00000000_00010000_00000000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00010000_00000000_00000000_00000000_00000000_00000000_00000000
            longs[3] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }

    /** Set when the first row had the ImGuiTableRowFlags_Headers flag. */
    var isUsingHeaders: Boolean
        get() {
            val b: Long = 0b00000000_00001000_00000000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00001000_00000000_00000000_00000000_00000000_00000000_00000000
            longs[3] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }

    /** Set when default context menu is open (also see: ContextPopupColumn, InstanceInteracted). */
    var isContextPopupOpen: Boolean
        get() {
            val b: Long = 0b00000000_00000100_00000000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00000100_00000000_00000000_00000000_00000000_00000000_00000000
            longs[3] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }

    var isSettingsRequestLoad: Boolean
        get() {
            val b: Long = 0b00000000_00000010_00000000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00000010_00000000_00000000_00000000_00000000_00000000_00000000
            longs[3] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }
    var isSettingsLoaded: Boolean
        get() {
            val b: Long = 0b00000000_00000001_00000000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00000001_00000000_00000000_00000000_00000000_00000000_00000000
            longs[3] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }

    /** Set when table settings have changed and needs to be reported into ImGuiTableSetttings data. */
    var isSettingsDirty: Boolean
        get() {
            val b: Long = 0b00000000_00000000_10000000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00000000_10000000_00000000_00000000_00000000_00000000_00000000
            longs[3] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }

    /** Set when display order is unchanged from default (DisplayOrder contains 0...Count-1) */
    var isDefaultDisplayOrder: Boolean
        get() {
            val b: Long = 0b00000000_00000000_01000000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00000000_01000000_00000000_00000000_00000000_00000000_00000000
            longs[3] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }

    var isResetDisplayOrderRequest: Boolean
        get() {
            val b: Long = 0b00000000_00000000_00100000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00000000_00100000_00000000_00000000_00000000_00000000_00000000
            longs[3] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }

    /** Set when we got past the frozen row (the first one). */
    var isFreezeRowsPassed: Boolean
        get() {
            val b: Long = 0b00000000_00000000_00010000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00000000_00010000_00000000_00000000_00000000_00000000_00000000
            longs[3] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }

    /** Backup of InnerWindow->SkipItem at the end of BeginTable(), because we will overwrite InnerWindow->SkipItem on a per-column basis */
    var hostSkipItems: Boolean
        get() {
            val b: Long = 0b00000000_00000000_00001000_00000000_00000000_00000000_00000000_00000000
            return (longs[3] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00000000_00001000_00000000_00000000_00000000_00000000_00000000
            longs[3] = when {
                value -> longs[3] or b
                else -> longs[3] and b.inv()
            }
        }

    init {
        settingsOffset = -1
        instanceInteracted = -1
        lastFrameActive = -1
        lastResizedColumn = -1
        contextPopupColumn = -1
        reorderColumn = -1
    }
}

/** sizeof() ~ 12 */
class TableColumnSettings {
    var widthOrWeight = 0f
    var userID: ID = 0
    private var int = 0
    var index: Int
        get() = (int shr 24).b.i
        set(value) {
            int = int or ((value and 0xff) shl 24)
        }
    var displayOrder: Int
        get() = (int shr 16).b.i
        set(value) {
            int = int or ((value and 0xff) shl 16)
        }
    var sortOrder: Int
        get() = (int shr 8).b.i
        set(value) {
            int = int or ((value and 0xff) shl 8)
        }
    var sortDirection: SortDirection
        get() = SortDirection.values()[((int shr 1) and 0b00000000_01111111).b.i]
        set(value) {
            int = int or ((value.ordinal shl 1) and 0b00000000_11111110)
        }

    /** This is called Active in ImGuiTableColumn, in .ini file we call it Visible. */
    var visible: Boolean
        get() = (int and 1) == 1
        set(value) {
            int = when {
                value -> int or 1
                else -> int and 1.inv()
            }
        }

    init {
        index = -1
        displayOrder = -1
        sortOrder = -1
        sortDirection = SortDirection.None
        visible = true
    }
}

/** See TableUpdateDrawChannels() */
val TABLE_MAX_DRAW_CHANNELS = 2 + 64 * 2


/** Storage for one column of a table
 *
 *  [Internal] sizeof() ~ 100
 *  We use the terminology "Active" to refer to a column that is not Hidden by user or programmatically. We don't use the term "Visible" because it is ambiguous since an Active column can be non-visible because of scrolling. */
class TableColumn {

    /** Clipping rectangle for the column */
    val clipRect = Rect()

    // Optional, value passed to TableSetupColumn()
    var userID: ID = 0

    /** Flags as they were provided by user. See ImGuiTableColumnFlags_ */
    var flagsIn = TableColumnFlag.None.i

    // Effective flags. See ImGuiTableColumnFlags_
    var flags = TableColumnFlag.None.i

    /** Absolute positions */
    var minX = 0f

    /** Absolute positions */
    var maxX = 0f

    /**  ~1.0f. Master width data when (Flags & _WidthStretch) */
    var resizeWeight = -1f

    /** Master width data when !(Flags & _WidthStretch) */
    var widthRequested = -1f

    /** == (MaxX - MinX). FIXME-TABLE: Store all persistent width in multiple of FontSize? */
    var widthGiven = -1f

    /** Start position for the frame, currently ~(MinX + CellPaddingX) */
    var startXRows = 0f

    var startXHeaders = 0f

    /** Submitted contents absolute maximum position, from which we can infer width. */
    var contentMaxPosRowsFrozen = 0f

    /** (kept as float because we need to manipulate those between each cell change)*/
    var contentMaxPosRowsUnfrozen = 0f
    var contentMaxPosHeadersUsed = 0f
    var contentMaxPosHeadersDesired = 0f

    /*
    [0] =   ImS16                   ContentWidthRowsFrozen
            ImS16                   ContentWidthRowsUnfrozen
            ImS16                   ContentWidthHeadersUsed
            ImS16                   ContentWidthHeadersDesired

    [1] =   ImS16                   NameOffset
            bool                    IsActive
            bool                    IsActiveNextFrame
            bool                    IsClipped
            bool                    SkipItems
            ImS8                    DisplayOrder
            ImS8                    IndexWithinActiveSet
            ImS8                    DrawChannelCurrent
            ImS8                    DrawChannelRowsBeforeFreeze
            ImS8                    DrawChannelRowsAfterFreeze
     */
    private val longs = LongArray(3)

    /** Contents width. Because row freezing is not correlated with headers/not-headers we need all 4 variants (ImDrawCmd merging uses different data than alignment code). */
    var contentWidthRowsFrozen: Int
        get() = (longs[0] shr 48).s.i
        set(value) {
            longs[0] = longs[0] and ((value.L and 0xffff) shl 48)
        }

    /** (encoded as ImS16 because we actually rarely use those width) */
    var contentWidthRowsUnfrozen: Int
        get() = (longs[0] shr 32).s.i
        set(value) {
            longs[0] = longs[0] and ((value.L and 0xffff) shl 32)
        }

    /** TableHeader() automatically softclip itself + report ideal desired size, to avoid creating extraneous draw calls */
    var contentWidthHeadersUsed: Int
        get() = (longs[0] shr 16).s.i
        set(value) {
            longs[0] = longs[0] and ((value.L and 0xffff) shl 16)
        }

    var contentWidthHeadersDesired: Int
        get() = longs[0].s.i
        set(value) {
            longs[0] = longs[0] and (value.L and 0xffff)
        }

    /** Offset into parent ColumnsNames[] */
    var nameOffset: Int
        get() = (longs[1] shr 48).s.i
        set(value) {
            longs[1] = longs[1] and ((value.L and 0xffff) shl 48)
        }

    /** Is the column not marked Hidden by the user (regardless of clipping). We're not calling this "Visible" here because visibility also depends on clipping. */
    var isActive: Boolean
        get() {
            val b: Long = 0b00000000_00000000_10000000_00000000_00000000_00000000_00000000_00000000
            return (longs[1] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00000000_10000000_00000000_00000000_00000000_00000000_00000000
            longs[1] = when {
                value -> longs[1] or b
                else -> longs[1] and b.inv()
            }
        }

    var isActiveNextFrame: Boolean
        get() {
            val b: Long = 0b00000000_00000000_01000000_00000000_00000000_00000000_00000000_00000000
            return (longs[1] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00000000_01000000_00000000_00000000_00000000_00000000_00000000
            longs[1] = when {
                value -> longs[1] or b
                else -> longs[1] and b.inv()
            }
        }

    /** Set when not overlapping the host window clipping rectangle. We don't use the opposite "!Visible" name because Clipped can be altered by events. */
    var isClipped: Boolean
        get() {
            val b: Long = 0b00000000_00000000_00100000_00000000_00000000_00000000_00000000_00000000
            return (longs[1] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00000000_00100000_00000000_00000000_00000000_00000000_00000000
            longs[1] = when {
                value -> longs[1] or b
                else -> longs[1] and b.inv()
            }
        }

    var skipItems: Boolean
        get() {
            val b: Long = 0b00000000_00000000_00010000_00000000_00000000_00000000_00000000_00000000
            return (longs[1] and b) == b
        }
        set(value) {
            val b: Long = 0b00000000_00000000_00010000_00000000_00000000_00000000_00000000_00000000
            longs[1] = when {
                value -> longs[1] or b
                else -> longs[1] and b.inv()
            }
        }

    /** Index within Table's IndexToDisplayOrder[] (column may be reordered by users) */
    var displayOrder: Int
        get() = (longs[1] shr 32).b.i
        set(value) {
            longs[1] = longs[1] and ((value.L and 0xff) shl 32)
        }

    /** Index within active/visible set (<= IndexToDisplayOrder) */
    var indexWithinActiveSet: Int
        get() = (longs[1] shr 24).b.i
        set(value) {
            longs[1] = longs[1] and ((value.L and 0xff) shl 24)
        }

    /** Index within DrawSplitter.Channels[] */
    var drawChannelCurrent: Int
        get() = (longs[1] shr 16).b.i
        set(value) {
            longs[1] = longs[1] and ((value.L and 0xff) shl 16)
        }

    var drawChannelRowsBeforeFreeze: Int
        get() = (longs[1] shr 8).b.i
        set(value) {
            longs[1] = longs[1] and ((value.L and 0xff) shl 8)
        }
    var drawChannelRowsAfterFreeze: Int
        get() = longs[1].b.i
        set(value) {
            longs[1] = longs[1] and (value.L and 0xff)
        }

    /** Index of prev active column within Columns[], -1 if first active column */
    var prevActiveColumn: Int
        get() = (longs[2] shr 56).b.i
        set(value) {
            longs[2] = longs[2] and ((value.L and 0xff) shl 56)
        }

    /** Index of next active column within Columns[], -1 if last active column */
    var nextActiveColumn: Int
        get() = (longs[2] shr 48).b.i
        set(value) {
            longs[2] = longs[2] and ((value.L and 0xff) shl 48)
        }

    /** Queue of 8 values for the next 8 frames to request auto-fit */
    var autoFitQueue: Int
        get() = (longs[2] shr 40).b.i
        set(value) {
            longs[2] = longs[2] and ((value.L and 0xff) shl 40)
        }

    /** Queue of 8 values for the next 8 frames to disable Clipped/SkipItem */
    var cannotSkipItemsQueue: Int
        get() = (longs[2] shr 32).b.i
        set(value) {
            longs[2] = longs[2] and ((value.L and 0xff) shl 32)
        }

    /** -1: Not sorting on this column */
    var sortOrder: Int
        get() = (longs[2] shr 24).b.i
        set(value) {
            longs[2] = longs[2] and ((value.L and 0xff) shl 24)
        }

    /** enum ImGuiSortDirection_ */
    var sortDirection: SortDirection
        get() = SortDirection.values()[(longs[2] shr 32).b.i]
        set(value) {
            longs[2] = longs[2] and ((value.ordinal and 0xff) shl 32)
        }


    init {
        nameOffset = -1
        isActive = true
        isActiveNextFrame = true
        displayOrder = -1
        indexWithinActiveSet = -1
        drawChannelCurrent = -1
        drawChannelRowsBeforeFreeze = -1
        drawChannelRowsAfterFreeze = -1
        prevActiveColumn = -1
        nextActiveColumn = -1
        autoFitQueue = (1 shl 3) - 1 // Skip for three frames
        cannotSkipItemsQueue = (1 shl 3) - 1 // Skip for three frames
        sortOrder = -1
        sortDirection = SortDirection.Ascending
    }
}

/** Storage for a table .ini settings
 *
 *  This is designed to be stored in a single ImChunkStream (1 header followed by N ImGuiTableColumnSettings, etc.) */
class TableSettings(columnsCount: Int) {

    /** Set to 0 to invalidate/delete the setting */
    var id: ID = 0
    var saveFlags = TableFlag.None.i
    private var int = 0
    var columnsCount: Int
        get() = (int shr 24).b.i
        set(value) {
            int = int and ((value and 0xff) shl 24)
        }
    var columnsCountMax: Int
        get() = (int shr 16).b.i
        set(value) {
            int = int and ((value and 0xff) shl 16)
        }

    init {
        this.columnsCount = columnsCount
        columnsCountMax = columnsCount
    }

    //    [JVM] we store here
    var columnSettings = Array(columnsCount) { TableColumnSettings() }
}

/** Storage for a column .ini settings */
//class TableColumnsSettings

/** Storage for a window .ini settings (we keep one of those even if the actual window wasn't instanced during this session)
 *
 *  Because we never destroy or rename ImGuiWindowSettings, we can store the names in a separate buffer easily.
 *  [JVM] We prefer keeping the `name` variable
 *
 *  ~ CreateNewWindowSettings */
class WindowSettings(val name: String = "") {
    var id: ID = hash(name)
    var pos = Vec2()
    var size = Vec2()
    var collapsed = false
}

//-----------------------------------------------------------------------------
// Tabs
//-----------------------------------------------------------------------------

class ShrinkWidthItem(var index: Int, var width: Float)
class PtrOrIndex(
        /** Either field can be set, not both. e.g. Dock node tab bars are loose while BeginTabBar() ones are in a pool. */
        val ptr: TabBar?,
        /** Usually index in a main pool. */
        val index: PoolIdx
) {

    constructor(ptr: TabBar) : this(ptr, PoolIdx(-1))

    constructor(index: PoolIdx) : this(null, index)
}

/** Helper: ImPool<>
 *  Basic keyed storage for contiguous instances, slow/amortized insertion, O(1) indexable, O(Log N) queries by ID over a dense/hot buffer,
 *  Honor constructor/destructor. Add/remove invalidate all pointers. Indexes have the same lifetime as the associated object. */
inline class PoolIdx(val i: Int) {
    operator fun inc() = PoolIdx(i + 1)
    operator fun dec() = PoolIdx(i - 1)
    operator fun compareTo(other: PoolIdx): Int = i.compareTo(other.i)
    operator fun minus(int: Int) = PoolIdx(i - int)
}

class TabBarPool {
    /** Contiguous data */
    val list = ArrayList<TabBar?>()

    /** ID->Index */
    val map = mutableMapOf<ID, PoolIdx>()

    operator fun get(key: ID): TabBar? = map[key]?.let { list[it.i] }
    operator fun get(n: PoolIdx): TabBar? = list.getOrNull(n.i)
    fun getIndex(p: TabBar): PoolIdx = PoolIdx(list.indexOf(p))
    fun getOrAddByKey(key: ID): TabBar = map[key]?.let { list[it.i] }
            ?: add().also { map[key] = PoolIdx(list.lastIndex) }

    operator fun contains(p: TabBar): Boolean = p in list
    fun clear() {
        list.clear()
        map.clear()
    }

    fun add(): TabBar = TabBar().also { list += it }
//    fun remove(key: ID, p: TabBar) = remove(key, getIndex(p))
//    fun remove(key: ID, idx: PoolIdx) {
//        list[idx.i] = null
//        map -= key
//    }

    val size: Int
        get() = list.size
}

class Pool<T>(val placementNew: () -> T) {
    val buf = ArrayList<T>()        // Contiguous data
    val map = mutableMapOf<ID, PoolIdx>()        // ID->Index

    fun destroy() = clear()

    fun getByKey(key: ID): T? = map[key]?.let { buf[it.i] }
    operator fun get(key: ID): T? = getByKey(key)

    fun getByIndex(n: PoolIdx): T = buf[n.i]
    operator fun get(n: PoolIdx): T = getByIndex(n)

    fun getIndex(p: T) = PoolIdx(buf.indexOf(p))
    fun getOrAddByKey(key: ID): T {
        map[key]?.let { return buf[it.i] }
        val new = add()
        map[key] = PoolIdx(buf.lastIndex) // not size because ::add already increased it
        return new
    }

    operator fun contains(p: T): Boolean = p in buf
    fun clear() {
        map.clear()
        buf.clear()
    }

    fun add(): T {
        val new = placementNew()
        buf += new
        return new
    }

    @Deprecated("just a placeholder to remind the different behaviour with the indices")
    fun remove(key: ID, p: T) = remove(key, getIndex(p))

    @Deprecated("just a placeholder to remind the different behaviour with the indices")
    fun remove(key: ID, idx: PoolIdx) {
        buf.removeAt(idx.i)
        map.remove(key)
        // update indices in map
        map.replaceAll { _, i -> i - (i > idx).i }
    }
//    void        Reserve(int capacity)
//    { Buf.reserve(capacity); Map.Data.reserve(capacity); }

    val size get() = buf.size
}
