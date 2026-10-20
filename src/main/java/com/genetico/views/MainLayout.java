package com.genetico.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.Lumo;

@StyleSheet("context://styles.css")
public class MainLayout extends AppLayout implements AfterNavigationObserver {
    private static final String ATRIBUTO_TEMA_CLARO = "temaClaro";

    private final H1 tituloTela = new H1();
    private final Button botaoTema = new Button();

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addToDrawer(criarLogo(), criarMenu());
        addToNavbar(criarCabecalho());
        aplicarTemaDaSessao();
    }

    private HorizontalLayout criarLogo() {
        var icone = VaadinIcon.ROAD.create();
        icone.addClassName("logo-icone");

        var logo = new HorizontalLayout(icone, new Span("AgInterface"));
        logo.addClassName("logo");
        logo.setAlignItems(FlexComponent.Alignment.CENTER);

        return logo;
    }

    private SideNav criarMenu() {
        var menu = new SideNav();
        menu.setId("menu-principal");

        menu.addItem(
                new SideNavItem("Endereços", EnderecoView.class, VaadinIcon.MAP_MARKER.create()),
                new SideNavItem("Rotas", RotaView.class, VaadinIcon.ROAD_BRANCHES.create()),
                new SideNavItem("Roteirizações", AlgoritmoGeneticoView.class, VaadinIcon.CLUSTER.create())
        );

        return menu;
    }

    private HorizontalLayout criarCabecalho() {
        tituloTela.addClassName("titulo-tela");

        botaoTema.setId("btn-alternar-tema");
        botaoTema.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        botaoTema.addClickListener(clique -> alternarTema());

        var cabecalho = new HorizontalLayout(new DrawerToggle(), tituloTela, botaoTema);
        cabecalho.addClassName("cabecalho");
        cabecalho.setAlignItems(FlexComponent.Alignment.CENTER);
        cabecalho.expand(tituloTela);

        return cabecalho;
    }

    private void alternarTema() {
        var temaClaro = UI.getCurrent().getElement().getThemeList().contains(Lumo.DARK);
        VaadinSession.getCurrent().setAttribute(ATRIBUTO_TEMA_CLARO, temaClaro);

        aplicarTemaDaSessao();
    }

    private void aplicarTemaDaSessao() {
        var temas = UI.getCurrent().getElement().getThemeList();

        if (Boolean.TRUE.equals(VaadinSession.getCurrent().getAttribute(ATRIBUTO_TEMA_CLARO))) {
            aplicarTemaClaro(temas);
        } else {
            aplicarTemaEscuro(temas);
        }
    }

    private void aplicarTemaClaro(ThemeList temas) {
        temas.remove(Lumo.DARK);
        botaoTema.setText("Tema escuro");
        botaoTema.setIcon(VaadinIcon.MOON_O.create());
    }

    private void aplicarTemaEscuro(ThemeList temas) {
        temas.add(Lumo.DARK);
        botaoTema.setText("Tema claro");
        botaoTema.setIcon(VaadinIcon.SUN_O.create());
    }

    @Override
    public void afterNavigation(AfterNavigationEvent evento) {
        var titulo = getContent().getClass().getAnnotation(PageTitle.class);
        tituloTela.setText(titulo == null ? "" : titulo.value());
    }
}
