package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.dao.RepositorioPagamento;
import br.com.caelum.leilao.infra.relogio.Relogio;

import java.util.Calendar;
import java.util.List;

public class GeradorDePagamento {

    private LeilaoDao leilaoDao;
    private RepositorioPagamento repositorioPagamento;
    private Avaliador avaliador;
    private Relogio relogio;

    GeradorDePagamento(LeilaoDao leilaoDao, RepositorioPagamento repositorioPagamento, Avaliador avaliador, Relogio relogio) {
        this.leilaoDao = leilaoDao;
        this.repositorioPagamento = repositorioPagamento;
        this.avaliador = avaliador;
        this.relogio = relogio;
    }

    public void gera() {
        List<Leilao> leiloesEncerrados = this.leilaoDao.encerrados();

        for (var leilao : leiloesEncerrados) {
            this.avaliador.avalia(leilao);
            Pagamento pagamento = new Pagamento(avaliador.getMaiorLance(), primeiroDiaUtil());
            this.repositorioPagamento.salva(pagamento);
        }
    }

    private Calendar primeiroDiaUtil() {
        Calendar data = this.relogio.hoje();
        int diaDaSemana = data.get(Calendar.DAY_OF_WEEK);
        if(diaDaSemana == Calendar.SATURDAY) data.add(Calendar.DAY_OF_MONTH, 2);
        else if(diaDaSemana == Calendar.SUNDAY) data.add(Calendar.DAY_OF_MONTH, 1);
        return data;
    }
}
