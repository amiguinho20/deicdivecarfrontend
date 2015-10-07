package br.com.fences.deicdivecarfrontend.filtrocustom;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import br.com.fences.fencesutils.filtrocustom.ArvoreSimples;
import br.com.fences.fencesutils.filtrocustom.ChaveValor;

@Named
@ApplicationScoped
public class FiltroArvoreConverter implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * RECURSIVO!
	 * Converte a ArvoreSimples para o TreeNode do PrimeFaces.
	 * Na primeira chamada, o segundo argumento deve ser nulo, simbolizando a raiz.
	 * O retorno da primeira chamada eh a raiz.
	 */
	public TreeNode converterArvoreSimplesParaArvorePrime(ArvoreSimples arvore, TreeNode treeNode, boolean desabilitar, boolean expandir)
	{
		TreeNode arvorePrime = null;
		if (arvore != null)
		{
			arvorePrime = new DefaultTreeNode(new ChaveValor<String, String>(arvore.getChave(), arvore.getValor()), treeNode);
			arvorePrime.setExpanded(expandir);
			arvorePrime.setSelectable(!desabilitar);
			for (ArvoreSimples nivel : arvore.getFilhos())
			{
				converterArvoreSimplesParaArvorePrime(nivel, arvorePrime, desabilitar, expandir);
			}
		}
		return arvorePrime;
	}
	
	/**
	 * RECURSIVO!
	 * Converte o TreeNode do PrimeFaces para a ArvoreSimples
	 * Na primeira chamada, o segundo argumento deve ser nulo, simbolizando a raiz.
	 * O retorno da primeira chamada eh a raiz.
	 */
	public ArvoreSimples converterArvorePrimeParaArvoreSimples(TreeNode treeNode, ArvoreSimples arvore, boolean apenasSelecionados)
	{
		ChaveValor<String, String> chaveValor = (ChaveValor<String, String>) treeNode.getData();
		ArvoreSimples arvoreSimples = new ArvoreSimples(chaveValor.getChave(), chaveValor.getValor());
		
		if (arvore != null)
		{	//-- da primeira vez nao tem pai, nas proximas eh a recursao dos filhos
			arvore.getFilhos().add(arvoreSimples);
		}
		
		if (treeNode.getChildCount() > 0)
		{
			for (TreeNode nivel : treeNode.getChildren())
			{
				if (apenasSelecionados)
				{
					boolean possui = possuiFilhoSelecionado(nivel);
					if (!possui && !nivel.isSelected())
					{
						continue;
					}
				}
				
				converterArvorePrimeParaArvoreSimples(nivel, arvoreSimples, apenasSelecionados);
			}
		}
		return arvoreSimples;
	}
	
	private boolean possuiFilhoSelecionado(TreeNode node)
	{
		boolean possui = false;
		
		if (node.getChildCount() > 0)
		{
			for (TreeNode filho : node.getChildren())
			{
				if (filho.isSelected())
				{
					possui = true;
					break;
				}
				else
				{
					possui = possuiFilhoSelecionado(filho);
					if (possui)
					{
						break;
					}
				}
			}
		}
		return possui;
	}

}
