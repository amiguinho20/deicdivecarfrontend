package br.com.fences.deicdivecarfrontend.roubocarga.mb;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.omnifaces.util.Messages;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.UploadedFile;

import br.com.fences.deicdivecarentidade.enderecoavulso.EnderecoAvulso;
import br.com.fences.deicdivecarfrontend.roubocarga.bo.EnderecoAvulsoBO;
import br.com.fences.deicdivecarfrontend.roubocarga.entity.FiltroEnderecoAvulso;
import br.com.fences.deicdivecarfrontend.roubocarga.util.EnderecoAvulsoLazyDataModel;
import br.com.fences.fencesutils.verificador.Verificador;

@Named
@ViewScoped
public class EnderecoAvulsoMB implements Serializable{

	private static final long serialVersionUID = 1866941789765596632L;

	@Inject          
	private transient Logger logger;        
	
	@Inject            
	private EnderecoAvulsoBO enderecoAvulsoBO;
	
	@Inject
	private FiltroEnderecoAvulso filtro;
	
	private Integer contagem;
	private LazyDataModel<EnderecoAvulso> enderecosAvulsosResultadoLazy;
	private List<EnderecoAvulso> enderecosAvulsosSelecionados;
	private List<EnderecoAvulso> enderecosAvulsosFiltrados;
            
	//-- inclusao/alteracao
	private EnderecoAvulso enderecoAvulso = new EnderecoAvulso();
	
	//-- upload
	private UploadedFile arquivo;
	private List<EnderecoAvulso> arquivoEnderecosAvulsos;

	private String desconsiderarPrimeiraLinha;
	private String indicadorAtivo;
	private String tipo;
	
	private String formulario = "formLista";

	@PostConstruct
	private void init() {	
		pesquisar();
	} 
	
	public void pesquisar(){
		setEnderecosAvulsosResultadoLazy(new EnderecoAvulsoLazyDataModel(enderecoAvulsoBO, filtro));
		setContagem(getEnderecosAvulsosResultadoLazy().getRowCount());

		limparMapa();
	}

	public void limpar(){
		filtro = new FiltroEnderecoAvulso();
		limparMapa();    
		pesquisar();  
	}
	
	public String incluir(){        
		if (enderecoAvulso != null && !Verificador.isValorado(enderecoAvulso.getId()))
		{ 
			try
			{
				enderecoAvulsoBO.adicionar(enderecoAvulso);
				enderecoAvulso = new EnderecoAvulso();
				setFormulario("formLista"); 
				limpar();
				Messages.addGlobalInfo("Inclusão realizada com sucesso.");
			}
			catch(Exception e)
			{
				Messages.addGlobalError("Ocorreu um erro na inclusão do registro. " + e.getMessage());
			}
		}
		return "enderecoAvulso";
	}
	
	public String alterar(){
		if (enderecoAvulso != null && Verificador.isValorado(enderecoAvulso.getId()))
		{   
			try
			{
				enderecoAvulsoBO.substituir(enderecoAvulso);
				enderecoAvulso = new EnderecoAvulso();
				setFormulario("formLista");
				limpar();
				Messages.addGlobalInfo("Alteração realizada com sucesso.");	
			}
			catch(Exception e)
			{
				Messages.addGlobalError("Ocorreu um erro na alteração do registro. " + e.getMessage());
			}			
		}
		return "enderecoAvulso";
	}
	
	public void ativarRegistrosSelecionados()
	{
		if (enderecosAvulsosSelecionados != null)
		{
			if (enderecosAvulsosSelecionados.isEmpty())
			{
				Messages.addGlobalWarn("Nenhum registro foi selecionado para ativação.");	
			}
			else
			{
				try
				{
					int qtdSelecionados = enderecosAvulsosSelecionados.size();
					for (EnderecoAvulso endAvulso : enderecosAvulsosSelecionados)
					{
						endAvulso.setIndicadorAtivo("Sim");
						enderecoAvulsoBO.substituir(endAvulso);
					}
					enderecosAvulsosSelecionados.clear();
					Messages.addGlobalInfo("A ativação de " + qtdSelecionados + " registro(s) foi realizada com sucesso.");
				}
				catch(Exception e)
				{
					Messages.addGlobalError("Ocorreu um erro na ativação dos registros [" + e.getMessage() + "].");
					Messages.addGlobalFatal(printStackTrace(e));
				}
			}
		}
	}
	
