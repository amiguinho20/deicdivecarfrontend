package br.com.fences.deicdivecarfrontend.roubocarga.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import br.com.fences.deicdivecarentidade.indiciado.Indiciado;
import br.com.fences.deicdivecarfrontend.roubocarga.bo.IndiciadoBO;
import br.com.fences.deicdivecarfrontend.roubocarga.entity.FiltroIndiciado;

public class IndiciadoLazyDataModel extends LazyDataModel<Indiciado> {

	private static final long serialVersionUID = 8313096364754460374L;
	
	private Logger logger =  Logger.getLogger(IndiciadoLazyDataModel.class);  

	private IndiciadoBO indiciadoBO;

	private List<Indiciado> indiciados;
	private FiltroIndiciado filtro;

	public IndiciadoLazyDataModel(IndiciadoBO indiciadoBO, FiltroIndiciado filtro) {
		this.indiciados = new ArrayList<>();
		this.indiciadoBO = indiciadoBO;
		this.filtro = filtro;
	}

	/**
	 * Metodo necessario para o "cache" dos registros selecionados via
	 * rowSelectMode = checkbox
	 */
	@Override
	public Indiciado getRowData(String rowKey) {
		Indiciado indiciado = indiciadoBO.consultar(rowKey);
		return indiciado;
	}

	@Override
	public List<Indiciado> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, Object> filters) {
	
		filtro.setPrimeMapFiltro(filters);
		
		indiciados = indiciadoBO.pesquisarLazy(filtro, first, pageSize);

		int count = indiciadoBO.contar(filtro);
		setRowCount(count);

		logger.info("contagem indiciados: " + getRowCount());
		
		filtro.setPrimeMapFiltro(null);
		
		return indiciados;
	}

}
