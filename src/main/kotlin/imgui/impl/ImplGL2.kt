package imgui.impl

import glm_.*
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import gln.ShaderType.Companion.FRAGMENT_SHADER
import gln.ShaderType.Companion.VERTEX_SHADER
import gln.checkError
import gln.glGetVec4i
import gln.glScissor
import gln.glViewport
import gln.glf.semantic
import gln.objects.GlProgram
import gln.objects.GlShader
import gln.texture.glBindTexture
import gln.uniform.glUniform
import imgui.*
import kool.*
import org.lwjgl.opengl.GL13C.GL_TEXTURE_2D
import org.lwjgl.opengl.GL15C
import org.lwjgl.opengl.GL21C.*

class ImplGL2 : LwjglRendererI {

    var program = GlProgram(0)
    var matUL = -1
    var CLIP_ORIGIN = false

    val bufferName = IntBuffer<Buffer>()
    val fontTexture = IntBuffer(1)

    override fun createDeviceObjects(): Boolean {

        // this shall be in init, but since we dont have it because we do differently about the glsl version we do this here
        ImGui.io.backendRendererName = "imgui impl opengl2"

        glslVersion = 120 //opengl 2.1

        val lastProgram = glGetInteger(GL_CURRENT_PROGRAM)
        val lastTexture = glGetInteger(GL_TEXTURE_BINDING_2D)
        val lastArrayBuffer = glGetInteger(GL_ARRAY_BUFFER_BINDING)
        val lastElementArrayBuffer = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING)

        program = GlProgram.create().apply {
            val vertHandle = GlShader.createFromSource(VERTEX_SHADER, vertexShader)
            val fragHandle = GlShader.createFromSource(FRAGMENT_SHADER, fragmentShader)
            this += vertHandle
            this += fragHandle
            glBindAttribLocation(i, semantic.attr.POSITION, "Position")
            glBindAttribLocation(i, semantic.attr.TEX_COORD, "UV")
            glBindAttribLocation(i, semantic.attr.COLOR, "Color")
            glLinkProgram(i)
            glDetachShader(i, vertHandle.i)
            glDetachShader(i, fragHandle.i)
            glDeleteShader(vertHandle.i)
            glDeleteShader(fragHandle.i)
        }
        program.used {
            matUL = "mat".uniform
            "Texture".unit = semantic.sampler.DIFFUSE
        }

        glGenBuffers(bufferName)

        glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.Vertex])
        glBufferData(GL_ARRAY_BUFFER, vtxSize.L, GL_STREAM_DRAW)
        glEnableVertexAttribArray(semantic.attr.POSITION)
        glEnableVertexAttribArray(semantic.attr.TEX_COORD)
        glEnableVertexAttribArray(semantic.attr.COLOR)

        glVertexAttribPointer(semantic.attr.POSITION, 2, GL_FLOAT, false, DrawVert.size, 0)
        glVertexAttribPointer(semantic.attr.TEX_COORD, 2, GL_FLOAT, false, DrawVert.size, Vec2.size.toLong())
        glVertexAttribPointer(semantic.attr.COLOR, 4, GL_UNSIGNED_BYTE, true, DrawVert.size, 2L * Vec2.size)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.Element])
        glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, idxSize.L, GL_STREAM_DRAW)

        createFontsTexture()

        glUseProgram(lastProgram)
        glBindTexture(GL_TEXTURE_2D, lastTexture)
        glBindBuffer(GL_ARRAY_BUFFER, lastArrayBuffer)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, lastElementArrayBuffer)

        return checkError("createDeviceObject")
    }

    private fun createFontsTexture(): Boolean {

        if(ImGui.io.fonts.isBuilt)
            return true

        /*  Load as RGBA 32-bits (75% of the memory is wasted, but default font is so small) because it is more likely
            to be compatible with user's existing shaders. If your ImTextureId represent a higher-level concept than
            just a GL texture id, consider calling GetTexDataAsAlpha8() instead to save on GPU memory.  */
        val (pixels, size) = ImGui.io.fonts.getTexDataAsRGBA32()

        // Upload texture to graphics system
        val lastTexture = glGetInteger(GL_TEXTURE_BINDING_2D)

        glGenTextures(fontTexture)
        glBindTexture(GL_TEXTURE_2D, fontTexture)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size.x, size.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels)

        // Store our identifier
        ImGui.io.fonts.texId = fontTexture[0]

        // Restore state
        glBindTexture(GL_TEXTURE_2D, lastTexture)

        return checkError("createFontsTexture")
    }

    private fun destroyFontsTexture() {
        if (fontTexture[0] != 0) {
            glDeleteTextures(fontTexture)
            ImGui.io.fonts.texId = 0
            fontTexture[0] = 0
        }
    }

    override fun destroyDeviceObjects() {
        glDeleteBuffers(bufferName)

        if (program.i >= 0) glDeleteProgram(program.i)

        destroyFontsTexture()
    }

    override fun renderDrawData(drawData: DrawData) {
        val fbSize = ImGui.io.displaySize * ImGui.io.displayFramebufferScale
        if (fbSize anyLessThanEqual 0) return
        drawData scaleClipRects ImGui.io.displayFramebufferScale

        // Backup GL state
        val lastActiveTexture = glGetInteger(GL_ACTIVE_TEXTURE)
        glActiveTexture(GL_TEXTURE0 + semantic.sampler.DIFFUSE)
        val lastProgram = glGetInteger(GL_CURRENT_PROGRAM)
        val lastTexture = glGetInteger(GL_TEXTURE_BINDING_2D)
        val lastArrayBuffer = glGetInteger(GL_ARRAY_BUFFER_BINDING)
        val lastElementBuffer = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING)
        val lastPolygonMode = glGetInteger(GL_POLYGON_MODE)
        val lastViewport = glGetVec4i(GL_VIEWPORT)
        val lastScissorBox = glGetVec4i(GL_SCISSOR_BOX)
        val lastBlendSrcRgb = glGetInteger(GL_BLEND_SRC_RGB)
        val lastBlendDstRgb = glGetInteger(GL_BLEND_DST_RGB)
        val lastBlendSrcAlpha = glGetInteger(GL_BLEND_SRC_ALPHA)
        val lastBlendDstAlpha = glGetInteger(GL_BLEND_DST_ALPHA)
        val lastBlendEquationRgb = glGetInteger(GL_BLEND_EQUATION_RGB)
        val lastBlendEquationAlpha = glGetInteger(GL_BLEND_EQUATION_ALPHA)
        val lastEnableBlend = glIsEnabled(GL_BLEND)
        val lastEnableCullFace = glIsEnabled(GL_CULL_FACE)
        val lastEnableDepthTest = glIsEnabled(GL_DEPTH_TEST)
        val lastEnableScissorTest = glIsEnabled(GL_SCISSOR_TEST)
        val clipOriginLowerLeft = when {
            CLIP_ORIGIN && glGetInteger(GL45C.GL_CLIP_ORIGIN) == GL_UPPER_LEFT -> false // Support for GL 4.5's glClipControl(GL_UPPER_LEFT)
            else -> true
        }

        // Setup render state: alpha-blending enabled, no face culling, no depth testing, scissor enabled
        glEnable(GL_BLEND)
        glBlendEquation(GL_FUNC_ADD)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_CULL_FACE)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_SCISSOR_TEST)
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)

        // Setup viewport, orthographic projection matrix
        glViewport(fbSize)
        val ortho = glm.ortho(mat, 0f, ImGui.io.displaySize.x.f, ImGui.io.displaySize.y.f, 0f)
        glUseProgram(program.i)
        glUniform(matUL, ortho)

        checkSize(drawData.cmdLists)

        val pos = drawData.displayPos
        for (cmdList in drawData.cmdLists) {

            cmdList.vtxBuffer.forEachIndexed { i, v ->
                val offset = i * DrawVert.size
                v.pos.to(vtxBuffer, offset)
                v.uv.to(vtxBuffer, offset + Vec2.size)
                vtxBuffer.putInt(offset + Vec2.size * 2, v.col)
            }
            glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.Vertex])
            glBufferData(GL_ARRAY_BUFFER, vtxBuffer, GL_STREAM_DRAW)
            cmdList.idxBuffer.forEachIndexed { i, idx -> idxBuffer[i] = idx }
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.Element])
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL_STREAM_DRAW)

            var idxBufferOffset = 0L
            for (cmd in cmdList.cmdBuffer) {
                val cb = cmd.userCallback
                if (cb != null)
                // User callback (registered via ImDrawList::AddCallback)
                    cb(cmdList, cmd)
                else {
                    val clipRect = Vec4(cmd.clipRect.x - pos.x, cmd.clipRect.y - pos.y, cmd.clipRect.z - pos.x, cmd.clipRect.w - pos.y);
                    // Apply scissor/clipping rectangle
                    if (clipOriginLowerLeft)
                        glScissor(clipRect.x.i, (fbSize.y - clipRect.w).i, (clipRect.z - clipRect.x).i, (clipRect.w - clipRect.y).i)
                    else
                        glScissor(clipRect.x.i, clipRect.y.i, clipRect.z.i, clipRect.w.i) // Support for GL 4.5's glClipControl(GL_UPPER_LEFT)

                    // Bind texture, Draw
                    glBindTexture(GL_TEXTURE_2D, cmd.textureId!!)
                    glDrawElements(GL_TRIANGLES, cmd.elemCount, GL_UNSIGNED_INT, idxBufferOffset)
                }
                idxBufferOffset += cmd.elemCount * Int.BYTES
            }
        }

        checkError("render")

        // Restore modified GL state
        glUseProgram(lastProgram)
        glBindTexture(GL_TEXTURE_2D, lastTexture)
        glActiveTexture(lastActiveTexture)
        glBindBuffer(GL_ARRAY_BUFFER, lastArrayBuffer)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, lastElementBuffer)
        glBlendEquationSeparate(lastBlendEquationRgb, lastBlendEquationAlpha)
        glBlendFuncSeparate(lastBlendSrcRgb, lastBlendDstRgb, lastBlendSrcAlpha, lastBlendDstAlpha)
        if (lastEnableBlend) glEnable(GL_BLEND) else glDisable(GL_BLEND)
        if (lastEnableCullFace) glEnable(GL_CULL_FACE) else glDisable(GL_CULL_FACE)
        if (lastEnableDepthTest) glEnable(GL_DEPTH_TEST) else glDisable(GL_DEPTH_TEST)
        if (lastEnableScissorTest) glEnable(GL_SCISSOR_TEST) else glDisable(GL_SCISSOR_TEST)
        glPolygonMode(GL_FRONT_AND_BACK, lastPolygonMode)
        glViewport(lastViewport)
        glScissor(lastScissorBox)
    }

    private fun checkSize(draws: ArrayList<DrawList>) {

        val minVtxSize = draws.map { it.vtxBuffer.size }.sum() * DrawVert.size
        val minIdxSize = draws.map { it.idxBuffer.size }.sum() * Int.BYTES

        var newVtxSize = vtxSize
        while (newVtxSize < minVtxSize)
            newVtxSize = newVtxSize shl 1
        var newIdxSize = idxSize
        while (newIdxSize < minIdxSize)
            newIdxSize = newIdxSize shl 1

        if (newVtxSize != vtxSize || newIdxSize != idxSize) {

            vtxSize = newVtxSize
            idxSize = newIdxSize

            vtxBuffer.free()
            vtxBuffer = ByteBuffer(vtxSize)
            idxBuffer.free()
            idxBuffer = IntBuffer(idxSize / Int.BYTES)

            val lastArrayBuffer = glGetInteger(GL_ARRAY_BUFFER_BINDING)
            val lastElementBuffer = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING)

            glBindBuffer(GL_ARRAY_BUFFER, bufferName[Buffer.Vertex])
            glBufferData(GL_ARRAY_BUFFER, vtxSize.L, GL_STREAM_DRAW)
            glEnableVertexAttribArray(semantic.attr.POSITION)
            glEnableVertexAttribArray(semantic.attr.TEX_COORD)
            glEnableVertexAttribArray(semantic.attr.COLOR)

            gln.vertexArray.glVertexAttribPointer(semantic.attr.POSITION, 2, GL_FLOAT, false, DrawVert.size, 0)
            gln.vertexArray.glVertexAttribPointer(semantic.attr.TEX_COORD, 2, GL_FLOAT, false, DrawVert.size, Vec2.size)
            gln.vertexArray.glVertexAttribPointer(semantic.attr.COLOR, 4, GL_UNSIGNED_BYTE, true, DrawVert.size, 2 * Vec2.size)

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName[Buffer.Element])
            glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, idxSize.L, GL_STREAM_DRAW)

            glBindBuffer(GL_ARRAY_BUFFER, lastArrayBuffer)
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, lastElementBuffer)

            checkError("checkSize")

            if (DEBUG) println("new buffers sizes, vtx: $vtxSize, idx: $idxSize")
        }
    }
}