	public void desativarRegistrosSelecionados()
	{
		if (enderecosAvulsosSelecionados != null)
		{
			if (enderecosAvulsosSelecionados.isEmpty())
			{
				Messages.addGlobalWarn("Nenhum registro foi selecionado para desativação.");	
			}
			else
			{
				try
				{
					int qtdSelecionados = enderecosAvulsosSelecionados.size();
					for (EnderecoAvulso endAvulso : enderecosAvulsosSelecionados)
					{
						endAvulso.setIndicadorAtivo("Não");
						enderecoAvulsoBO.substituir(endAvulso);
					}
					enderecosAvulsosSelecionados.clear();
					Messages.addGlobalInfo("A desativação de " + qtdSelecionados + " registro(s) foi realizada com sucesso.");
				}
				catch(Exception e)
				{
					Messages.addGlobalError("Ocorreu um erro na desativação dos registros [" + e.getMessage() + "].");
					Messages.addGlobalFatal(printStackTrace(e));
				}
			}
		}
	}
	
	public void excluirRegistrosSelecionados()
	{
		if (enderecosAvulsosSelecionados != null)
		{
			if (enderecosAvulsosSelecionados.isEmpty())
			{
				Messages.addGlobalWarn("Nenhum registro foi selecionado para exclusão.");	
			}
			else
			{
				try
				{
					int qtdSelecionados = enderecosAvulsosSelecionados.size();
					for (EnderecoAvulso endAvulso : enderecosAvulsosSelecionados)
					{
						enderecoAvulsoBO.remover(endAvulso);
					}
					enderecosAvulsosSelecionados.clear();
					pesquisar(); //-- atualizar lista
					Messages.addGlobalInfo("A exclusão de " + qtdSelecionados + " registro(s) foi realizada com sucesso.");
				}
				catch(Exception e)
				{
					Messages.addGlobalError("Ocorreu um erro na exclusão dos registros [" + e.getMessage() + "].");
					Messages.addGlobalFatal(printStackTrace(e));
				}
			}
		}
	}
	
	
	public void lerArquivo(FileUploadEvent event)
	{
		if (event != null && event.getFile() != null)
		{
			arquivo = event.getFile();
			
			arquivoEnderecosAvulsos = new ArrayList<>();
			Workbook arquivoPlanilha = null;
			try {
				if (arquivo.getFileName().contains(".xlsx"))
				{
					arquivoPlanilha = new XSSFWorkbook(arquivo.getInputstream());  
				}
				else
				{
					arquivoPlanilha = new HSSFWorkbook(arquivo.getInputstream());
				}
				
				Sheet abaPlanilha = arquivoPlanilha.getSheetAt(0);
				Iterator<Row> linhaIterator = abaPlanilha.iterator();
				
				while (linhaIterator.hasNext())
				{
					Row linha = linhaIterator.next();
					
					EnderecoAvulso endAvulso = new EnderecoAvulso();
					arquivoEnderecosAvulsos.add(endAvulso);
					
					endAvulso.setRazaoSocial(recuperarValor(linha.getCell(0)));
					endAvulso.setLogradouro(recuperarValor(linha.getCell(1)));
					endAvulso.setNumero(recuperarValor(linha.getCell(2)));
					endAvulso.setBairro(recuperarValor(linha.getCell(3)));
					endAvulso.setCep(recuperarValor(linha.getCell(4)));
					endAvulso.setCidade(recuperarValor(linha.getCell(5)));
					endAvulso.setUf(recuperarValor(linha.getCell(6)));
				}
			} catch (Exception e) {
				Messages.addGlobalError("Não foi possível ler o arquivo [" + arquivo.getFileName() + "]. Erro: " + e.getMessage() );
				Messages.addGlobalFatal(printStackTrace(e));
			}
		}
		else
		{
			Messages.addGlobalFatal("Arquivo nulo.");
		}
	}
	
