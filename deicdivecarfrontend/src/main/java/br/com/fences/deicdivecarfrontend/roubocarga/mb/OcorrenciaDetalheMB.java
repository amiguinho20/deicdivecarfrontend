package br.com.fences.deicdivecarfrontend.roubocarga.mb;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
//import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.fences.deicdivecarfrontend.roubocarga.bo.RdoRouboCargaReceptacaoBO;
import br.com.fences.fencesutils.formatar.FormatarData;
import br.com.fences.fencesutils.verificador.Verificador;
import br.com.fences.ocorrenciaentidade.ocorrencia.Ocorrencia;
import br.com.fences.ocorrenciaentidade.ocorrencia.natureza.Circunstancia;
import br.com.fences.ocorrenciaentidade.ocorrencia.natureza.Desdobramento;
import br.com.fences.ocorrenciaentidade.ocorrencia.natureza.Modalidade;
import br.com.fences.ocorrenciaentidade.ocorrencia.natureza.Natureza;
import br.com.fences.ocorrenciaentidade.ocorrencia.pessoa.Pessoa;



@Named
//@javax.faces.view.ViewScoped
@SessionScoped //-- por causa do mapa na segunda pagina   
public class OcorrenciaDetalheMB implements Serializable{

	private static final long serialVersionUID = 1866941789765596632L;
	
	@Inject
	private transient Logger logger;

	@Inject
	private RdoRouboCargaReceptacaoBO rdoRouboCargaReceptacaoBO;

	private Ocorrencia ocorrenciaDetalhe;
	private String origemDetalhe;
	
	private boolean informativoFuncionalidade;
	
	
	@PostConstruct
	private void init() {
	} 
	 
	public void mudarInformativoFuncionalidade(){
		informativoFuncionalidade = !informativoFuncionalidade;
	}
	
	/**
	 * A pesquisa lazy de fato e' feita apos o termino da execucao desse metodo, pelo primefaces.
	 * O filtro nao pode ser limpo aqui.
	 */
	public void pesquisar(){ 
	}
 
	  
	/**
	 * Botao limpar. Faz a limpeza e executa a pesquisa
	 */
	public void limpar(){
	}
	
	public void limparMapa(){
	}

	
	public String atualizarOcorrenciaDetalhe(Ocorrencia ocorrenciaParaAtualizar)
	{
		ocorrenciaDetalhe = ocorrenciaParaAtualizar;
		if (ocorrenciaDetalhe != null && Verificador.isValorado(ocorrenciaDetalhe.getId()))
		{
			ocorrenciaDetalhe = rdoRouboCargaReceptacaoBO.consultar(ocorrenciaDetalhe.getId());
		}
		return "ocorrenciadetalhe";      
	} 
	
	public String atualizarOcorrenciaDetalheString(String id)
	{
		if (Verificador.isValorado(id))
		{
			ocorrenciaDetalhe = rdoRouboCargaReceptacaoBO.consultar(id);
		}
		return "ocorrenciadetalhe";      
	}  
	
	
	public String formatarColecaoEmUmBox(String separadorInterno, String separadorExterno, Collection colecao, String arg1)
	{
		return formatarColecaoEmUmBox(true, separadorInterno, separadorExterno, colecao, arg1);
	}
	public String formatarColecaoEmUmBox(String separadorInterno, String separadorExterno, Collection colecao, String arg1, String arg2)
	{
		return formatarColecaoEmUmBox(true, separadorInterno, separadorExterno, colecao, arg1, arg2);
	}
	public String formatarColecaoEmUmBox(String separadorInterno, String separadorExterno, Collection colecao, String arg1, String arg2, String arg3)
	{
		return formatarColecaoEmUmBox(true, separadorInterno, separadorExterno, colecao, arg1, arg2, arg3);
	}
	
	private String formatarColecaoEmUmBox(boolean x, String separadorInterno, String separadorExterno, Collection colecao, String... atributos)
	{
		StringBuilder retorno = new StringBuilder();
		if (Verificador.isValorado(colecao))
		{
			for (Object obj : colecao)
			{
				StringBuilder registro = new StringBuilder();
				for (String atributo : atributos)
				{
					String metodo = "get" + atributo.substring(0, 1).toUpperCase() + atributo.substring(1);
					String valor = null;
					try
					{
						Class<? extends Object> clazz = obj.getClass(); 
						Method method = clazz.getMethod(metodo, new Class[] {});
						valor = (String) method.invoke(obj, null);
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
						logger.error("Erro na reflexao em formatarColecaoEmUmBox do atributo[" + atributo + "]:" + e.getMessage());
						logger.error(e);
						continue;
					}
					if (Verificador.isValorado(valor))
					{
						if (Verificador.isValorado(registro.toString()))
						{
							registro.append(separadorInterno);
						}
						registro.append(valor);
					}
				}
				if (Verificador.isValorado(registro.toString()))
				{
					if (Verificador.isValorado(retorno.toString()))
					{
						retorno.append(separadorExterno);
					}
					retorno.append(registro.toString());
				}
			}
		}
		return retorno.toString();
	}
	
