package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.dao.RepositorioPagamento;
import br.com.caelum.leilao.infra.relogio.Relogio;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Calendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class GeradorPagamentoTest {

    @Test
    public void deveGerarPagamentoParaUmLeilaoEncerrado() {

        Leilao leilao = new CriadorDeLeilao().para("Qualquer coisa")
                .lance(new Usuario("Maria"), 2000.0)
                .lance(new Usuario("João"), 2500.0)
                .constroi();

        LeilaoDao leilaoDao = mock(LeilaoDao.class);
        when(leilaoDao.encerrados()).thenReturn(List.of(leilao));

        RepositorioPagamento repositorioPagamento = mock(RepositorioPagamento.class);

        GeradorDePagamento geradorDePagamento = new GeradorDePagamento(leilaoDao, repositorioPagamento, new Avaliador(), new Relogio());
        geradorDePagamento.gera();

        ArgumentCaptor<Pagamento> argumentCaptor = ArgumentCaptor.forClass(Pagamento.class);
        verify(repositorioPagamento).salva(argumentCaptor.capture());

        Pagamento pagamento = argumentCaptor.getValue();

        assertThat(pagamento.getValor(), equalTo(2500.0));

    }

    @Test
    public void quandoDiaDoPagamentoForSabadoDeveFazerPagamentoNoPrimeiroDiaUtilPosterior() {
        Leilao leilao = new CriadorDeLeilao().para("Qualquer coisa")
                .lance(new Usuario("Maria"), 2000.0)
                .lance(new Usuario("João"), 2500.0)
                .constroi();

        LeilaoDao leilaoDao = mock(LeilaoDao.class);
        when(leilaoDao.encerrados()).thenReturn(List.of(leilao));

        RepositorioPagamento repositorioPagamento = mock(RepositorioPagamento.class);

        Relogio relogio = mock(Relogio.class);
        Calendar sabado = Calendar.getInstance();
        sabado.set(2021,Calendar.AUGUST, 7);
        when(relogio.hoje()).thenReturn(sabado);

        GeradorDePagamento geradorDePagamento = new GeradorDePagamento(leilaoDao, repositorioPagamento, new Avaliador(), relogio);
        geradorDePagamento.gera();

        ArgumentCaptor<Pagamento> argumentCaptor = ArgumentCaptor.forClass(Pagamento.class);
        verify(repositorioPagamento).salva(argumentCaptor.capture());

        Pagamento pagamento = argumentCaptor.getValue();

        assertThat(pagamento.getData().get(Calendar.DAY_OF_WEEK), equalTo(Calendar.MONDAY));
        assertThat(pagamento.getData().get(Calendar.DAY_OF_MONTH), equalTo(9));
    }

    @Test
    public void quandoDiaDoPagamentoForDomingoDeveFazerPagamentoNoPrimeiroDiaUtilPosterior() {
        Leilao leilao = new CriadorDeLeilao().para("Qualquer coisa")
                .lance(new Usuario("Maria"), 2000.0)
                .lance(new Usuario("João"), 2500.0)
                .constroi();

        LeilaoDao leilaoDao = mock(LeilaoDao.class);
        when(leilaoDao.encerrados()).thenReturn(List.of(leilao));

        RepositorioPagamento repositorioPagamento = mock(RepositorioPagamento.class);

        Relogio relogio = mock(Relogio.class);
        Calendar sabado = Calendar.getInstance();
        sabado.set(2021,Calendar.AUGUST, 8);
        when(relogio.hoje()).thenReturn(sabado);

        GeradorDePagamento geradorDePagamento = new GeradorDePagamento(leilaoDao, repositorioPagamento, new Avaliador(), relogio);
        geradorDePagamento.gera();

        ArgumentCaptor<Pagamento> argumentCaptor = ArgumentCaptor.forClass(Pagamento.class);
        verify(repositorioPagamento).salva(argumentCaptor.capture());

        Pagamento pagamento = argumentCaptor.getValue();

        assertThat(pagamento.getData().get(Calendar.DAY_OF_WEEK), equalTo(Calendar.MONDAY));
        assertThat(pagamento.getData().get(Calendar.DAY_OF_MONTH), equalTo(9));
    }
}
