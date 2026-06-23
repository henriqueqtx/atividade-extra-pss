package com.ufes.delivery.desconto.taxa.entrega;

import com.ufes.delivery.model.CupomDescontoEntrega;
import com.ufes.delivery.model.Pedido;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class FormaDescontoTaxaPorBairro implements IFormaDescontoTaxaEntrega {
    private Map<String, Double> descontoBairro;

    public FormaDescontoTaxaPorBairro() {
        descontoBairro = new HashMap<>();

        descontoBairro.put("Centro", 2.00);
        descontoBairro.put("Bela Vista", 3.00);
        descontoBairro.put("Cidade Maravilhosa", 1.50);
    }

    @Override
    public CupomDescontoEntrega calcularDesconto(Pedido pedido) {
        double valorDesconto = buscarDesconto(pedido).orElse(0.0);
        return new CupomDescontoEntrega("Desconto Taxa Bairro", valorDesconto);
    }

    @Override
    public boolean seAplica(Pedido pedido) {
        return buscarDesconto(pedido).isPresent();
    }

    private Optional<Double> buscarDesconto(Pedido pedido) {
        return Optional.ofNullable(descontoBairro.get(pedido.getCliente().getBairro()));
    }
}
