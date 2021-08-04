package br.com.caelum.leilao.infra.dao;

import br.com.caelum.leilao.dominio.Leilao;

import java.util.List;

public interface LeilaoDao {
    void salva(Leilao leilao);
    List<Leilao> encerrados();
    List<Leilao> correntes();
    void atualiza(Leilao leilao);
}