	public void carregarArquivo()
	{
		if (arquivoEnderecosAvulsos == null || arquivoEnderecosAvulsos.isEmpty())
		{
			Messages.addGlobalError("O arquivo não foi informado.");
		}
		else
		{
			String REGEX_TEXTO = "\\u0020-\\u007e\\u00a0-\\u00ff|[^\\u005c|\\u0022]";
			
			List<String> ufs = Arrays.asList("SP", "AC", "AL", "AP", "AM",
					"BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
					"PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC",
					"SE", "TO"); 	
			
			int linha = 0;
			boolean valido = true;
			for (EnderecoAvulso endAvulso : arquivoEnderecosAvulsos)
			{
				linha++;
				
				if (linha == 1)
				{
					if (Verificador.isValorado(desconsiderarPrimeiraLinha) && desconsiderarPrimeiraLinha.equalsIgnoreCase("Sim"))
					{
						continue;
					}
				}
				
				String caracteresInvalidos = "";
				if (!Verificador.isValorado(endAvulso.getRazaoSocial()))
				{
					Messages.addGlobalWarn("Linha [" + linha + "] a razão social está vazia.");
					valido = false;
				}
				else
				{
					caracteresInvalidos = endAvulso.getRazaoSocial().replaceAll(REGEX_TEXTO, "");
					if (!caracteresInvalidos.isEmpty())
					{
						Messages.addGlobalWarn("Linha [" + linha + "] na razão social os caracteres especiais " + caracteresInvalidos + " não são permitidos");
						valido = false;
					}
				}
				
				if (!Verificador.isValorado(endAvulso.getLogradouro()))
				{
					Messages.addGlobalWarn("Linha [" + linha + "] o logradouro está vazio.");
					valido = false;
				}
				else
				{
					caracteresInvalidos = endAvulso.getLogradouro().replaceAll(REGEX_TEXTO, "");
					if (!caracteresInvalidos.isEmpty())
					{
						Messages.addGlobalWarn("Linha [" + linha + "] no logradouro os caracteres especiais " + caracteresInvalidos + " não são permitidos");
						valido = false;
					}
				}
				
		    	String valor = endAvulso.getNumero();
		        if (Verificador.isValorado(valor)) 
		        {
		        	if (valor.matches("\\d{1,5}"))
		        	{
		        		int numero = Integer.parseInt(valor);
		        		if (numero < 0 || numero > 50000)
		        		{
							Messages.addGlobalWarn("Linha [" + linha + "] no número apenas números entre 0 e 50000 são permitidos.");
							valido = false;
		        		}
		        	}
		        	else
		        	{
						Messages.addGlobalWarn("Linha [" + linha + "] no número apenas de 1 a 5 dígitos permitidos.");
						valido = false;
		        	}
		        }
		        else
		        {
		        	Messages.addGlobalWarn("Linha [" + linha + "] o número está vazio.");
		        	valido = false;
		        }
				
		        if (Verificador.isValorado(endAvulso.getCep())) 
		        {
		        	endAvulso.setCep(endAvulso.getCep().replaceAll("-", "")); //-- trata o hifen
		        	if (!endAvulso.getCep().matches("\\d{8}"))
		        	{
		        		Messages.addGlobalWarn("Linha [" + linha + "] no cep apenas oito dígitos de 0 a 9 são permitidos.");
		        		valido = false;
		        	}
		        }
		        
				caracteresInvalidos = "";
				if (!Verificador.isValorado(endAvulso.getCidade()))
				{
					Messages.addGlobalWarn("Linha [" + linha + "] a razão social está vazia.");
					valido = false;
				}
				else
				{
					caracteresInvalidos = endAvulso.getCidade().replaceAll(REGEX_TEXTO, "");
					if (!caracteresInvalidos.isEmpty())
					{
						Messages.addGlobalWarn("Linha [" + linha + "] na razão social os caracteres especiais " + caracteresInvalidos + " não são permitidos.");
						valido = false;
					}
				}

				if (!ufs.contains(endAvulso.getUf()))
				{
					Messages.addGlobalWarn("Linha [" + linha + "] não contém uma UF válida em letra maiúscula.");
					valido = false;
				}
			}
						
			if (valido)
			{
				for (EnderecoAvulso endAvulso : arquivoEnderecosAvulsos)
				{
					endAvulso.setIndicadorAtivo(indicadorAtivo);
					endAvulso.setTipo(tipo);
				}
				try
				{
					enderecoAvulsoBO.adicionar(arquivoEnderecosAvulsos);
					Messages.addGlobalInfo("Registros carregados com sucesso.");
					formulario = "formLista";
					arquivo = null;
					arquivoEnderecosAvulsos = null;
				}
				catch(Exception e)
				{
					Messages.addGlobalFatal("Erro na inclusão em lote [{0}]", e.getMessage());
					Messages.addGlobalFatal(printStackTrace(e));
				}
			}
			else
			{
				arquivo = null;
				arquivoEnderecosAvulsos = null;
			}
		}

	}
	
