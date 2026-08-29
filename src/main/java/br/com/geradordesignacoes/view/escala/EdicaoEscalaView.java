package br.com.geradordesignacoes.view.escala;

import br.com.geradordesignacoes.controller.EdicaoEscalaController;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Pessoa;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Janela de edição de designações persistidas. */
public class EdicaoEscalaView {

    private final Stage stage = new Stage();
    private final TableView<Designacao> tabela = new TableView<>();
    private final ComboBox<Pessoa> responsavel = new ComboBox<>();
    private final ComboBox<Pessoa> ajudante = new ComboBox<>();
    private final Button botaoSalvar = new Button("Salvar alterações");

    public EdicaoEscalaView(Escala escala) {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Editar escala de " + escala.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        TableColumn<Designacao, String> parte = new TableColumn<>("Parte");
        parte.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().parte().getNome()));
        TableColumn<Designacao, String> atual = new TableColumn<>("Participantes atuais");
        atual.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().responsavel().getNome()
                        + (data.getValue().ajudante() == null ? "" : " / " + data.getValue().ajudante().getNome())
        ));
        tabela.getColumns().addAll(parte, atual);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        configurarConversor(responsavel);
        configurarConversor(ajudante);

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);
        formulario.addRow(0, new Label("Responsável:"), responsavel);
        formulario.addRow(1, new Label("Ajudante:"), ajudante);

        VBox raiz = new VBox(12, tabela, formulario, botaoSalvar);
        raiz.setPadding(new javafx.geometry.Insets(12));
        stage.setScene(new javafx.scene.Scene(raiz, 700, 450));

        new EdicaoEscalaController(this, escala);
    }

    private void configurarConversor(ComboBox<Pessoa> combo) {
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Pessoa pessoa) { return pessoa == null ? "" : pessoa.getNome(); }
            @Override public Pessoa fromString(String texto) { return null; }
        });
    }

    public void mostrar() { stage.showAndWait(); }
    public TableView<Designacao> getTabela() { return tabela; }
    public Button getBotaoSalvar() { return botaoSalvar; }
    public Pessoa getResponsavelSelecionado() { return responsavel.getValue(); }
    public Pessoa getAjudanteSelecionado() { return ajudante.getValue(); }
    public void carregarPessoas(List<Pessoa> pessoas) {
        responsavel.setItems(FXCollections.observableArrayList(pessoas));
        ajudante.setItems(FXCollections.observableArrayList(pessoas));
    }
    public void carregarDesignacoes(List<Designacao> designacoes) { tabela.setItems(FXCollections.observableArrayList(designacoes)); }
    public void selecionarParticipantes(Pessoa novoResponsavel, Pessoa novoAjudante) {
        responsavel.setValue(novoResponsavel);
        ajudante.setValue(novoAjudante);
    }

    public boolean confirmarConflitos(Pessoa novoResponsavel, Pessoa novoAjudante, List<LocalDate> datas) {
        String nomes = novoAjudante == null || novoAjudante.getId().equals(novoResponsavel.getId())
                ? novoResponsavel.getNome()
                : novoResponsavel.getNome() + " e/ou " + novoAjudante.getNome();
        String datasFormatadas = datas.stream()
                .map(data -> "- " + data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .collect(java.util.stream.Collectors.joining("\n"));
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Conflito de designações");
        alerta.setHeaderText(null);
        alerta.setContentText(nomes + " já possui outras designações neste mês:\n\n"
                + datasFormatadas + "\n\nDeseja realmente atribuir esta nova designação?");
        return alerta.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    public void exibirAviso(String mensagem) { new Alert(Alert.AlertType.WARNING, mensagem, ButtonType.OK).showAndWait(); }
    public void exibirSucesso(String mensagem) { new Alert(Alert.AlertType.INFORMATION, mensagem, ButtonType.OK).showAndWait(); }
}