	private String formatarNatureza(TreeNode superior, int nivel)
	{
		nivel++;
		StringBuilder arvoreFormatada = new StringBuilder();
		if (superior != null)
		{
			for (TreeNode inferior : superior.getChildren())
			{	
					if (nivel == 1)
					{
						arvoreFormatada.append("<ul style='margin-top:0px; margin-bottom:0px; padding-left:0px;'>");
					}
					else
					{
						arvoreFormatada.append("<ul>");
					}
	
					String inferiorLabel = (String) inferior.getData();
					arvoreFormatada.append("<li>" + inferiorLabel + "</li>");
	
					if (!inferior.getChildren().isEmpty())
					{
						String inferiorFormatado = formatarNatureza(inferior, nivel);
						arvoreFormatada.append("<li>" + inferiorFormatado + "</li>");
					}
					arvoreFormatada.append("</ul>");
			}
		}
		return arvoreFormatada.toString();
	}

	public String montarNatureza(Ocorrencia ocorrencia)
	{
		TreeNode treeNatureza = new DefaultTreeNode("Natureza", null);
		treeNatureza.setExpanded(true);
		TreeNode natOcorrencia = null;
		TreeNode especie = null;
		TreeNode subespecie = null;
		TreeNode condutaNatureza = null;
		TreeNode desdobramentoCircunstancia = null;
		TreeNode modalidade = null;
		
		if (ocorrencia == null)
		{
			return null;
		}
		
		for (Natureza natureza : ocorrencia.getNaturezas())
		{
			//-- ocorrencia
			boolean possui = false;
			for (TreeNode child : treeNatureza.getChildren())
			{
				if (child.getData().equals(natureza.getDescrOcorrencia()))
				{
					possui = true;
					natOcorrencia = child;
				}
			}
			if (!possui)
			{
				natOcorrencia = new DefaultTreeNode(natureza.getDescrOcorrencia(), treeNatureza);
				natOcorrencia.setExpanded(true);
			}
			
			//-- especie
			possui = false;
			for (TreeNode child : natOcorrencia.getChildren())
			{
				if (child.getData().equals(natureza.getDescrEspecie()))
				{
					possui = true;
					especie = child;
				}
			}
			if (!possui)
			{
				especie = new DefaultTreeNode(natureza.getDescrEspecie(), natOcorrencia);
				especie.setExpanded(true);
			}
			
			//-- subespecie
			possui = false;
			for (TreeNode child : especie.getChildren())
			{
				if (child.getData().equals(natureza.getDescrSubespecie()))
				{
					possui = true;
					subespecie = child;
				}
			}
			if (!possui)
			{
				subespecie = new DefaultTreeNode(natureza.getDescrSubespecie(), especie);
				subespecie.setExpanded(true);
			}

			//-- conduta/natureza
			possui = false;
			String condutaRubrica = null;
			if (Verificador.isValorado(natureza.getRubrica()))
			{
				condutaRubrica = natureza.getRubrica();
			}
			else
			{
				condutaRubrica = natureza.getDescrConduta();
			}
			if (condutaRubrica != null)
			{
				for (TreeNode child : subespecie.getChildren())
				{
					if (child.getData().equals(condutaRubrica))
					{
						possui = true;
						condutaNatureza = child;
					}
				}
				if (!possui)
				{
					condutaNatureza = new DefaultTreeNode(condutaRubrica, subespecie);
					condutaNatureza.setExpanded(true);
				}
			}
			
			//-- desdobramentoCircunstancia
			for (Desdobramento desdobramento : natureza.getDesdobramentos())
			{
				if (Verificador.isValorado(desdobramento.getDescrDesdobramento())){
					possui = false;
					for (TreeNode child : condutaNatureza.getChildren())
					{
						if (child.getData().equals(desdobramento.getDescrDesdobramento()))
						{
							possui = true;
							desdobramentoCircunstancia = child;
						}
					}
					if (!possui)
					{
						desdobramentoCircunstancia = new DefaultTreeNode(desdobramento.getDescrDesdobramento(), condutaNatureza);
						desdobramentoCircunstancia.setExpanded(true);
					}
					//-- modalidade
					for (Modalidade modalid : desdobramento.getModalidades())
					{
						possui = false;
						for (TreeNode child : desdobramentoCircunstancia.getChildren())
						{
							if (child.getData().equals(modalid.getRubrica()))
							{
								possui = true;
								modalidade = child;
							}
						}
						if (!possui)
						{
							modalidade = new DefaultTreeNode(modalid.getRubrica(), desdobramentoCircunstancia);
							modalidade.setExpanded(true);
						}
					}
				}
			}
			for (Circunstancia circunstancia : natureza.getCircunstancias())
			{
				if (Verificador.isValorado(circunstancia.getDescrCircunstancia()))
				{
					possui = false;
					for (TreeNode child : condutaNatureza.getChildren())
					{
						if (child.getData().equals(circunstancia.getDescrCircunstancia()))
						{
							possui = true;
							desdobramentoCircunstancia = child;
						}
					}
					if (!possui)
					{
						desdobramentoCircunstancia = new DefaultTreeNode(circunstancia.getDescrCircunstancia(), condutaNatureza);
						desdobramentoCircunstancia.setExpanded(true);
					}
				}
			}
		}

		String naturezaFormatada = formatarNatureza(treeNatureza, 0);
		return naturezaFormatada;
		
	}

