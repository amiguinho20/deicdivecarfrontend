package br.com.fences.deicdivecarfrontend.roubocarga.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import br.com.fences.deicdivecarfrontend.roubocarga.bo.RdoRouboCargaReceptacaoBO;
import br.com.fences.deicdivecarfrontend.roubocarga.entity.FiltroRouboCargaReceptacao;
import br.com.fences.ocorrenciaentidade.ocorrencia.Ocorrencia;

public class RdoRouboCargaReceptacaoLazyDataModel extends LazyDataModel<Ocorrencia> {

	private static final long serialVersionUID = 8313096364754460374L;

	private RdoRouboCargaReceptacaoBO rdoRouboCargaReceptacaoBO;

	private List<Ocorrencia> ocorrencias;
	private FiltroRouboCargaReceptacao filtro;

	public RdoRouboCargaReceptacaoLazyDataModel(RdoRouboCargaReceptacaoBO rdoRouboCargaReceptacaoBO, FiltroRouboCargaReceptacao filtro) {
		this.ocorrencias = new ArrayList<>();
		this.rdoRouboCargaReceptacaoBO = rdoRouboCargaReceptacaoBO;
		this.filtro = filtro;
	}

	/**
	 * Metodo necessario para o "cache" dos registros selecionados via
	 * rowSelectMode = checkbox
	 */
	@Override
	public Ocorrencia getRowData(String rowKey) {
		Ocorrencia ocorrencia = rdoRouboCargaReceptacaoBO.consultar(rowKey);
		return ocorrencia;
	}

	@Override
	public List<Ocorrencia> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, Object> filters) {
		
		ocorrencias = rdoRouboCargaReceptacaoBO.pesquisarLazy(filtro, first, pageSize);

		int count = rdoRouboCargaReceptacaoBO.contar(filtro);
		setRowCount(count);
		
		return ocorrencias;
	}

}