	public String recuperarValor(Cell celula)
	{
		String valor = "";
		try
		{
			valor = celula.getStringCellValue();
		}
		catch(Exception e)
		{
			try
			{
				valor = Double.toString(celula.getNumericCellValue());
				valor = valor.replace(".0","");
			}
			catch(Exception x){}
		}
		return valor;
	}
	
	public String printStackTrace(Throwable exception) {
	    StringWriter stringWriter = new StringWriter();
	    exception.printStackTrace(new PrintWriter(stringWriter, true));
	    return stringWriter.toString();
	}
	  
	public void limparMapa(){
//		geoModel = null;
	}

	
	public Integer getContagem() {
		return contagem;
	}

	public void setContagem(Integer contagem) {
		this.contagem = contagem;
	}


	public FiltroEnderecoAvulso getFiltro() {
		return filtro;
	}

	public void setFiltro(FiltroEnderecoAvulso filtro) {
		this.filtro = filtro;
	}

	public LazyDataModel<EnderecoAvulso> getEnderecosAvulsosResultadoLazy() {
		return enderecosAvulsosResultadoLazy;
	}

	public void setEnderecosAvulsosResultadoLazy(
			LazyDataModel<EnderecoAvulso> enderecosAvulsosResultadoLazy) {
		this.enderecosAvulsosResultadoLazy = enderecosAvulsosResultadoLazy;
	}

	public List<EnderecoAvulso> getEnderecosAvulsosSelecionados() {
		return enderecosAvulsosSelecionados;
	}

	public void setEnderecosAvulsosSelecionados(
			List<EnderecoAvulso> enderecosAvulsosSelecionados) {
		this.enderecosAvulsosSelecionados = enderecosAvulsosSelecionados;
	}

	public EnderecoAvulso getEnderecoAvulso() {
		return enderecoAvulso;
	}

	public void setEnderecoAvulso(EnderecoAvulso enderecoAvulso) {
		this.enderecoAvulso = enderecoAvulso;
	}

	public UploadedFile getArquivo() {
		return arquivo;
	}

	public void setArquivo(UploadedFile arquivo) {
		this.arquivo = arquivo;
	}

	public String getFormulario() {
		return formulario;
	}

	public void setFormulario(String formulario) {
		this.formulario = formulario;
	}

	public String getDesconsiderarPrimeiraLinha() {
		return desconsiderarPrimeiraLinha;
	}

	public void setDesconsiderarPrimeiraLinha(String desconsiderarPrimeiraLinha) {
		this.desconsiderarPrimeiraLinha = desconsiderarPrimeiraLinha;
	}

	public String getIndicadorAtivo() {
		return indicadorAtivo;
	}

	public void setIndicadorAtivo(String indicadorAtivo) {
		this.indicadorAtivo = indicadorAtivo;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

//	public FiltroMapa getFiltroMapa() {
//		return filtroMapa;
//	}
//
//	public void setFiltroMapa(FiltroMapa filtroMapa) {
//		this.filtroMapa = filtroMapa;
//	}
	public List<EnderecoAvulso> getArquivoEnderecosAvulsos() {
		return arquivoEnderecosAvulsos;
	}

	public void setArquivoEnderecosAvulsos(
			List<EnderecoAvulso> arquivoEnderecosAvulsos) {
		this.arquivoEnderecosAvulsos = arquivoEnderecosAvulsos;
	}

	public List<EnderecoAvulso> getEnderecosAvulsosFiltrados() {
		return enderecosAvulsosFiltrados;
	}

	public void setEnderecosAvulsosFiltrados(
			List<EnderecoAvulso> enderecosAvulsosFiltrados) {
		this.enderecosAvulsosFiltrados = enderecosAvulsosFiltrados;
	}

	
}
