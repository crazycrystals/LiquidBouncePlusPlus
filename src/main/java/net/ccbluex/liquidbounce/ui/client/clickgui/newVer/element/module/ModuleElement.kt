package net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.module

import net.ccbluex.liquidbounce.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.ColorManager
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.components.ToggleSwitch
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.module.value.ValueElement
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.module.value.impl.BooleanElement
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.module.value.impl.ListElement
import net.ccbluex.liquidbounce.ui.client.clickgui.newVer.element.module.value.impl.NumberElement
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ColorUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.MouseUtils
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.ccbluex.liquidbounce.value.Value
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.*

import java.awt.*

class ModuleElement(val module: Module): MinecraftInstance() {

    @JvmStatic
    protected val expandIcon = ResourceLocation("liquidbounce+/expand.png")

    private val toggleSwitch = ToggleSwitch()
    private val valueElements = mutableListOf<ValueElement>()

    private var animHeight = 0F
    private var fadeKeybind = 0F
    private var animPercent = 0F

    private var listeningToKey = false
    var expanded = false

    init {
        for (value in module.values) {
            if (value is BoolValue)
                valueElements.add(BooleanElement(value))
            if (value is ListValue)
                valueElements.add(ListElement(value))
            if (value is IntegerValue)
                valueElements.add(IntElement(value))
            if (value is FloatValue)
                valueElements.add(FloatElement(value))
        }
    }

    fun drawElement(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, height: Float) {
        animPercent = AnimationUtils.animate(if (expanded) 100F else 0F, animPercent, 0.25F * RenderUtils.deltaTime * 0.0075F)
        var expectedHeight = 0F
        for (ve in valueElements)
            if (ve.isDisplayable())
                expectedHeight += ve.valueHeight
        animHeight = animPercent / 100F * (expectedHeight + 10F)

        RenderUtils.originalRoundedRect(x + 9.5F, y + 4.5F, x + width - 9.5F, y + height + animHeight - 4.5F, 4F, ColorManager.buttonOutline.rgb)
        Stencil.write(true)
        RenderUtils.originalRoundedRect(x + 10F, y + 5F, x + width - 10F, y + height + animHeight - 5F, 4F, ColorManager.moduleBackground.rgb)
        Stencil.erase(true)
        RenderUtils.newDrawRect(x + 10F, y + height - 5F, x + width - 10F, y + height - 4.5F, 0xFF303030)
        Fonts.font40.drawString(module.name, x + 20F, y + height / 2F - Fonts.font40.FONT_HEIGHT, -1)
        Fonts.fontSmall.drawString(module.getDescription(), x + 20F, y + height / 2F + 4F, 0xA0A0A0)

        val keyName = if (listeningToKey) "Listening" else Keyboard.getKeyName(module.keyBind)

        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, 
                x + 25F + Fonts.font40.getStringWidth(module.name),
                y + height / 2F - Fonts.font40.FONT_HEIGHT - 2F,
                x + 35F + Fonts.font40.getStringWidth(module.name) + Fonts.fontSmall.getStringWidth(keyName)))
            fadeKeybind = (fadeKeybind + 0.1F * RenderUtils.deltaTime * 0.0095F).coerceIn(0F, 1F)
        else
            fadeKeybind = (fadeKeybind - 0.1F * RenderUtils.deltaTime * 0.0095F).coerceIn(0F, 1F)

        RenderUtils.originalRoundedRect(
                x + 25F + Fonts.font40.getStringWidth(module.name),
                y + height / 2F - Fonts.font40.FONT_HEIGHT - 2F,
                x + 35F + Fonts.font40.getStringWidth(module.name) + Fonts.font40Small.getStringWidth(keyName),
                y + height / 2F, 2F, BlendUtils.blend(Color(0xFF454545), Color(0xFF353535), fadeKeybind).rgb)
        Fonts.fontSmall.drawString(keyName, x + 30F + Fonts.font40.getStringWidth(module.name), y + height / 2F - Fonts.font40.FONT_HEIGHT + 1.5F, -1)

        toggleSwitch.state = module.state

        if (module.values.size > 0) {
            RenderUtils.newDrawRect(x + width - 40F, y + 5F, x + width - 39.5F, y + height - 5F, 0xFF303030)
            GlStateManager.resetColor()
            glPushMatrix()
            glTranslatef(x + width - 25F, y + height / 2F, 0F)
            glPushMatrix()
            glRotatef(180F * (animHeight / (expectedHeight + 10F)), 0F, 0F, 1F)
            glColor4f(1F, 1F, 1F, 1F)
            RenderUtils.drawImage(expandIcon, -4, -4, 8, 8)
            glPopMatrix()
            glPopMatrix()
            toggleSwitch.onDraw(x + width - 70F, y + height / 2F - 5F, 20F, 10F, 0xFF252525)
        } else
            toggleSwitch.onDraw(x + width - 40F, y + height / 2F - 5F, 20F, 10F, 0xFF252525)

        if (expanded || animHeight > 0F) {
            var startYPos = y + height
            for (ve in valueElements)
                if (ve.isDisplayable())
                    startYPos += ve.drawElement(mouseX, mouseY, x + 10F, startYPos, width - 20F, 0xFF252525)
        }
        Stencil.dispose()

        return height + animHeight
    }

    fun handleClick(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, height: Float) {
        if (listeningToKey) {
            resetState()
            return
        }
        val keyName = if (listeningToKey) "Listening" else Keyboard.getKeyName(module.keyBind)
        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, 
                x + 25F + Fonts.font40.getStringWidth(module.name),
                y + height / 2F - Fonts.font40.FONT_HEIGHT - 2F,
                x + 35F + Fonts.font40.getStringWidth(module.name) + Fonts.fontSmall.getStringWidth(keyName))) {
            listeningToKey = true
            return
        }
        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, 
                x + width - if (module.values.size > 0) 70F else 40F, y, 
                x + width - if (module.values.size > 0) 50F else 20F, y + height))
            module.toggle()
        if (module.values.size > 0 && MouseUtils.mouseWithinBounds(mouseX, mouseY, x + width - 40F, y, x + width - 10F, y + height))
            expanded = !expanded
        if (expanded) {
            var startY = y + height
            for (ve in valueElements) {
                if (!ve.isDisplayable()) continue
                ve.onClick(mouseX, mouseY, x + 10F, startY, width - 20F)
                startY += ve.valueHeight
            }
        }
    }

    fun handleRelease(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, height: Float) {
        if (expanded) {
            var startY = y + height
            for (ve in valueElements) {
                if (!ve.isDisplayable()) continue
                ve.onRelease(mouseX, mouseY, x + 10F, startY, width - 20F)
                startY += ve.valueHeight
            }
        }
    }

    fun handleKeyTyped(typed: Char, code: Int): Boolean {
        if (listeningToKey) {
            if (code == 1) {
                module.keyBind = 0
                listeningToKey = false
            } else {
                module.keyBind = code
                listeningToKey = false
            }
            return true
        }
        if (expanded)
            for (ve in valueElements)
                if (ve.isDisplayable() && ve.onKeyPress(typed, code)) return true
        return false
    }

    fun listeningKeybind(): Boolean = listeningToKey
    fun resetState() {
        listeningToKey = false
    }

}