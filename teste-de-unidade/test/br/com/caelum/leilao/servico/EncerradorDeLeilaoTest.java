package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.Carteiro;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.Year;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class EncerradorDeLeilaoTest {

    @Test
    public void deveEncerrarLeiloesQueComecaramUmaSemanaAtras() {
        var antiga = Calendar.getInstance();
        antiga.set(2021, Calendar.JULY, 20);

        var leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(antiga).constroi();
        var leilao2 = new CriadorDeLeilao().para("TV de Plasma").naData(antiga).constroi();


        LeilaoDao leilaodao = mock(LeilaoDao.class);
        when(leilaodao.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

        var encerradorDeLeilao = new EncerradorDeLeilao(leilaodao, mock(Carteiro.class));
        encerradorDeLeilao.encerra();

        assertThat(encerradorDeLeilao.getTotalEncerrados(), equalTo(2));
        assertThat(leilao1.isEncerrado(), equalTo(true));
        assertThat(leilao2.isEncerrado(), equalTo(true));
    }

    @Test
    public void naoDevemEncerrarLeiloesComMenosDeUmaSemana() {
        var ontem = Calendar.getInstance();
        ontem.add(Calendar.DAY_OF_MONTH, -1);

        var leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(ontem).constroi();
        var leilao2 = new CriadorDeLeilao().para("TV de Plasma").naData(ontem).constroi();

        var leilaoDao = mock(LeilaoDao.class);

        when(leilaoDao.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

        var encerraLeilao = new EncerradorDeLeilao(leilaoDao, mock(Carteiro.class));

        encerraLeilao.encerra();

        assertThat(encerraLeilao.getTotalEncerrados(), equalTo(0));
        assertThat(leilao1.isEncerrado(), equalTo(false));
        assertThat(leilao2.isEncerrado(), equalTo(false));

        verify(leilaoDao, never()).atualiza(leilao1);
        verify(leilaoDao, never()).atualiza(leilao2);
    }

    @Test
    public void quandoAListaDeLeilaoForVaiaEncerraLeilaoNaoFazNada() {
        var leilaoDao = mock(LeilaoDao.class);

        when(leilaoDao.correntes()).thenReturn(List.of());

        var encerraLeilao = new EncerradorDeLeilao(leilaoDao, mock(Carteiro.class));

        encerraLeilao.encerra();

        assertThat(encerraLeilao.getTotalEncerrados(), equalTo(0));
    }

    @Test
    public void deveAtualizarLeiloesEncerrados() {
        var data = Calendar.getInstance();
        data.add(Calendar.DAY_OF_MONTH, -9);

        var leilao1 = new CriadorDeLeilao().para("TV de Plasma").naData(data).constroi();

        var leilaoDao = mock(LeilaoDao.class);

        when(leilaoDao.correntes()).thenReturn(List.of(leilao1));

        var encerraLeilao = new EncerradorDeLeilao(leilaoDao, mock(Carteiro.class));
        encerraLeilao.encerra();

        assertThat(encerraLeilao.getTotalEncerrados(), equalTo(1));
        verify(leilaoDao, times(1)).atualiza(leilao1);
    }

    @Test
    public void deveEnviarEmailAposPersistirLeilao() {
        var data = Calendar.getInstance();
        data.add(Calendar.DAY_OF_WEEK_IN_MONTH, -8);

        var leilao1 = new CriadorDeLeilao().para("Qualquer coisa").naData(data).constroi();

        var leilaoDao = mock(LeilaoDao.class);
        when(leilaoDao.correntes()).thenReturn(List.of(leilao1));

        Carteiro carteiro = mock(Carteiro.class);

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(leilaoDao, carteiro);
        encerradorDeLeilao.encerra();

        InOrder inOrder = inOrder(leilaoDao,carteiro);
        inOrder.verify(leilaoDao, times(1)).atualiza(leilao1);
        inOrder.verify(carteiro, times(1)).enviaEmail(leilao1);
    }

    @Test
    public void deveContinuarMesmoQuandODaoFalha() {
        var data = Calendar.getInstance();
        data.add(Calendar.DAY_OF_WEEK_IN_MONTH, -8);

        var leilao1 = new CriadorDeLeilao().para("Qualquer coisa").naData(data).constroi();
        var leilao2 = new CriadorDeLeilao().para("Qualquer coisa 2").naData(data).constroi();

        var leilaoDao = mock(LeilaoDao.class);
        when(leilaoDao.correntes()).thenReturn(List.of(leilao1, leilao2));
        doThrow(new RuntimeException()).when(leilaoDao).atualiza(leilao1);

        Carteiro carteiro = mock(Carteiro.class);

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(leilaoDao, carteiro);
        encerradorDeLeilao.encerra();

        verify(carteiro, never()).enviaEmail(leilao1);

        verify(leilaoDao).atualiza(leilao2);
        verify(carteiro).enviaEmail(leilao2);
    }

    @Test
    public void deveContinuarMesmoQuandOCarteiroFalha() {
        var data = Calendar.getInstance();
        data.add(Calendar.DAY_OF_WEEK_IN_MONTH, -8);

        var leilao1 = new CriadorDeLeilao().para("Qualquer coisa").naData(data).constroi();
        var leilao2 = new CriadorDeLeilao().para("Qualquer coisa 2").naData(data).constroi();

        var leilaoDao = mock(LeilaoDao.class);
        when(leilaoDao.correntes()).thenReturn(List.of(leilao1, leilao2));

        Carteiro carteiro = mock(Carteiro.class);
        doThrow(new RuntimeException()).when(carteiro).enviaEmail(leilao1);

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(leilaoDao, carteiro);
        encerradorDeLeilao.encerra();

        verify(leilaoDao).atualiza(leilao2);
        verify(carteiro).enviaEmail(leilao2);
    }

    @Test
    public void verificaSeCarteiroNuncaEhInvocadoSeDaoSempreFalhar() {
        var data = Calendar.getInstance();
        data.add(Calendar.DAY_OF_WEEK_IN_MONTH, -8);

        var leilao1 = new CriadorDeLeilao().para("Qualquer coisa").naData(data).constroi();
        var leilao2 = new CriadorDeLeilao().para("Qualquer coisa 2").naData(data).constroi();

        var leilaoDao = mock(LeilaoDao.class);
        when(leilaoDao.correntes()).thenReturn(List.of(leilao1, leilao2));
        doThrow(new RuntimeException()).when(leilaoDao).atualiza(any(Leilao.class));

        Carteiro carteiro = mock(Carteiro.class);

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(leilaoDao, carteiro);
        encerradorDeLeilao.encerra();

        verify(carteiro, never()).enviaEmail(any(Leilao.class));
    }
}
