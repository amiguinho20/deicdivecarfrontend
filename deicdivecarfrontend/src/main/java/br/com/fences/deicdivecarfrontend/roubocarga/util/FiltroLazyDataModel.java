package br.com.fences.deicdivecarfrontend.roubocarga.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import br.com.fences.deicdivecarfrontend.filtrocustom.FiltroBO;
import br.com.fences.fencesutils.filtrocustom.FiltroCondicao;
import br.com.fences.ocorrenciaentidade.composto.OcorrenciaResultadoComposto;
import br.com.fences.ocorrenciaentidade.ocorrencia.Ocorrencia;
/**
 * filtrocustom
 * 
 */
public class FiltroLazyDataModel extends LazyDataModel<Ocorrencia> {

	private static final long serialVersionUID = 8313096364754460374L;

	private FiltroBO filtroBO;

	private List<Ocorrencia> ocorrencias;
	private List<FiltroCondicao> filtroCondicoes;

	public FiltroLazyDataModel(FiltroBO filtroBO, List<FiltroCondicao> filtroCondicoes) {
		this.ocorrencias = new ArrayList<>();
		this.filtroBO = filtroBO;
		this.filtroCondicoes = filtroCondicoes;
	}

	/**
	 * Metodo necessario para o "cache" dos registros selecionados via
	 * rowSelectMode = checkbox
	 */
	@Override
	public Ocorrencia getRowData(String rowKey) {
		Ocorrencia ocorrencia = null;
		ocorrencia = filtroBO.consultar(rowKey);
		return ocorrencia;
	}

	@Override
	public List<Ocorrencia> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, Object> filters) {
		
		ocorrencias = filtroBO.pesquisarLazy(filtroCondicoes, first, pageSize);

		//-- se a contagem nao for informada, a lista nao exibe os registros
		int count = filtroBO.contar(filtroCondicoes);
		setRowCount(count);
		
		//setRowCount(ocorrencias.size());
//		OcorrenciaResultadoComposto orc = filtroBO.pesquisarLazyComposto(filtroCondicoes, first, pageSize);
//		ocorrencias = orc.getOcorrencias();
//		setRowCount(orc.getQuantidadeTotal());
		
		
		return ocorrencias;
	}

}
