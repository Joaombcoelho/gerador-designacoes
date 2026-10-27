package br.com.geradordesignacoes.model;

/**
 * Identificadores estáveis das habilidades configuráveis.
 * Os códigos são persistidos no catálogo SQLite e não dependem do texto exibido.
 */
public enum HabilidadeDesignacaoCodigo {
    PRESIDENCIA("PRESIDENCIA", "Presidência"),
    JOIAS_ESPIRITUAIS("JOIAS_ESPIRITUAIS", "Joias espirituais"),
    TESOUROS("TESOUROS", "Tesouros"),
    NECESSIDADES_LOCAIS("NECESSIDADES_LOCAIS", "Necessidades Locais"),
    DISCURSO("DISCURSO", "Discurso");

    private final String codigo;
    private final String nome;

    HabilidadeDesignacaoCodigo(String codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public String codigo() {
        return codigo;
    }

    public String nome() {
        return nome;
    }
}
