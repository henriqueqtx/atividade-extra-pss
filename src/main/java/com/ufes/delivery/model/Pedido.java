package com.ufes.delivery.model;

import com.mycompany.logsauditoria.LogEntry;
import com.mycompany.logsauditoria.interfaces.ILogger;
import com.ufes.delivery.configuracao.ConfiguracaoService;
import com.ufes.delivery.util.UsuarioLogadoService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Pedido {
    private String codigo;
    private double taxaEntrega = ConfiguracaoService.getTaxaEntregaPadrao();
    private List<Item> itens = new ArrayList<>();
    private Cliente cliente;
    private List<CupomDescontoEntrega> cuponsDescontoEntrega = new ArrayList<>();
    private LocalDateTime data;

    private CupomDescontoPedido cupomPedidoAplicado;

    public Pedido(LocalDateTime data, Cliente cliente, String codigo) {
        if (data == null) {
            throw new IllegalArgumentException("Data do pedido deve ser informada");
        }

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente do pedido deve ser informado");
        }

        this.cliente = cliente;
        this.data = data;
        this.codigo = codigo;
    }

    public void adicionarItem(Item objeto) {
        if (objeto == null) {
            throw new IllegalArgumentException("Item do pedido deve ser informado");
        }

        itens.add(objeto);
    }

    public double getValorPedido() {
        double valor = 0;
        for (Item item : itens) {
            valor += item.valorTotal();
        }
        return valor;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<Item> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public double getTaxaEntrega() {
        return taxaEntrega;
    }

    public double getTotalDescontosTaxaEntrega() {
        double desconto = 0;

        for (CupomDescontoEntrega cupom : cuponsDescontoEntrega) {
            desconto += cupom.getValorDesconto();
        }

        return desconto;
    }

    public List<CupomDescontoEntrega> getCupomDescontoEntrega() {
        return Collections.unmodifiableList(cuponsDescontoEntrega);
    }

    public void limparCuponsDescontoEntrega() {
        cuponsDescontoEntrega.clear();
    }

    public void adicionarCupomDescontoEntrega(CupomDescontoEntrega cupom) {
        if (cupom == null) {
            throw new IllegalArgumentException("Cupom de desconto da entrega deve ser informado");
        }

        double totalDescontosAposAdicionar = getTotalDescontosTaxaEntrega() + cupom.getValorDesconto();
        double limiteAplicavel = taxaEntrega;

        if (cupom.getValorDesconto() < 0) {
            throw new IllegalArgumentException("Desconto na taxa de entrega nao pode ser negativo");
        }

        if (totalDescontosAposAdicionar > limiteAplicavel) {
            throw new IllegalStateException(
                    "Desconto total na taxa de entrega nao pode ultrapassar " + limiteAplicavel);
        }

        cuponsDescontoEntrega.add(cupom);
    }

    public double getTaxaEntregaComDesconto() {
        double taxaComDesconto = taxaEntrega - getTotalDescontosTaxaEntrega();
        if (taxaComDesconto < 0) {
            return 0;
        }
        return taxaComDesconto;
    }

    public double calcularValorTotal(ILogger logger) {
        double valorTotal = getValorPedido() + getTaxaEntregaComDesconto();
        Optional<CupomDescontoPedido> cupomAplicado = getCupomAplicado();
        
        Map<String, String> dadosExtra = new HashMap<>();
        dadosExtra.put("codigo_pedido", this.codigo);
        dadosExtra.put("nome_cliente", this.cliente.getNome());

        try {
            LogEntry log = new LogEntry(
                UsuarioLogadoService.getNomeUsuario(),
                LocalDate.now(),
                LocalTime.now(),
                "Calculo do valor total do pedido (calcularValorTotal)",
                dadosExtra
            );
            logger.registrar(log);
        } catch (Exception e) {
            System.out.println("Erro ao gravar log: " + e.getMessage());
        }
        
        if (cupomAplicado.isPresent()) {
            CupomDescontoPedido cupom = cupomAplicado.get();
            return valorTotal - valorTotal * cupom.getPercentual() / 100;
        }

        return valorTotal;
    }
    
    public double calcularValorTotal() {
        double valorTotal = getValorPedido() + getTaxaEntregaComDesconto();
        Optional<CupomDescontoPedido> cupomAplicado = getCupomAplicado();

        if (cupomAplicado.isPresent()) {
            CupomDescontoPedido cupom = cupomAplicado.get();
            return valorTotal - valorTotal * cupom.getPercentual() / 100;
        }
        return valorTotal;
    }


    public LocalDateTime getData() {
        return data;
    }

    public Optional<CupomDescontoPedido> getCupomAplicado() {
        return Optional.ofNullable(cupomPedidoAplicado);
    }

    public void setCupomAplicado(CupomDescontoPedido cupomPedidoAplicado) {
        if (cupomPedidoAplicado == null) {
            throw new IllegalArgumentException("Cupom do pedido deve ser informado");
        }

        this.cupomPedidoAplicado = cupomPedidoAplicado;
    }

    public String getCodigo() {
        return codigo;
    }
    
    

    @Override
    public String toString() {
        return "Pedido{"
                + "data=" + data
                + ", cliente=" + cliente
                + ", itens=" + itens
                + ", taxaEntrega=" + taxaEntrega
                + ", cuponsDescontoEntrega=" + cuponsDescontoEntrega
                + ", cupomPedidoAplicado=" + cupomPedidoAplicado
                + ", valorPedido=" + getValorPedido()
                + ", totalDescontosTaxaEntrega=" + getTotalDescontosTaxaEntrega()
                + ", valorTotal=" + calcularValorTotal()
                + "}";
    }
}
