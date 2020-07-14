package tz.co.asoft.enterprise.buttons

import kotlinx.html.ButtonType
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.RClass
import styled.css
import styled.styledButton
import tz.co.asoft.react.icons.IconProps
import tz.co.asoft.theme.ThemeConsumer

fun RBuilder.ContainedButton(name: String, icon: RClass<IconProps>? = null, onClick: (() -> Unit)? = null) = ThemeConsumer { theme ->
    styledButton {
        css { +styles.contained(theme) }
        attrs.type = if (onClick == null) ButtonType.submit else ButtonType.button
        attrs.onClickFunction = {
            if (onClick != null) {
                it.preventDefault()
                onClick()
            }
        }
        attrs["data-name"] = name
        ButtonLayout(name, icon)
    }
}