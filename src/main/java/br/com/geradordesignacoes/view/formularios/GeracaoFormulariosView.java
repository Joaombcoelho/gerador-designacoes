package br.com.geradordesignacoes.view.formularios;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/**
 * Tela separada para conferir os dados persistidos e gerar formulários.
 */
public class GeracaoFormulariosView {

    private final BorderPane root;
    private final DatePicker campoMes;
    private final ListView<String> listaDados;
    private final Button botaoAtualizar;
    private final Button botaoExportarS140;
    private final Label labelStatus;

    public GeracaoFormulariosView() {
        root = new BorderPane();
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(24));

        campoMes = new DatePicker(LocalDate.now());
        campoMes.setPromptText("Selecione uma data do mês");
        campoMes.getStyleClass().add("date-picker");
        campoMes.setMaxWidth(260);

        listaDados = new ListView<>();
        listaDados.getStyleClass().add("list-view");
        listaDados.setPlaceholder(new Label(
                "Selecione um mês e atualize os dados salvos."
        ));

        botaoAtualizar = new Button("Atualizar dados do banco");
        botaoAtualizar.getStyleClass().add("secondary-button");

        botaoExportarS140 = new Button("Gerar S-140 Word");
        botaoExportarS140.getStyleClass().add("success-button");

        labelStatus = new Label(
                "Confira as programações e escalas salvas antes de gerar os formulários."
        );
        labelStatus.setWrapText(true);
        labelStatus.getStyleClass().add("label");

        criarLayout();
    }

    private void criarLayout() {
        Label titulo = new Label("Geração dos formulários");
        titulo.getStyleClass().add("page-title");

        Label subtitulo = new Label(
                "Esta tela consulta as versões persistidas no banco. " +
                "As alterações manuais devem ser salvas antes da geração."
        );
        subtitulo.getStyleClass().add("page-subtitle");
        subtitulo.setWrapText(true);

        Label labelMes = new Label("Mês da reunião");
        labelMes.getStyleClass().add("label");

        HBox selecao = new HBox(10, labelMes, campoMes, botaoAtualizar);
        selecao.setAlignment(Pos.CENTER_LEFT);

        VBox cabecalho = new VBox(8, titulo, subtitulo, selecao);
        root.setTop(cabecalho);

        Label tituloDados = new Label("Dados encontrados no banco");
        tituloDados.getStyleClass().add("card-title");

        VBox painel = new VBox(10, tituloDados, listaDados);
        painel.getStyleClass().add("card");
        VBox.setVgrow(listaDados, Priority.ALWAYS);
        BorderPane.setMargin(painel, new Insets(18, 0, 0, 0));
        root.setCenter(painel);

        HBox botoes = new HBox(10, labelStatus, botaoExportarS140);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(labelStatus, Priority.ALWAYS);
        labelStatus.setMaxWidth(Double.MAX_VALUE);

        VBox rodape = new VBox(12, new Separator(), botoes);
        BorderPane.setMargin(rodape, new Insets(18, 0, 0, 0));
        root.setBottom(rodape);
    }

    public Parent getView() {
        return root;
    }

    public DatePicker getCampoMes() {
        return campoMes;
    }

    public Button getBotaoAtualizar() {
        return botaoAtualizar;
    }

    public Button getBotaoExportarS140() {
        return botaoExportarS140;
    }

    public void atualizarDados(List<String> linhas) {
        listaDados.setItems(FXCollections.observableArrayList(linhas));
    }

    public void atualizarStatus(String mensagem) {
        labelStatus.setText(mensagem == null ? "" : mensagem);
    }
}