	public String montarNaturezaPessoa(Pessoa pessoa)
	{
		TreeNode treeNatureza = new DefaultTreeNode("Natureza", null);
		treeNatureza.setExpanded(true);
		TreeNode natOcorrencia = null;
		TreeNode especie = null;
		TreeNode subespecie = null;
		TreeNode condutaNatureza = null;
		TreeNode desdobramentoCircunstancia = null;
		TreeNode modalidade = null;
		
		if (pessoa == null)
		{
			return null;
		}
		
		for (br.com.fences.ocorrenciaentidade.ocorrencia.pessoa.Natureza natureza : pessoa.getNaturezas())
		{
			//-- ocorrencia
			boolean possui = false;
			for (TreeNode child : treeNatureza.getChildren())
			{
				if (child.getData().equals(natureza.getDescrOcorrencia()))
				{
					possui = true;
					natOcorrencia = child;
				}
			}
			if (!possui)
			{
				natOcorrencia = new DefaultTreeNode(natureza.getDescrOcorrencia(), treeNatureza);
				natOcorrencia.setExpanded(true);
			}
			
			//-- especie
			possui = false;
			for (TreeNode child : natOcorrencia.getChildren())
			{
				if (child.getData().equals(natureza.getDescrEspecie()))
				{
					possui = true;
					especie = child;
				}
			}
			if (!possui)
			{
				especie = new DefaultTreeNode(natureza.getDescrEspecie(), natOcorrencia);
				especie.setExpanded(true);
			}
			
			//-- subespecie
			if (!Verificador.isValorado(natureza.getDescrSubespecie()))
			{
				continue;
			}
			possui = false;
			for (TreeNode child : especie.getChildren())
			{
				if (child.getData().equals(natureza.getDescrSubespecie()))
				{
					possui = true;
					subespecie = child;
				}
			}
			if (!possui)
			{
				subespecie = new DefaultTreeNode(natureza.getDescrSubespecie(), especie);
				subespecie.setExpanded(true);
			}

			//-- natureza
			if (!Verificador.isValorado(natureza.getRubrica()))
			{
				continue;
			}
			possui = false;
			for (TreeNode child : subespecie.getChildren())
			{
				if (child.getData().equals(natureza.getRubrica()))
				{
					possui = true;
					condutaNatureza = child;
				}
			}
			if (!possui)
			{
				condutaNatureza = new DefaultTreeNode(natureza.getRubrica(), subespecie);
				condutaNatureza.setExpanded(true);
			}

			//-- desdobramento
			if (!Verificador.isValorado(natureza.getDescrDesdobramento()))
			{
				continue;
			}
			possui = false;
			for (TreeNode child : condutaNatureza.getChildren())
			{
				if (child.getData().equals(natureza.getDescrDesdobramento()))
				{
					possui = true;
					desdobramentoCircunstancia = child;
				}
			}
			if (!possui)
			{
				desdobramentoCircunstancia = new DefaultTreeNode(natureza.getDescrDesdobramento(), condutaNatureza);
				desdobramentoCircunstancia.setExpanded(true);
			}

			//-- modalidade
			if (!Verificador.isValorado(natureza.getDescrModalidade()))
			{
				continue;
			}
			possui = false;
			for (TreeNode child : desdobramentoCircunstancia.getChildren())
			{
				if (child.getData().equals(natureza.getDescrModalidade()))
				{
					possui = true;
					modalidade = child;
				}
			}
			if (!possui)
			{
				modalidade = new DefaultTreeNode(natureza.getDescrDesdobramento(), desdobramentoCircunstancia);
				modalidade.setExpanded(true);
			}			
		}
		String naturezaFormatada = formatarNatureza(treeNatureza, 0);
		return naturezaFormatada;
		
	}
	
	
	
	
	

