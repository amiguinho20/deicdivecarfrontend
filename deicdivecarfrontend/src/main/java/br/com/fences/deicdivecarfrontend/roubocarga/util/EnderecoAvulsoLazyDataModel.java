package br.com.fences.deicdivecarfrontend.roubocarga.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import br.com.fences.deicdivecarentidade.enderecoavulso.EnderecoAvulso;
import br.com.fences.deicdivecarfrontend.roubocarga.bo.EnderecoAvulsoBO;
import br.com.fences.deicdivecarfrontend.roubocarga.entity.FiltroEnderecoAvulso;

public class EnderecoAvulsoLazyDataModel extends LazyDataModel<EnderecoAvulso> {

	private static final long serialVersionUID = 8313096364754460374L;
	
	private Logger logger =  Logger.getLogger(EnderecoAvulsoLazyDataModel.class);  


	private EnderecoAvulsoBO enderecoAvulsoBO;

	private List<EnderecoAvulso> enderecosAvulsos;
	private FiltroEnderecoAvulso filtro;

	public EnderecoAvulsoLazyDataModel(EnderecoAvulsoBO enderecoAvulsoBO, FiltroEnderecoAvulso filtro) {
		this.enderecosAvulsos = new ArrayList<>();
		this.enderecoAvulsoBO = enderecoAvulsoBO;
		this.filtro = filtro;
	}

	/**
	 * Metodo necessario para o "cache" dos registros selecionados via
	 * rowSelectMode = checkbox
	 */
	@Override
	public EnderecoAvulso getRowData(String rowKey) {
		EnderecoAvulso enderecoAvulso = enderecoAvulsoBO.consultar(rowKey);
		return enderecoAvulso;
	}

	@Override
	public List<EnderecoAvulso> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, Object> filters) {
	
		filtro.setPrimeMapFiltro(filters);
		
		enderecosAvulsos = enderecoAvulsoBO.pesquisarLazy(filtro, first, pageSize);

		int count = enderecoAvulsoBO.contar(filtro);
		setRowCount(count);

		filtro.setPrimeMapFiltro(null);
		
		return enderecosAvulsos;
	}

}
