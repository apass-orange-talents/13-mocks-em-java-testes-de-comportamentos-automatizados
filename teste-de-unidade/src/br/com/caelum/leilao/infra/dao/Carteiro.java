package br.com.caelum.leilao.infra.dao;

import br.com.caelum.leilao.dominio.Leilao;

public interface Carteiro {
    void enviaEmail(Leilao leilao);
}
