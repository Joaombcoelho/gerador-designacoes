package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.Parte;

import java.util.List;
import java.util.Optional;

public class ParteService {

    private final ParteDAO parteDAO;

    public ParteService(ParteDAO parteDAO) {
        this.parteDAO = parteDAO;
    }


    public Parte salvar(Parte parte) {
        validarParte(parte);

        return parteDAO.salvar(parte);
    }


    public List<Parte> listarTodas() {
        return parteDAO.listarTodos();
    }


    public Optional<Parte> buscarPorId(Integer id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "O ID da parte não pode ser nulo."
            );
        }

        return parteDAO.buscarPorId(id);
    }


    public void atualizar(Parte parte) {

        if (parte == null) {
            throw new IllegalArgumentException(
                    "A parte não pode ser nula."
            );
        }

        if (parte.getId() == null) {
            throw new IllegalArgumentException(
                    "A parte precisa possuir ID para atualização."
            );
        }

        validarParte(parte);

        parteDAO.atualizar(parte);
    }


    public void excluir(Integer id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "O ID da parte não pode ser nulo."
            );
        }

        parteDAO.excluir(id);
    }


    private void validarParte(Parte parte) {

        if (parte == null) {
            throw new IllegalArgumentException(
                    "A parte não pode ser nula."
            );
        }

        if (parte.getNome() == null || parte.getNome().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da parte é obrigatório."
            );
        }

        if (parte.getTipo() == null) {
            throw new IllegalArgumentException(
                    "O tipo da parte é obrigatório."
            );
        }

        if (parte.getPrivilegioMinimo() == null) {
            throw new IllegalArgumentException(
                    "O privilégio mínimo é obrigatório."
            );
        }

        if (parte.getSexoPermitido() == null) {
            throw new IllegalArgumentException(
                    "O sexo permitido é obrigatório."
            );
        }
    }
}