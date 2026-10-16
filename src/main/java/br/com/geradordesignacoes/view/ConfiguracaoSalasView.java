package br.com.geradordesignacoes.view;

import br.com.geradordesignacoes.model.ConfiguracaoSalas;
import br.com.geradordesignacoes.service.ConfiguracaoSalasService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ConfiguracaoSalasView {

    private final ConfiguracaoSalasService service;
    private final VBox root;
    private final CheckBox salaB;
    private final CheckBox salaC;
    private final Label mensagem;

    public ConfiguracaoSalasView() {
        this(new ConfiguracaoSalasService());
    }

    ConfiguracaoSalasView(ConfiguracaoSalasService service) {
        this.service = service;
        this.salaB = new CheckBox("Habilitar Sala B");
        this.salaC = new CheckBox("Habilitar Sala C");
        this.mensagem = new Label();

        root = criarLayout();
    }

    private VBox criarLayout() {
        Label titulo = new Label("Configurações das salas auxiliares");
        titulo.getStyleClass().add("page-title");

        Label subtitulo = new Label(
                "Defina quais salas estarão disponíveis para novas programações."
        );
        subtitulo.getStyleClass().add("page-subtitle");

        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setMaxWidth(560);

        Label cardTitulo = new Label("Salas auxiliares");
        cardTitulo.getStyleClass().add("card-title");

        Label aviso = new Label(
                "Essa configuração não altera programações semanais já existentes."
        );
        aviso.getStyleClass().add("page-subtitle");
        aviso.setWrapText(true);

        HBox botoes = new HBox(10);
        botoes.setAlignment(Pos.CENTER_LEFT);

        Button salvar = new Button("Salvar configurações");
        salvar.getStyleClass().add("primary-button");
        salvar.setOnAction(_ -> salvar());

        botoes.getChildren().add(salvar);

        mensagem.setWrapText(true);

        card.getChildren().addAll(
                cardTitulo,
                aviso,
                salaB,
                salaC,
                botoes,
                mensagem
        );

        VBox conteudo = new VBox(18, titulo, subtitulo, card);
        conteudo.getStyleClass().add("content-area");
        conteudo.setPadding(new Insets(28));
        return conteudo;
    }

    public void atualizar() {
        try {
            ConfiguracaoSalas configuracao = service.buscar();
            salaB.setSelected(configuracao.salaBHabilitada());
            salaC.setSelected(configuracao.salaCHabilitada());
            mensagem.setText("");
            mensagem.getStyleClass().removeAll("status-success", "status-error");
        } catch (RuntimeException e) {
            mostrarMensagem(
                    "Não foi possível carregar as configurações das salas.",
                    false
            );
        }
    }

    private void salvar() {
        try {
            service.salvar(
                    new ConfiguracaoSalas(
                            salaB.isSelected(),
                            salaC.isSelected()
                    )
            );
            mostrarMensagem(
                    "Configurações das salas salvas com sucesso.",
                    true
            );
        } catch (RuntimeException e) {
            mostrarMensagem(
                    "Não foi possível salvar as configurações das salas.",
                    false
            );
        }
    }

    private void mostrarMensagem(String texto, boolean sucesso) {
        mensagem.setText(texto);
        mensagem.getStyleClass().removeAll("status-success", "status-error");
        mensagem.getStyleClass().add(
                sucesso ? "status-success" : "status-error"
        );
    }

    public Parent getView() {
        return root;
    }
}
