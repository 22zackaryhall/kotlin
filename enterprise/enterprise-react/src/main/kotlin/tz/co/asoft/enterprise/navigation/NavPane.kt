package tz.co.asoft.enterprise.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.css.*
import kotlinx.css.properties.s
import kotlinx.css.properties.transition
import kotlinx.html.DIV
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.*
import react.router.dom.navLink
import styled.StyledDOMBuilder
import styled.css
import styled.styledDiv
import tz.co.asoft.*
import tz.co.asoft.enterprise.layout.Grid
import tz.co.asoft.enterprise.navigation.NavPane.Props
import tz.co.asoft.enterprise.panels.NavMenu
import tz.co.asoft.enterprise.simplebar.SimpleBar
import tz.co.asoft.react.icons.MdClose

private class NavPane : RComponent<Props, RState>() {
    class Props(
        val drawerController: MutableStateFlow<DrawerState>,
        val header: StyledDOMBuilder<DIV>.(Theme) -> Unit,
        val moduleGroups: Map<String, List<NavMenu>>,
        val iconResolver: RBuilder.(link: String) -> RClass<*>
    ) : RProps

    private fun RBuilder.group(handler: StyledDOMBuilder<DIV>.() -> Unit) = Grid { theme ->
        css {
            position = Position.relative
            paddingBottom = 1.em
            borderBottom = "solid 1px ${theme.onBackgroundVariantColor}"
            child("div") {
                justifySelf = JustifyContent.center
            }
        }
        handler()
    }

    private fun RBuilder.header() = ThemeConsumer { theme ->
        group {
            props.apply { header(theme) }
            styledDiv {
                css {
                    position = Position.absolute
                    top = 0.px
                    right = 0.px
                    fontSize = 1.3.em
                    marginRight = 0.2.em
                    marginTop = 0.2.em
                    cursor = Cursor.pointer
                    zIndex = 5
                }
                attrs.onClickFunction = { props.drawerController.value = DrawerState.Closed }
                MdClose {}
            }
        }
    }

    private fun RBuilder.menu(navMenu: NavMenu) = ThemeConsumer { theme ->
        navLink<RProps>(to = navMenu.link, exact = true, strict = true) {
            styledDiv {
                css {
                    width = 100.pct
                    display = Display.grid
                    padding(vertical = 0.3.em, horizontal = 1.em)
                    gap = Gap("1em")
                    gridTemplateColumns = GridTemplateColumns("1fr 9fr")
                    transition(duration = 0.2.s)
                    cursor = Cursor.pointer
                    div {
                        display = Display.flex
                        alignItems = Align.center
                        justifyContent = JustifyContent.flexStart
                    }
                    hover {
                        backgroundColor = theme.primaryColor
                        color = theme.onPrimaryColor
                        child(".icon") {
                            color = theme.onPrimaryColor
                        }
                    }
                }

                attrs.onClickFunction = { if (isMobile) props.drawerController.value = DrawerState.Closed }

                styledDiv {
                    attrs.classes = setOf("icon")
                    css {
                        justifySelf = JustifyContent.center
                    }
                    props.apply {
                        (iconResolver(navMenu.name)){}
                    }
                }

                styledDiv {
                    css {
                        justifySelf = JustifyContent.start
                    }
                    +navMenu.name
                }
            }
        }
    }

    override fun RBuilder.render(): dynamic = ThemeConsumer { theme ->
        SimpleBar {
            css {
                width = 100.pct
                height = 100.vh
                borderRight = "solid 1px ${theme.onBackgroundVariantColor}"
                backgroundColor = theme.backgroundVariantColor
            }
            header()
            props.moduleGroups.entries.forEach {
                group {
                    css {
                        width = 100.pct
                        display = Display.grid
                        gridTemplateColumns = GridTemplateColumns("1fr")
                        paddingTop = 1.em
                        gap = Gap("0em")
                        child(".active") {
                            backgroundColor = theme.primaryColor
                            color = theme.onPrimaryColor
                        }
                    }
                    it.value.forEach { menu(it) }
                }
            }
        }
    }
}

fun RBuilder.NavPane(
    drawerController: MutableStateFlow<DrawerState>,
    moduleGroups: Map<String, List<NavMenu>>,
    header: StyledDOMBuilder<DIV>.(Theme) -> Unit,
    iconResolver: RBuilder.(link: String) -> RClass<*>
) = child(NavPane::class.js, Props(drawerController, header, moduleGroups, iconResolver)) {}