	public String formatarOcorrencia(Ocorrencia ocorrencia)
	{
		StringBuilder ocorrenciaFormatada = new StringBuilder();
		if (ocorrencia != null)
		{
			ocorrenciaFormatada.append(ocorrencia.getNumBo()); 
			ocorrenciaFormatada.append("/");
			ocorrenciaFormatada.append(ocorrencia.getAnoBo());
			ocorrenciaFormatada.append("/");
			ocorrenciaFormatada.append(ocorrencia.getNomeDelegacia());
		}
		return ocorrenciaFormatada.toString();
	}
	
	public String formatarReferencia(Ocorrencia ocorrencia)
	{
		StringBuilder ocorrenciaFormatada = new StringBuilder();
		if (ocorrencia != null)
		{
			ocorrenciaFormatada.append(ocorrencia.getNumReferenciaBo());
			ocorrenciaFormatada.append("/");
			ocorrenciaFormatada.append(ocorrencia.getAnoReferenciaBo());
			ocorrenciaFormatada.append("/");
			ocorrenciaFormatada.append(ocorrencia.getDelegaciaReferencia());
		}
		return ocorrenciaFormatada.toString();
	}
	
	public String formatarEndereco(Ocorrencia ocorrencia)
	{
		String endereco = concatenarEndereco(ocorrencia.getLogradouro(),
				ocorrencia.getNumeroLogradouro(),
				ocorrencia.getBairro(), ocorrencia.getCidade(),
				ocorrencia.getIdUf());
		return endereco;
	}
	
	private String concatenarEndereco(String... campos) 
	{
		String resultado = "";
		for (String campo : campos) 
		{
			if (campo != null && !campo.trim().isEmpty() && !campo.trim().equals("0"))
			{
				campo = campo.replaceAll(",", ""); //-- retirar virgulas adicionais
				if (!resultado.isEmpty())
				{
					resultado += ", ";
				} 
				resultado += campo.trim(); 					
			}
		}
		return resultado;
	}
	
	public String formatarData(String original){
		String formatacao = "";
		if (Verificador.isValorado(original))
		{
			try
			{
				if (original.length() > 10)
				{
					DateFormat dfOrigem = FormatarData.getAnoMesDiaHoraMinutoSegundoConcatenados();
					DateFormat dfDestino = FormatarData.getDiaMesAnoComBarrasEHoraMinutoSegundoComDoisPontos();
					Date data = dfOrigem.parse(original);
					formatacao = dfDestino.format(data);
				}
				else
				{
					DateFormat dfOrigem = FormatarData.getAnoMesDiaContatenado();
					DateFormat dfDestino = FormatarData.getDiaMesAnoComBarras();
					Date data = dfOrigem.parse(original);
					formatacao = dfDestino.format(data);
				}
			}
			catch (ParseException e)
			{
				//@TODO nao tratar - temporario
				e.printStackTrace();
			}
				
		}
		return formatacao;
	}
	
	public String formatarComplementar(Ocorrencia complementar)
	{
		return formatarOcorrencia(complementar) + " - " + formatarEndereco(complementar);
	}
	
	public String formatarNatureza(Natureza natureza)
	{
		StringBuilder nat = new StringBuilder();
		if (natureza != null)
		{
			List<String> naturezas = new ArrayList<>();
			if (Verificador.isValorado(natureza.getDescrOcorrencia()))
			{
				naturezas.add(natureza.getDescrOcorrencia());
			}
			if (Verificador.isValorado(natureza.getDescrOcorrencia()))
			{
				naturezas.add(natureza.getDescrEspecie());
			}
			if (Verificador.isValorado(natureza.getDescrOcorrencia()))
			{
				naturezas.add(natureza.getDescrSubespecie());
			}
			if (Verificador.isValorado(natureza.getRubrica()))
			{
				naturezas.add(natureza.getRubrica());
			}
			if (Verificador.isValorado(natureza.getDescrConduta()))
			{
				naturezas.add(natureza.getDescrConduta());
			}
			for (String aux : naturezas)
			{
				if (!nat.toString().isEmpty())
				{
					nat.append("; ");
				}
				nat.append(aux);
			}
		}
		return nat.toString();
	}
	
	public Ocorrencia getOcorrenciaDetalhe() {
		return ocorrenciaDetalhe;
	}

	public void setOcorrenciaDetalhe(Ocorrencia ocorrenciaDetalhe) {
		this.ocorrenciaDetalhe = ocorrenciaDetalhe;
	}

	public String getOrigemDetalhe() {
		return origemDetalhe;
	}

	public void setOrigemDetalhe(String origemDetalhe) {
		this.origemDetalhe = origemDetalhe;
	}

	public boolean isInformativoFuncionalidade() {
		return informativoFuncionalidade;
	}

	public void setInformativoFuncionalidade(boolean informativoFuncionalidade) {
		this.informativoFuncionalidade = informativoFuncionalidade;
	}

}
