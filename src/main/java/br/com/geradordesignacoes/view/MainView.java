package br.com.geradordesignacoes.view;

import br.com.geradordesignacoes.view.escala.EscalaView;
import br.com.geradordesignacoes.view.historico.HistoricoView;
import br.com.geradordesignacoes.view.parte.ParteView;
import br.com.geradordesignacoes.view.pessoa.PessoaView;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class MainView {

    private final BorderPane root;
    private final PessoaView pessoaView = new PessoaView();
    private final ParteView parteView = new ParteView();
    private final EscalaView escalaView = new EscalaView();
    private final HistoricoView historicoView = new HistoricoView();

    public MainView() {
        root = new BorderPane();

        criarMenu();
        criarTelaInicial();
    }

    private void criarMenu() {

        MenuBar menuBar = new MenuBar();

        // Arquivo
        Menu menuArquivo = new Menu("Arquivo");
        MenuItem itemSair = new MenuItem("Sair");
        menuArquivo.getItems().add(itemSair);

        // Cadastros
        Menu menuCadastros = new Menu("Cadastros");
        MenuItem itemPessoas = new MenuItem("Pessoas");
        MenuItem itemPartes = new MenuItem("Partes");

        menuCadastros.getItems().addAll(itemPessoas, itemPartes);

        // Escala
        Menu menuEscala = new Menu("Escala");
        MenuItem itemGerarEscala = new MenuItem("Gerar Escala");
        menuEscala.getItems().add(itemGerarEscala);

        // Histórico
        Menu menuHistorico = new Menu("Histórico");
        MenuItem itemConsultarHistorico = new MenuItem("Consultar Histórico");
        menuHistorico.getItems().add(itemConsultarHistorico);

        // Ajuda
        Menu menuAjuda = new Menu("Ajuda");
        MenuItem itemSobre = new MenuItem("Sobre");
        menuAjuda.getItems().add(itemSobre);

        // Eventos
        itemPessoas.setOnAction(e ->
                mostrarTela(pessoaView.getView()));

        itemPartes.setOnAction(e ->
                mostrarTela(parteView.getView()));

        itemGerarEscala.setOnAction(e ->
                mostrarTela(escalaView.getView()));

        itemConsultarHistorico.setOnAction(e ->
                mostrarTela(historicoView.getView()));

        // Adiciona os menus
        menuBar.getMenus().addAll(
                menuArquivo,
                menuCadastros,
                menuEscala,
                menuHistorico,
                menuAjuda
        );

        root.setTop(menuBar);
    }

    private void criarTelaInicial() {

        Label titulo = new Label("Bem-vindo ao Gerador de Designações");

        StackPane painel = new StackPane(titulo);
        painel.setAlignment(Pos.CENTER);

        root.setCenter(painel);
    }

    private void mostrarTela(Parent view) {
        root.setCenter(view);
    }

    public Parent getView() {
        return root;
    }

}